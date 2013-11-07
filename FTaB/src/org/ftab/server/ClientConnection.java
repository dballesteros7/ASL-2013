/**
 * ClientConnection.java
 * Created: Oct 14, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ftab.communication.ProtocolMessage;
import org.ftab.communication.exceptions.InvalidHeaderException;
import org.ftab.communication.requests.ConnectionRequest;
import org.ftab.communication.requests.QueueModificationRequest;
import org.ftab.communication.requests.RetrieveMessageRequest;
import org.ftab.communication.requests.RetrieveMessageRequest.Filter;
import org.ftab.communication.requests.RetrieveMessageRequest.Order;
import org.ftab.communication.requests.SendMessageRequest;
import org.ftab.communication.responses.GetQueuesResponse;
import org.ftab.communication.responses.RequestResponse;
import org.ftab.communication.responses.RequestResponse.Status;
import org.ftab.communication.responses.RetrieveMessageResponse;
import org.ftab.database.Client;
import org.ftab.database.Message;
import org.ftab.database.client.ChangeClientStatus;
import org.ftab.database.client.CreateClient;
import org.ftab.database.client.FetchClient;
import org.ftab.database.exceptions.CreateMessageException;
import org.ftab.database.exceptions.InexistentClientException;
import org.ftab.database.exceptions.InexistentQueueException;
import org.ftab.database.exceptions.QueueAlreadyExistsException;
import org.ftab.database.exceptions.QueueNotEmptyException;
import org.ftab.database.message.CreateMessage;
import org.ftab.database.message.DeleteMessage;
import org.ftab.database.message.RetrieveMessage;
import org.ftab.database.queue.CreateQueue;
import org.ftab.database.queue.DeleteQueue;
import org.ftab.database.queue.GetQueuesWithMessages;
import org.ftab.server.exceptions.RemoteSocketClosedException;

/**
 * Framework object that takes care of processing the requests from a single
 * client in the associated socket channel.
 */
public class ClientConnection {
    /**
     * The class' logger
     */
    private final static Logger LOGGER = Logger.getLogger(ClientConnection.class
            .getName());

    /**
     * Status of the reading operation. The object is either reading the request
     * header or body at any point.
     */
    private static enum ReadStatus {
        READING_HEADER, READING_BODY
    };

    /**
     * Status of the write operations. IDLE means there is nothing else to write
     * at the moment but the last message was not the last one, WRITING
     * indicates that there are more bytes to write and DISCONNECT means that
     * the SocketChannel can be closed.
     */
    public static enum WriteStatus {
        IDLE, WRITING, DISCONNECT
    };

    /**
     * Fixed size buffer to maintain the header of requests.
     */
    private final ByteBuffer headerBuffer;
    /**
     * Dynamic buffer to store the body of requests.
     */
    private ByteBuffer bodyBuffer;
    /**
     * Queue with buffers to write through the socket.
     */
    private LinkedList<ByteBuffer> writeBuffer;
    /**
     * Database client object with the client information.
     */
    private Client client;
    /**
     * Current reading status.
     */
    private ReadStatus readingStatus;
    /**
     * Status of the connection, indicates if the client did the initial
     * connection request already.
     */
    private boolean connected;
    /**
     * Flag to indicate that as soon as the write queue is empty we must
     * disconnect the socket.
     */
    private boolean disconnectionRequested;
    /**
     * Database connection pool handler.
     */
    private final DBConnectionDispatcher dbConnectionDispatcher;

    /**
     * Create a new framework object with the given DB connection pool and
     * initial disconnected state. It is ready to read requests.
     * 
     * @param nDispatcher
     *            database connection pool handler.
     */
    public ClientConnection(DBConnectionDispatcher nDispatcher) {
        headerBuffer = ByteBuffer.allocate(ProtocolMessage.HEADER_SIZE);
        client = null;
        connected = false;
        readingStatus = ReadStatus.READING_HEADER;
        writeBuffer = new LinkedList<ByteBuffer>();
        dbConnectionDispatcher = nDispatcher;
        disconnectionRequested = false;
    }

