'''
The ServerClient class implements a client that behaves as follows:

1. Once started, the client will run until it is signaled to stop by changing
its running flag to False. 
2. During execution, the client will check for the top priority message in a fixed queue and pop it.

ServerClients are also known as Rick, and their username in the system is
always formatted as Rick%d where %d is an integer counter.
'''

import threading

from org.ftab.client import Client

class ServerClient(threading.Thread):

    def __init__(self, clientName, serviceQueue):
        threading.Thread.__init__(self)
        self.running = False
        self.queue = serviceQueue
        self.name = clientName
        self.clientInstance = Client(self.name)
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
        self.clientInstance.ViewMessageFromQueue(self.queue, True)
