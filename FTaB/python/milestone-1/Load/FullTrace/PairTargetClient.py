'''
The ServerClient class implements a client that behaves as follows:

1. Once started, the client will run until it is signaled to stop by changing
its running flag to False. 
2. During execution, the client will check for the top priority message in a fixed queue and pop it.
   Note that this is meant to work in conjunction with the PairSourceClient in such a way that this client only
   will find messages addressed specifically to him.
   
ServerClients are also known as Maruja, and their username in the system is
always formatted as Maruja%d where %d is an integer counter.
'''

import threading

from org.ftab.client import Client


class PairTargetClient(threading.Thread):

    DEFAULT_RESPONSE = "Request acknowledged."

    def __init__(self, clientName, serviceQueue, buddyName):
        threading.Thread.__init__(self)
        self.running = False
        self.queue = serviceQueue
        self.name = clientName
        self.clientInstance = Client(self.name)
        self.buddy = buddyName
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
            self.sendResponse()
        return

    def disconnect(self):
        self.clientInstance.Disconnect()
        return

    def getNextMessage(self):
        request = None
        while request is None and self.running:
            request = self.clientInstance.ViewMessageFromQueue(self.queue, True)

    def sendResponse(self):
        context = 0
        self.clientInstance.SendMessage(self.DEFAULT_RESPONSE, 10, context, self.buddy, [self.queue])
        return
