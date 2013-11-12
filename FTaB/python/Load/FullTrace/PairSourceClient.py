'''
The PairSourceClient class implements a client that behaves as follows:

1. Once started, the client will run until it is signaled to stop by changing
its running flag to False. 
2. During execution, the client will send a message to a given queue with a certain receiver continuously.

PairSourceClient are also known as Tola, and their username in the system is
always formatted as Tola%d where %d is an integer counter.
'''

import random
import os
import threading
import linecache

from org.ftab.client import Client

class PairSourceClient(threading.Thread):

    def __init__(self, clientName, msgSize, queue, buddyName):
        threading.Thread.__init__(self)
        self.running = False
        self.queue = queue
        self.name = clientName
        self.clientInstance = Client(self.name)
        self.msgSize = msgSize
        self.buddy = buddyName
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
            self.retrieveResponse()
        return

    def disconnect(self):
        self.clientInstance.Disconnect()
        return

    def buildMessage(self):
        textBasePath = os.path.join(__file__, "../..", "Metamorphosis.txt")
        txt = "%s\n" % self.name
        while(len(txt) < self.msgSize):
            randomLine = random.randint(1, 1900)
            txt += linecache.getline(textBasePath, randomLine)
        return txt[:self.msgSize]

    def sendMessage(self, msg):
        context = 0
        self.clientInstance.SendMessage(msg, random.randint(1, 10), context, self.buddy, [self.queue])
        return

    def retrieveResponse(self):
        request = None
        while request is None and self.running:
            request = self.clientInstance.ViewMessageFromQueue(self.queue, True)
