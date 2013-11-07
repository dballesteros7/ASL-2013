'''
This module defines a type of load generator known as a one-way broadcast client.

Created on Oct 27, 2013

@author: Diego Ballesteros (diegob)
'''

'''
The BroadcastClient class implements a client that behaves as follows:

1. Once started, the client will run until it is signaled to stop by changing
its running flag to False. 
2. During execution, the client will send a message with it's name in the first line and a random english text in the second line. 
The message size can be configured before starting the client. The sending operation happens as soon as the last message was sent.

BroadcastClients are also known as Alice, and their username in the system is
always formatted as Alice%d where %d is an integer counter.
'''

import random
import time
import os
import threading
import linecache

from org.ftab.communication.requests.SendMessageRequest import Context
from org.ftab.client import Client

class BroadcastClient(threading.Thread):

    def __init__(self, clientName, msgSize, possibleQueues, logPath):
        threading.Thread.__init__(self)
        self.running = False
        self.queues = possibleQueues
        self.name = clientName
        self.clientInstance = Client(self.name)
        self.logFile = open(os.path.join(logPath, "%s.log" % self.name), 'a')
        self.msgSize = msgSize
        return

    def setup(self, ipAddress, port):
        start = time.time()
        result = self.clientInstance.Connect(ipAddress, port)
        end = time.time()
        if result:
            self.logFile.write("CONNECT %s %s\n" % (start, end))
        else:
            raise Exception("Failed to connect to server")
        return

    def run(self):
        self.running = True
        msg = self.buildMessage()
        while(self.running):
            self.sendMessage(msg)
        return

    def disconnect(self):
        start = time.time()
        self.clientInstance.Disconnect()
        end = time.time()
        self.logFile.write("DISCONNECT %s %s\n" % (start, end))
        self.logFile.close()
        return

    def buildMessage(self):
        textBasePath = os.path.join(__file__,"..", "Metamorphosis.txt")
        txt = "%s\n" % self.name
        while(len(txt) < self.msgSize):
            randomLine = random.randint(1, 1900)
            txt += linecache.getline(textBasePath, randomLine)
        return txt[:self.msgSize]

    def sendMessage(self, msg):
        queueToSend = random.choice(self.queues)
        context = Context.valueOf("NONE");
        start = time.time()
        result = self.clientInstance.SendMessage(msg, 5, context, [queueToSend])
        end = time.time()
        if result:
            self.logFile.write("SEND %s %s\n" % (start, end))
        else:
            self.logFile.write("ERROR_SEND %s %s\n" % (start, end))
        return