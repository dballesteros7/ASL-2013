'''
The ClientClient class implements a client that behaves as follows:

1. Once started, the client will run until it is signaled to stop by changing
its running flag to False. 
2. During execution, the client will send a message to a given queue continuously.

ClientClients are also known as Carl, and their username in the system is
always formatted as Carl%d where %d is an integer counter.
'''

import random
import os
import threading
import linecache

from org.ftab.client import Client

class ClientClient(threading.Thread):

    def __init__(self, clientName, msgSize, queue):
        threading.Thread.__init__(self)
        self.running = False
        self.queue = queue
        self.name = clientName
        self.clientInstance = Client(self.name)
        self.msgSize = msgSize
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
        textBasePath = os.path.join(__file__, "../..", "Metamorphosis.txt")
        txt = "%s\n" % self.name
        while(len(txt) < self.msgSize):
            randomLine = random.randint(1, 1900)
            txt += linecache.getline(textBasePath, randomLine)
        return txt[:self.msgSize]

    def sendMessage(self, msg):
        context = int(self.name[4:])
        self.clientInstance.SendMessage(msg, random.randint(1, 10), context, [self.queue])
        return
