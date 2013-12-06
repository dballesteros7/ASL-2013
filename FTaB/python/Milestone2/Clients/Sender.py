'''
Sender module, it defines the sender class.

Created on Dec 5, 2013

@author: diegob
'''

import random
import threading
import time

from Milestone2 import Constants
from org.ftab.client import Client


class Sender(threading.Thread):
    '''
    This thread-class runs a client that sends
    messages non-stop until signaled with a wait time between messages
    distributed as some random variable.
    '''

    def __init__(self, clientName, possibleQueues, distribution):
        '''
        Initialization for the class.
        '''
        threading.Thread.__init__(self)
        self.clientName = clientName  # Name of the client in the system
        self.running = False  # Indicates if the sender is running
        self.clientInstance = Client(self.clientName)  # Creates a java client object
        self.queues = possibleQueues  # Records the queues to which the client can send messages
        self.distribution = distribution()  # Wait periods generator

    def connect(self, serverAddress, port):
        '''
        Connect to the given remote server
        '''
        self.clientInstance.Connect(serverAddress, port)
        self.distribution.seed("%s%s" % (self.clientName, time.time()))
    
    def sendMessage(self):
        '''
        Sends a message to a random queue without an specific receiver
        and with a predefined priority.
        '''
        self.clientInstance.SendMessage(Constants.TEXT_MESSAGE, Constants.PRIORITY, 0, random.sample(self.queues, 1))
        
    def disconnect(self):
        '''
        Disconnects from the server
        '''
        self.clientInstance.Disconnect()

    def run(self):
        '''
        Run a sender instance, it sends messages with a randomly
        distributed inter-message time until signaled to stop by an
        external thread.
        '''
        self.running = True
        while(self.running):
            self.sendMessage()
            self.distribution.wait()
        return
