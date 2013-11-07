'''
This module defines a type of load generator known as a one-way bounce client.

Created on Oct 22, 2013

@author: Diego Ballesteros (diegob)
'''

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
import time
import os
import threading

from org.ftab.communication.requests.SendMessageRequest import Context
from org.ftab.client import Client

class OneWayClient(threading.Thread):

    def __init__(self, clientName, possibleQueues, brothers, logPath):
        threading.Thread.__init__(self)
        self.running = False
        self.queues = possibleQueues
        self.name = clientName
        self.clientInstance = Client(self.name)
        self.logFile = open(os.path.join(logPath, "%s.log" % self.name), 'w')
        self.brothers = brothers
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
        msg = self.buildInitialMessage()
        while(self.running):
            self.sendMessageToRandomReceiver(msg)
            msg = self.getNextMessage()
        return

    def disconnect(self):
        start = time.time()
        self.clientInstance.Disconnect()
        end = time.time()
        self.logFile.write("DISCONNECT %s %s\n" % (start, end))
        self.logFile.close()
        return

    def buildInitialMessage(self):
        msg = "1 %s\n" % self.name
        return msg

    def sendMessageToRandomReceiver(self, msg):
        receiver = random.randint(0, self.brothers - 1)  # self.brothers includes myself
        receiver = "Larry%d" % receiver
        queueToSend = random.choice(self.queues)
        context = Context.valueOf("NONE");
        start = time.time()
        result = self.clientInstance.SendMessage(msg, 5, context, [queueToSend], receiver)
        end = time.time()
        if result:
            self.logFile.write("SEND %s %s\n" % (start, end))
        else:
            self.logFile.write("ERROR_SEND %s %s\n" % (start, end))
        return

    def getNextMessage(self):
        found = False;
        while not found and self.running:
            start = time.time()
            result = self.clientInstance.GetWaitingQueues()
            end = time.time()
            if result:
                self.logFile.write("QUEUE_FOUND %s %s\n" % (start, end))
                found = True
            else:
                self.logFile.write("NO_QUEUE %s %s\n" % (start, end))
        if found:
            start = time.time()
            msg = self.clientInstance.ViewMessageFromQueue(result[0], True)
            end = time.time()
            if msg:
                self.logFile.write("READ %s %s\n" % (start, end))
                content = msg.getContent()
                lines = content.split("\n")
                lastLine = lines[-2]
                counter = int(lastLine.split(" ")[0])
                newMessage = ""
                done = False
                i = 0
                newLine = "%d %s\n" % (counter + 1, self.name)
                while not done and i < (len(lines) - 1):
                    if(len(newMessage) + len(lines[i]) + 1 > (2000 - len(newLine))):
                        done = True
                    else:
                        newMessage += "%s\n" % lines[i]
                    i += 1
                newMessage += newLine
                return newMessage
            else:
                raise Exception("Unexpected condition where a message was not found")

if __name__ == '__main__':
    pass
