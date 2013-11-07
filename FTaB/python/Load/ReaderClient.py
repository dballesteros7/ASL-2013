'''
This module defines a type of load generator known as a one-way bounce client.

Created on Oct 27, 2013

@author: Diego Ballesteros (diegob)
'''

'''
The ReaderClient class implements a client that behaves as follows:

1. Once started, the client will run until it is signaled to stop by changing
its running flag to False. 
2. During execution, the client will read the top priority message from a random queue and pop it. 
This will continue, until execution stops.

OneWayClients are also known as Bob, and their username in the system is
always formatted as Bob%d where %d is an integer counter.
'''

import random
import time
import os
import threading

from org.ftab.client import Client

class ReaderClient(threading.Thread):

    def __init__(self, clientName, possibleQueues, logPath):
        threading.Thread.__init__(self)
        self.running = False
        self.queues = possibleQueues
        self.name = clientName
        self.clientInstance = Client(self.name)
        self.logFile = open(os.path.join(logPath, "%s.log" % self.name), 'a')
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
        while(self.running):
            self.getNextMessage()
        return

    def disconnect(self):
        start = time.time()
        self.clientInstance.Disconnect()
        end = time.time()
        self.logFile.write("DISCONNECT %s %s\n" % (start, end))
        self.logFile.close()
        return

    def getNextMessage(self):
        start = time.time()
        msg = self.clientInstance.ViewMessageFromQueue(random.choice(self.queues), True)
        end = time.time()
        if msg:
            self.logFile.write("READ %s %s\n" % (start, end))
        else:
            self.logFile.write("MISS %s %s\n" % (start, end))