    /**
     * Read bytes out of the socket channel and process the header/body once the
     * corresponding buffer is full. If the body buffer is full then this method
     * may return true if there is a new buffer in the write queue.
     * 
     * @param sc
     *            socket channel to read from.
     * @return true if there is a new buffer with information in the write
     *         queue, false otherwise.
     * @throws IOException
     *             if there is an error while reading from the channel.
     * @throws RemoteSocketClosedException
     *             if the socket is closed remotely.
     */
    public boolean processRead(SocketChannel sc) throws IOException,
            RemoteSocketClosedException {
        if (disconnectionRequested) {
            // Block the reads if a disconnect was requested, let the outbound
            // buffer clear itself.
            LOGGER.warning("Client " + ServerLogger.parseSocketAddress(sc) +
                    " attempted to perform additional operations after " +
                    "requesting a disconnect.");
            return true;
        }
        switch (readingStatus) {
        case READING_HEADER:
            int r = sc.read(headerBuffer);
            if (r < 0) {
                LOGGER.severe("Socket was closed unexpectedly by " +
                        ServerLogger.parseSocketAddress(sc));
                throw new RemoteSocketClosedException();
            }
            if (!headerBuffer.hasRemaining()) {
                headerBuffer.flip();
                try {
                    int bodyLength = processHeader();
                    readingStatus = ReadStatus.READING_BODY;
                    bodyBuffer = ByteBuffer.allocate(bodyLength);
                    LOGGER.config("Finished reading header for new request from " +
                            ServerLogger.parseSocketAddress(sc) +
                            ", expecting now " +
                            bodyLength +
                            " bytes for the body.");
                } catch (InvalidHeaderException e) {
                    LOGGER.severe("Received an invalid header from " +
                            ServerLogger.parseSocketAddress(sc) +
                            " , ignoring it and awaiting a new one.");
                    headerBuffer.clear();
                }
            }
            return false;
        case READING_BODY:
            int k = sc.read(bodyBuffer);
            if (k < 0) {
                LOGGER.severe("Socket was closed unexpectedly by " +
                        ServerLogger.parseSocketAddress(sc));
                throw new RemoteSocketClosedException();
            }
            if (!bodyBuffer.hasRemaining()) {
                bodyBuffer.flip();
                boolean needWrite = processBody(ServerLogger
                        .parseSocketAddress(sc));
                headerBuffer.clear();
                bodyBuffer = null;
                readingStatus = ReadStatus.READING_HEADER;
                return needWrite;
            }
            return false;
        }
        return false;
    }

    /**
     * Write bytes from the buffers in the write queue to the socket channel. It
     * returns a status indicating if the object has anything else to write
     * after the operation and whether it is the last message to be sent.
     * 
     * @param sc
     *            socket channel where to write.
     * @return the current writing status of the object.
     * @throws IOException
     *             if there is an error writing to the socket.
     */
    public WriteStatus processWrite(SocketChannel sc) throws IOException {
        if (writeBuffer.size() == 0 && disconnectionRequested) {
            LOGGER.config("There are no more responses in the queue and a " +
                    "disconnect was requested, changing the writing " +
                    "status for " + ServerLogger.parseSocketAddress(sc) + ".");
            return WriteStatus.DISCONNECT;
        }
        if (writeBuffer.size() == 0 && !disconnectionRequested) {
            LOGGER.config("Finished writing the queued responses for " +
                    ServerLogger.parseSocketAddress(sc) + ".");
            return WriteStatus.IDLE;
        }
        ByteBuffer toWrite = writeBuffer.peek();
        sc.write(toWrite);
        if (!toWrite.hasRemaining()) {
            writeBuffer.pop();
            LOGGER.config("Finished sending a responses to " +
                    ServerLogger.parseSocketAddress(sc) + ".");
        }
        return WriteStatus.WRITING;
    }

    /**
     * Process the header buffer after it is full. This validates the header
     * content and determines the size of the message body.
     * 
     * @return the size of the body.
     * @throws InvalidHeaderException
     *             if the header content is invalid.
     */
    private int processHeader() throws InvalidHeaderException {
        // Get the expected body size
        int bodySize = ProtocolMessage.getBodySize(headerBuffer);
        // Flip the buffer so we can use it when processing the body
        headerBuffer.flip();
        return bodySize;
    }

