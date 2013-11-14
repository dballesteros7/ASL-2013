'''
Script that starts N Larrys, M Tolas, M Marujas, Z Carls and X Ricks.
These run for certain given time in a controlled fashion.

Created on Nov 9, 2013

@author: Diego Ballesteros (diegob)
'''

import ConfigParser
import sys
import time

from Load.FullTrace.ClientClient import ClientClient
from Load.FullTrace.OneWayClient import OneWayClient
from Load.FullTrace.PairSourceClient import PairSourceClient
from Load.FullTrace.PairTargetClient import PairTargetClient
from Load.FullTrace.ServerClient import ServerClient
from java.util.logging import Level
from org.ftab.logging.client import ClientLogger
from org.ftab.logging.client.filters import RetrieveFilter, \
    SendFilter, QueueRetrieveFilter
from org.ftab.logging.client.formatters import ClientDetailRTFormatter


def main():
    config = ConfigParser.RawConfigParser()
    config.readfp(open(sys.argv[1]))

    queueBaseName = config.get("System", "QueueBaseName")  # Base name of the queues
    availableQueues = int(config.get("System", "ValidQueues"))  # Number of valid queues in the system.
    serverA = config.get("System", "Server1")  # Server A
    serverB = config.get("System", "Server2")  # Server B
    portA = int(config.get("System", "Port1"))  # Port for server A
    portB = int(config.get("System", "Port2"))  # Port for server B

    runtime = int(config.get("Trace", "RunningTime"))  # Time that the clients are allowed to run for.
    larryNumber = int(config.get("Trace", "LarryNumber"))  # Number of clients to spawn.
    tolaMarujaNumber = int(config.get("Trace", "TolaMarujaNumber"))
    carlNumber = int(config.get("Trace", "CarlNumber"))
    rickNumber = int(config.get("Trace", "RickNumber"))
    messageSize = int(config.get("Trace", "MessageSize"))
    logPathRetrieve = config.get("Trace", "LogPathRetrieve")  # Path for the log output
    logPathSend = config.get("Trace", "LogPathSend")
    logPathFindQueue = config.get("Trace", "LogPathFindQueue")

    ClientLogger.addLogStream(logPathRetrieve, RetrieveFilter(), ClientDetailRTFormatter())
    ClientLogger.addLogStream(logPathSend, SendFilter(), ClientDetailRTFormatter())
    ClientLogger.addLogStream(logPathFindQueue, QueueRetrieveFilter(), ClientDetailRTFormatter())
    ClientLogger.setLevel(Level.ALL)

    possibleQueues = []
    for i in xrange(availableQueues):
        queue = "%s%d" % (queueBaseName, i)
        possibleQueues.append(queue)
    clients = []
    for i in xrange(larryNumber):
        client = OneWayClient('Larry%d' % i, possibleQueues[:-2], larryNumber)
        client.setup(serverA, portA)
        clients.append(client)
    for i in xrange(tolaMarujaNumber):
        clientA = PairSourceClient('Tola%d' % i, messageSize, possibleQueues[-2], 'Maruja%d' % i)
        clientB = PairTargetClient('Maruja%d' % i, possibleQueues[-2], 'Tola%d' % i)
        clientA.setup(serverB, portB)
        clientB.setup(serverB, portB)
        clients.append(clientA)
        clients.append(clientB)

    for i in xrange(rickNumber):
        client = ServerClient('Rick%d' % i, possibleQueues[-1])
        client.setup(serverB, portB)
        clients.append(client)
    for i in xrange(carlNumber):
        client = ClientClient('Carl%d' % i, messageSize, possibleQueues[-1])
        client.setup(serverB, portB)
        clients.append(client)

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
        print 'Usage: jython %s <trace_config_file>' % sys.argv[0]
        sys.exit(0)
    sys.exit(main())
