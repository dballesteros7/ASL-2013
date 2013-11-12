'''
The OneWayClient class implements a client that behaves as follows:

1. Once started, the client will run until it is signaled to stop by changing
its running flag to False. 
2. During execution, the client will first send an initial message to another 
random OneWayClient in the system with a counter and
its name in a single line. 
3. After the initial message, the client will wait for a message addressed to 
him to appear in any of the queues, and it will read this message and append a 
line with its name and an increasing counter. 
4. This message will be sent to another random OneWayClient and the cycle 
resets.

OneWayClients are also known as Larry, and their username in the system is
always formatted as Larry%d where %d is an integer counter.
'''

import random
import threading

from org.ftab.client import Client

class OneWayClient(threading.Thread):

    def __init__(self, clientName, possibleQueues, brothers):
        threading.Thread.__init__(self)
        self.running = False
        self.queues = possibleQueues
        self.name = clientName
        self.clientInstance = Client(self.name)
        self.brothers = brothers
        return

    def setup(self, ipAddress, port):
        result = self.clientInstance.Connect(ipAddress, port)
        if not result:
            raise Exception("Failed to connect to server")
        return

    def run(self):
        self.running = True
        msg = self.buildInitialMessage()
        while(self.running):
            self.sendMessageToRandomReceiver(msg)
            msg = self.getNextMessage()
        return

    def disconnect(self):
        self.clientInstance.Disconnect()
        return

    def buildInitialMessage(self):
        msg = "1 %s\n" % self.name
        return msg

    def sendMessageToRandomReceiver(self, msg):
        receiver = random.randint(0, self.brothers - 1)
        receiver = "Larry%d" % receiver
        queueToSend = random.choice(self.queues)
        context = 0;
        self.clientInstance.SendMessage(msg, 5, context, [queueToSend], receiver)
        return

    def getNextMessage(self):
        found = False
        result = []
        while not found and self.running:
            result = self.clientInstance.GetWaitingQueues()
            if result:
                found = True
        if found:
            msg = self.clientInstance.ViewMessageFromQueue(result[0], True)
            if msg:
                content = msg.getContent()
                lines = content.split("\n")
                lastLine = lines[-2]
                counter = int(lastLine.split(" ")[0])
                newLine = "%d %s\n" % (counter + 1, self.name)
                newMessage = content + newLine
                return newMessage[-2000:]
            else:
                raise Exception("Unexpected condition where a message was not found")
