'''
This module defines a type of load generator known as a one-way broadcast client.

The BroadcastClient class implements a client that behaves as follows:

1. Once started, the client will run until it is signaled to stop by changing
its running flag to False. 
2. During execution, the client will send a message with it's name in the first line and a random english text in the second line. 
The message size can be configured before starting the client. The sending operation happens as soon as the last message was sent.
3. The message is sent to a random sample of queues from a given initial list and given a fixed size, additionally the priority is
   an uniformly distributed integer between 1 and 10.
BroadcastClients are also known as Alice, and their username in the system is
always formatted as Alice%d where %d is an integer counter.


Created on Oct 27, 2013

@author: Diego Ballesteros (diegob)
'''

import random
import os
import threading
import linecache

from org.ftab.client import Client

class BroadcastClient(threading.Thread):

    def __init__(self, clientName, msgSize, possibleQueues, queuesNumber):
        threading.Thread.__init__(self)
        self.running = False
        self.queues = possibleQueues
        self.name = clientName
        self.clientInstance = Client(self.name)
        self.msgSize = msgSize
        self.kQueues = queuesNumber
        return

    def setup(self, ipAddress, port):
        result = self.clientInstance.Connect(ipAddress, port)
        if not result:
            raise Exception("Failed to connect to server")
        return

    def run(self):
        self.running = True
        msg = self.buildMessage()
        while(self.running):
            self.sendMessage(msg)
        return

    def disconnect(self):
        self.clientInstance.Disconnect()
        return

    def buildMessage(self):
        textBasePath = os.path.join(__file__, "..", "Metamorphosis.txt")
        txt = "%s\n" % self.name
        while(len(txt) < self.msgSize):
            randomLine = random.randint(1, 1900)
            txt += linecache.getline(textBasePath, randomLine)
        return txt[:self.msgSize]

    def sendMessage(self, msg):
        context = 0;
        self.clientInstance.SendMessage(msg, random.randint(1, 10), context, random.sample(self.queues, self.kQueues))
        return