    /**
     * Process a full body buffer and carry out the provided request.
     * 
     * @param address
     *            String representation of the remote address originating the
     *            requests.
     * @return true if there is a new object in the write queue, false
     *         otherwise.
     */
    private boolean processBody(String address) {
        ProtocolMessage request = ProtocolMessage.fromBytes(bodyBuffer);
        ByteBuffer nextResponseBuffer = null;
        if (!connected) {
            switch (request.getMessageType()) {
            case CONNECTION_REQUEST:
                ConnectionRequest conRequest = (ConnectionRequest) request;
                String username = conRequest.getUsername();
                if (conRequest.isConnection()) {
                    nextResponseBuffer = connectClient(username, address);
                    break;
                }
            default:
                RequestResponse errorResponse = new RequestResponse(
                        Status.EXCEPTION,
                        "Client must be connected before attempting any other action.");
                nextResponseBuffer = ProtocolMessage.toBytes(errorResponse);
                break;
            }
        } else {
            switch (request.getMessageType()) {
            case CONNECTION_REQUEST:
                ConnectionRequest conRequest = (ConnectionRequest) request;
                if (!conRequest.isConnection()) {
                    nextResponseBuffer = disconnectClient(address);
                } else {
                    // No need to tell the client that he is doing something
                    // which has no effect.
                    LOGGER.config("Received connection request from " +
                            address + " but user is already connected.");
                    RequestResponse errorResponse = new RequestResponse(
                            Status.SUCCESS);
                    nextResponseBuffer = ProtocolMessage.toBytes(errorResponse);
                }
                break;
            case QUEUE_MODIFICATION:
                QueueModificationRequest queueRequest = (QueueModificationRequest) request;
                String queueName = queueRequest.getQueueName();
                if (queueRequest.isDelete())
                    nextResponseBuffer = deleteQueue(queueName, address);
                else
                    nextResponseBuffer = createQueue(queueName, address);
                break;
            case SEND_MESSAGE:
                SendMessageRequest sendMessageRequest = (SendMessageRequest) request;
                nextResponseBuffer = sendMessage(sendMessageRequest, address);
                break;
            case RETRIEVE_MESSAGE:
                RetrieveMessageRequest retrieveMessageRequest = (RetrieveMessageRequest) request;
                nextResponseBuffer = retrieveMessage(retrieveMessageRequest,
                        address);
                break;
            case RETRIEVE_QUEUES:
                nextResponseBuffer = getQueues(address);
                break;
            default:
                LOGGER.severe("Received an invalid action request from " +
                        address);
                RequestResponse errorResponse = new RequestResponse(
                        Status.EXCEPTION, "Unexpected message type received.");
                nextResponseBuffer = ProtocolMessage.toBytes(errorResponse);
                break;
            }
        }
        if (nextResponseBuffer != null) {
            writeBuffer.addLast(nextResponseBuffer);
            return true;
        }
        return false;
    }

