/**
 * UserClient.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.database;

/**
 * Class that represents a client, i.e. a component in the first tier of the
 * messaging system. This contains the information about the client that is
 * stored in the database.
 */
public class Client {

    /**
     * Unique identifier of the client in the database
     */
    private final int clientId;

    /**
     * Username for the client
     */
    private final String username;

    /**
     * Value that indicates if the user is connected to a server or not
     */
    private final boolean online;

    public Client(final int nClientId, final String nUsername,
            final boolean nOnline) {
        clientId = nClientId;
        username = nUsername;
        online = nOnline;
    }

    /**
     * Get the client's id in the database
     * 
     * @return client's id in the database
     */
    public int getClientId() {
        return clientId;
    }

    /**
     * Get the client's username
     * 
     * @return client's username
     */
    public String getClientUsername() {
        return username;
    }

    /**
     * Indicates if the user is currently online
     * 
     * @return true if the user is online, false otherwise
     */
    public boolean isClientOnline() {
        return online;
    }

}
