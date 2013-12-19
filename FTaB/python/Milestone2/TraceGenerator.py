'''
Trace generator for milestone 2 experiments.

Created on Dec 5, 2013

@author: Diego Ballesteros (diegob)
'''

import ConfigParser
from java.util.logging import Level
import sys
import time

from org.ftab.logging.client import ClientLogger
from org.ftab.logging.client.filters import SendRetrieveFilter
from org.ftab.logging.client.formatters import ClientDetailRTFormatter
from Milestone2.Clients.Reader import Reader
from Milestone2.Clients.Sender import Sender

def main():
    config = ConfigParser.RawConfigParser()
    config.readfp(open(sys.argv[1]))

    queueBaseName = config.get("system", "queuebasename") # Base name of the queues
    availableQueues = int(config.get("system", "validqueues")) # Number of valid queues in the system.
    serverA = config.get("system", "servera") # Server A
    portA = int(config.get("system", "porta")) # Port for server A
    serverB = config.get("system", "serverb") # Server B
    portB = int(config.get("system", "portb")) # Port for server B
    serverC = config.get("system", "serverc") # Server C
    portC = int(config.get("system", "portc")) # Port for server C
    serverD = config.get("system", "serverd") # Server D
    portD = int(config.get("system", "portd")) # Port for server D
    serverE = config.get("system", "servere") # Server E
    portE = int(config.get("system", "porte")) # Port for server E
    servers = int(config.get("system", "numberofservers")) # Number of servers to use (2-5)

    runtime = int(config.get("trace", "runningtime")) # Time that the clients are allowed to run for.
    senderNumber = int(config.get("trace", "sendernumber")) # Number of senders to spawn.
    readerNumber = int(config.get("trace", "readernumber")) # Number of readers to spawn.
    logPath = config.get("trace", "logpath") # Path for the log output
    distributionName = config.get("trace", "distribution")
    distributionMod = __import__('Milestone2.DistributionGenerator.%s' % distributionName, globals(), locals(), [distributionName], 0)
    distributionClass = getattr(distributionMod, distributionName)

    ClientLogger.addLogStream(logPath, SendRetrieveFilter(), ClientDetailRTFormatter())
    ClientLogger.setLevel(Level.ALL)
    
    possibleQueues = []
    for i in xrange(availableQueues):
        queue = "%s%d" % (queueBaseName, i)
        possibleQueues.append(queue)
    clients = []
    
    assignedReaders = 0
    assignedSenders = 0
    while assignedReaders < readerNumber or assignedSenders < senderNumber:
        if assignedReaders < readerNumber:
            client = Reader('Bob%d' % assignedReaders, possibleQueues, distributionClass)
            if (servers == 2 and assignedReaders % 2 == 0) or (servers == 5 and assignedReaders % 5 == 0):
                client.connect(serverA, portA)
            elif (servers == 2 and assignedReaders % 2 == 1) or (servers == 5 and assignedReaders % 5 == 1):
                client.connect(serverB, portB)
            elif (servers == 5 and assignedReaders % 5 == 2):
                client.connect(serverC, portC)
            elif (servers == 5 and assignedReaders % 5 == 3):
                client.connect(serverD, portD)
            else:
                client.connect(serverE, portE)
            clients.append(client)
            assignedReaders += 1
        if assignedSenders < senderNumber:
            client = Sender('Alice%d' % assignedSenders, possibleQueues, distributionClass)
            if (servers == 2 and assignedSenders % 2 == 0) or (servers == 5 and assignedSenders % 5 == 0):
                client.connect(serverA, portA)
            elif (servers == 2 and assignedSenders % 2 == 1) or (servers == 5 and assignedSenders % 5 == 1):
                client.connect(serverB, portB)
            elif (servers == 5 and assignedSenders % 5 == 2):
                client.connect(serverC, portC)
            elif (servers == 5 and assignedSenders % 5 == 3):
                client.connect(serverD, portD)
            else:
                client.connect(serverE, portE)
            clients.append(client)
            assignedSenders += 1

    for client in clients:
        client.start()

    startTime = time.time()
    endTime = time.time()
    while endTime < startTime + runtime:
        endTime = time.time()

    for client in clients:
        client.running = False
    print "Joining..."
    for client in clients:
        client.join()
    print "Disconnecting..."
    for client in clients:
        client.disconnect()

    return 0

if __name__ == '__main__':
    if(len(sys.argv) < 2):
        sys.exit(1)
    if(len(sys.argv) == 2 and sys.argv[1] == 'help'):
        print 'Usage: jython %s trace.ini' % sys.argv[0]
        sys.exit(0)
    sys.exit(main())