    /**
     * Process a connection request by the client, it is guaranteed that this is
     * called only if the user is not connected already from this
     * ClientConnection instance. If the user is not already connected from
     * another instance then the method will mark it as connected, creating it
     * first if necessary. Also the ClientConnection instance will be flagged as
     * connected.
     * 
     * @param username
     *            client to connect.
     * @param address
     *            remote address that originated the request.
     * @return buffer with the response to the request.
     */
    private ByteBuffer connectClient(String username, String address) {
        LOGGER.info("Received connection request from " + address +
                " for client " + username + ".");
        Connection conn = null;
        try {
            conn = dbConnectionDispatcher.retrieveDatabaseConnection();
            Client tmpClient = FetchClient.execute(username, conn);
            if (tmpClient == null) {
                int clientId = CreateClient.execute(username, true, conn);
                client = new Client(clientId, username, true);
                LOGGER.config("Created client " + username +
                        " in the database.");
            } else {
                if (tmpClient.isClientOnline()) {
                    RequestResponse failureResponse = new RequestResponse(
                            Status.USER_ONLINE);
                    LOGGER.info("Responded to connection request from " +
                            address +
                            " with failure since the specified client " +
                            "is already online.");
                    return ProtocolMessage.toBytes(failureResponse);
                } else {
                    client = tmpClient;
                    ChangeClientStatus.execute(username, true, conn);
                }
            }
            connected = true;
            LOGGER.info("Connected client " + username +
                    " successfully, request originated from " + address + ".");
            conn.commit();
            RequestResponse successResponse = new RequestResponse(
                    Status.SUCCESS);
            return ProtocolMessage.toBytes(successResponse);
        } catch (SQLException e) {
            LOGGER.severe("Caught exception while trying to connect client from " +
                    address + ".");
            LOGGER.log(Level.SEVERE, "", e);
            RequestResponse errorResponse = new RequestResponse(
                    Status.EXCEPTION, e.toString());
            if (conn != null)
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    logRollbackException(e1);
                }
            return ProtocolMessage.toBytes(errorResponse);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logCloseException(e);
                }
            }
        }
    }

    /**
     * Process a disconnection request by the client, it is guaranteed that this
     * is called only if the user is connected already. If the disconnection is
     * successful then the ClientConnection instance will be marked for
     * disconnection and will stop accepting requests.
     * 
     * @param address
     *            remote address that originated the request.
     * 
     * @return buffer with the response to the request.
     */
    private ByteBuffer disconnectClient(String address) {
        LOGGER.info("Received disconnection request from " + address +
                " for client " + client.getClientUsername() + ".");
        Connection conn = null;
        try {
            conn = dbConnectionDispatcher.retrieveDatabaseConnection();
            ChangeClientStatus.execute(client.getClientUsername(), false, conn);
            disconnectionRequested = true;
            connected = false;
            conn.commit();
            LOGGER.info("Succesfully disconnected client " +
                    client.getClientUsername() + " connected from " + address +
                    ".");
            RequestResponse successResponse = new RequestResponse(
                    Status.SUCCESS);
            return ProtocolMessage.toBytes(successResponse);
        } catch (SQLException e) {
            LOGGER.severe("Caught exception while trying to disconnect " +
                    "client from " + address + ".");
            LOGGER.log(Level.SEVERE, "", e);
            RequestResponse errorResponse = new RequestResponse(
                    Status.EXCEPTION, e.toString());
            if (conn != null)
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    logRollbackException(e1);
                }
            return ProtocolMessage.toBytes(errorResponse);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logCloseException(e);
                }
            }
        }
    }

    /**
     * Process a request to create a queue in the system.
     * 
     * @param queueName
     *            name of the queue to create.
     * @param address
     *            remote address that originated the request.
     * @return the buffer with the response.
     */
    private ByteBuffer createQueue(String queueName, String address) {
        LOGGER.info("Received request to create queue " + queueName + " from " +
                address + ".");
        Connection conn = null;
        try {
            conn = dbConnectionDispatcher.retrieveDatabaseConnection();
            CreateQueue.execute(queueName, conn);
            conn.commit();
            LOGGER.info("Queue " + queueName + " created for " + address + ".");
            RequestResponse successResponse = new RequestResponse(
                    Status.SUCCESS);
            return ProtocolMessage.toBytes(successResponse);
        } catch (SQLException e) {
            LOGGER.severe("Caught exception while trying to create queue " +
                    "for " + address + ".");
            LOGGER.log(Level.SEVERE, "", e);
            RequestResponse errorResponse = new RequestResponse(
                    Status.EXCEPTION, e.toString());
            if (conn != null)
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    logRollbackException(e1);
                }
            return ProtocolMessage.toBytes(errorResponse);
        } catch (QueueAlreadyExistsException e) {
            LOGGER.info("Responded with failure to create queue request from " +
                    address + " because queue " + queueName +
                    " already exists.");
            RequestResponse failureResponse = new RequestResponse(
                    Status.QUEUE_EXISTS);
            if (conn != null)
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    logRollbackException(e1);
                }
            return ProtocolMessage.toBytes(failureResponse);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logCloseException(e);
                }
            }
        }
    }

    /**
     * Process a request to delete a queue in the system.
     * 
     * @param queueName
     *            name of the queue to delete.
     * @param address
     *            remote address that originated the request.
     * @return the buffer with the response.
     */

    private ByteBuffer deleteQueue(String queueName, String address) {
        LOGGER.info("Received request to delete queue " + queueName + " from " +
                address + ".");
        Connection conn = null;
        try {
            conn = dbConnectionDispatcher.retrieveDatabaseConnection();
            DeleteQueue.execute(queueName, conn);
            conn.commit();
            LOGGER.info("Queue " + queueName + " deleted for " + address + ".");
            RequestResponse successResponse = new RequestResponse(
                    Status.SUCCESS);
            return ProtocolMessage.toBytes(successResponse);
        } catch (SQLException e) {
            LOGGER.severe("Caught exception while trying to create queue " +
                    "for " + address + ".");
            LOGGER.log(Level.SEVERE, "", e);
            RequestResponse errorResponse = new RequestResponse(
                    Status.EXCEPTION, e.toString());
            if (conn != null)
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    logRollbackException(e1);
                }
            return ProtocolMessage.toBytes(errorResponse);
        } catch (QueueNotEmptyException e) {
            LOGGER.info("Responded with failure to delete queue request from " +
                    address + " because queue " + queueName + " is not empty.");
            RequestResponse failureResponse = new RequestResponse(
                    Status.QUEUE_NOT_EMPTY);
            if (conn != null)
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    logRollbackException(e1);
                }
            return ProtocolMessage.toBytes(failureResponse);
        } catch (InexistentQueueException e) {
            LOGGER.info("Responded with failure to delete queue request from " +
                    address + " because queue " + queueName +
                    " does not exist.");
            RequestResponse failureResponse = new RequestResponse(
                    Status.QUEUE_NOT_EXISTS);
            if (conn != null)
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    logRollbackException(e1);
                }
            return ProtocolMessage.toBytes(failureResponse);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logCloseException(e);
                }
            }
        }
    }

    /**
     * Process a request to create a message in the system.
     * 
     * @param sendMessageRequest
     *            request to create a message.
     * @param address
     *            remote address that originated the request.
     * @return the buffer with the response.
     */
    private ByteBuffer sendMessage(SendMessageRequest sendMessageRequest,
            String address) {
        int queueListSize = 0;
        for (@SuppressWarnings("unused")
        String x : sendMessageRequest.getQueueList())
            ++queueListSize;
        LOGGER.info("Received request to create a message of " +
                sendMessageRequest.getMessage().length() + " chars, sent to " +
                queueListSize + " queues from " + address + ".");
        Connection conn = null;
        try {
            conn = dbConnectionDispatcher.retrieveDatabaseConnection();
            String receiver = sendMessageRequest.getReceiver();
            if (!sendMessageRequest.hasReceiver()) {
                CreateMessage.execute(client.getClientId(), sendMessageRequest
                        .getQueueList(), sendMessageRequest.getContext()
                        .getByteValue(), sendMessageRequest.getPriority(),
                        sendMessageRequest.getMessage(), conn);

            } else {
                CreateMessage.execute(client.getClientId(), receiver,
                        sendMessageRequest.getQueueList(), sendMessageRequest
                                .getContext().getByteValue(),
                        sendMessageRequest.getPriority(), sendMessageRequest
                                .getMessage(), conn);
            }
            conn.commit();
            LOGGER.info("Created message message of " +
                    sendMessageRequest.getMessage().length() +
                    " chars, sent to " + queueListSize + " queues from " +
                    address + ".");
            RequestResponse successResponse = new RequestResponse(
                    Status.SUCCESS);
            return ProtocolMessage.toBytes(successResponse);
        } catch (SQLException e) {
            LOGGER.severe("Caught exception while trying to send message from " +
                    address + ".");
            LOGGER.log(Level.SEVERE, "", e);
            RequestResponse errorResponse = new RequestResponse(
                    Status.EXCEPTION, e.toString());
            if (conn != null)
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    logRollbackException(e1);
                }
            return ProtocolMessage.toBytes(errorResponse);
        } catch (InexistentQueueException e) {
            LOGGER.info("Responded with failure to a send message request from " +
                    address + " because a queue does not exist.");
            RequestResponse failureResponse = new RequestResponse(
                    Status.QUEUE_NOT_EXISTS);
            if (conn != null)
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    logRollbackException(e1);
                }
            return ProtocolMessage.toBytes(failureResponse);
        } catch (CreateMessageException e) {
            LOGGER.severe("Responded with failure to a send message request from " +
                    address + " because of unknown circumstances");
            RequestResponse failureResponse = new RequestResponse(
                    Status.EXCEPTION, e.toString());
            if (conn != null)
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    logRollbackException(e1);
                }
            return ProtocolMessage.toBytes(failureResponse);
        } catch (InexistentClientException e) {
            LOGGER.info("Responded with failure to a send message request from " +
                    address + " because the receiver does not exist.");
            RequestResponse failureResponse = new RequestResponse(
                    Status.NO_CLIENT);
            if (conn != null)
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    logRollbackException(e1);
                }
            return ProtocolMessage.toBytes(failureResponse);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logCloseException(e);
                }
            }
        }
    }

    /**
     * Process a request for a message in the system.
     * 
     * @param retrieveMessageRequest
     *            request for a message.
     * @param address
     *            remote address that originated the request.
     * @return the buffer with the response.
     */
    private ByteBuffer retrieveMessage(
            RetrieveMessageRequest retrieveMessageRequest, String address) {
        LOGGER.info("Received request to retrieve a message by " +
                retrieveMessageRequest.getFilterType().toString() +
                " ordered by " +
                retrieveMessageRequest.getOrderBy().toString() + " from " +
                address + ".");
        Connection conn = null;
        try {
            conn = dbConnectionDispatcher.retrieveDatabaseConnection();
            boolean byQueue = retrieveMessageRequest.getFilterType() == Filter.QUEUE;
            boolean byPrio = retrieveMessageRequest.getOrderBy() == Order.PRIORITY;
            boolean isPop = retrieveMessageRequest.isPopMessage();
            Message msg = RetrieveMessage.execute(client.getClientId(),
                    retrieveMessageRequest.getFilterValue(), byPrio, byQueue,
                    conn);
            if (msg == null) {
                conn.commit();
                LOGGER.info("Responded with failure to a retrieve message request from " +
                        address + " because no message was found.");
                RequestResponse errorResponse = new RequestResponse(
                        Status.NO_MESSAGE);
                return ProtocolMessage.toBytes(errorResponse);
            } else {
                LOGGER.info("Found message " + msg.getId() + " filtered by " +
                        retrieveMessageRequest.getFilterType().toString() +
                        " ordered by " +
                        retrieveMessageRequest.getOrderBy().toString() +
                        " for " + address + ".");
                if (isPop) {
                    DeleteMessage.execute(msg.getId(), msg.getQueueName(), conn);
                    conn.commit();
                    LOGGER.info("Popped message " + msg.getId() + " from queue " + msg.getQueueName() +
                            " for " + address + ".");
                } else {
                    conn.commit();
                }
                RetrieveMessageResponse messageResponse = new RetrieveMessageResponse(
                        msg);
                return ProtocolMessage.toBytes(messageResponse);
            }
        } catch (SQLException e) {
            LOGGER.severe("Caught exception while trying to retrieve message for " +
                    address + ".");
            LOGGER.log(Level.SEVERE, "", e);
            RequestResponse errorResponse = new RequestResponse(
                    Status.EXCEPTION, e.toString());
            if (conn != null)
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    logRollbackException(e1);
                }
            return ProtocolMessage.toBytes(errorResponse);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logCloseException(e);
                }
            }
        }
    }

    /**
     * Process a request to retrieve queues where there are messages waiting for
     * a client.
     * 
     * @param address
     *            remote address that originated the request.
     * @return the buffer with the response.
     */
    private ByteBuffer getQueues(String address) {
        LOGGER.info("Received request to retrieve queues with pending messages for " +
                client.getClientUsername() + " from " + address + ".");
        Connection conn = null;
        try {
            conn = dbConnectionDispatcher.retrieveDatabaseConnection();
            ArrayList<String> result = GetQueuesWithMessages.execute(
                    client.getClientId(), conn);
            conn.commit();
            if(result.size() > 0){
                LOGGER.info("Found " + result.size() +
                        " queues with messages waiting for " + address + ".");
                GetQueuesResponse queueResponse = new GetQueuesResponse(result);
                return ProtocolMessage.toBytes(queueResponse);
            } else {
                LOGGER.info("Did not find any queues with messages waiting for " + address + ".");
                RequestResponse response = new RequestResponse(Status.NO_QUEUE);
                return ProtocolMessage.toBytes(response);
            }
        } catch (SQLException e) {
            RequestResponse errorResponse = new RequestResponse(
                    Status.EXCEPTION, e.toString());
            if (conn != null)
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    logRollbackException(e1);
                }
            return ProtocolMessage.toBytes(errorResponse);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logCloseException(e);
                }
            }
        }
    }

    /**
     * Utility method to standardize the logging of rollback exceptions.
     * 
     * @param e
     *            Rollback exception.
     */
    private void logRollbackException(SQLException e) {
        LOGGER.severe("Caught exception while trying to rollback.");
        LOGGER.log(Level.SEVERE, "", e);
    }

    /**
     * Utility method to standardize the logging of exceptions when closing a
     * connection.
     * 
     * @param e
     *            SQL exception.
     */
    private void logCloseException(SQLException e) {
        LOGGER.severe("Caught exception while trying to close the connection.");
        LOGGER.log(Level.SEVERE, "", e);
    }

}
