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
import threading

from org.ftab.client import Client

class ReaderClient(threading.Thread):

    def __init__(self, clientName, possibleQueues, pop = True):
        threading.Thread.__init__(self)
        self.running = False
        self.queues = possibleQueues
        self.name = clientName
        self.clientInstance = Client(self.name)
        self.pop = pop
        return

    def setup(self, ipAddress, port):
        result = self.clientInstance.Connect(ipAddress, port)
        if not result:
            raise Exception("Failed to connect to server")
        return

    def run(self):
        self.running = True
        while(self.running):
            self.getNextMessage()
        return

    def disconnect(self):
        self.clientInstance.Disconnect()
        return

    def getNextMessage(self):
        msg = self.clientInstance.ViewMessageFromQueue(random.choice(self.queues), self.pop)