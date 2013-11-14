'''
Script that starts N parallel receivers and splits them evenly between two servers.
The receivers run for a given time T reading from a random queue. 
After the time is over, the clients disconnect and the program finishes.

Created on Nov 9, 2013

@author: Diego Ballesteros (diegob)
'''

import ConfigParser
from java.util.logging import Level
import sys
import time

from Load.ReaderClient import ReaderClient
from org.ftab.logging.client import ClientLogger
from org.ftab.logging.client.filters import SendRetrieveFilter
from org.ftab.logging.client.formatters import ClientDetailRTFormatter


def main():
    config = ConfigParser.RawConfigParser()
    config.readfp(open(sys.argv[1]))

    queueBaseName = config.get("System", "QueueBaseName") # Base name of the queues
    availableQueues = int(config.get("System", "ValidQueues")) # Number of valid queues in the system.
    serverA = config.get("System", "ServerA") # Server A
    serverB = config.get("System", "ServerB") # Server A
    serverC = config.get("System", "ServerC") # Server A
    serverD = config.get("System", "ServerD") # Server A
    serverE = config.get("System", "ServerE") # Server A
    port = int(config.get("System", "Port")) # Port for servers
    
    runtime = int(config.get("Trace", "RunningTime")) # Time that the clients are allowed to run for.
    readerNumber = int(config.get("Trace", "ReaderNumber")) # Number of clients to spawn.
    readerOffset = int(config.get("Trace", "ReaderOffset"))
    logPath = config.get("Trace", "LogPath") # Path for the log output
    
    ClientLogger.addLogStream(logPath, SendRetrieveFilter(), ClientDetailRTFormatter())
    ClientLogger.setLevel(Level.ALL)
    
    possibleQueues = []
    for i in xrange(availableQueues):
        queue = "%s%d" % (queueBaseName, i)
        possibleQueues.append(queue)
    clients = []
    for i in xrange(readerNumber):
        client = ReaderClient('Bob%d' % (i + readerOffset), possibleQueues)
        if(i + readerOffset) % 5 == 0:
            client.setup(serverA, port)
        elif (i + readerOffset) % 5 == 1:
            client.setup(serverB, port)
        elif (i + readerOffset) % 5 == 2:
            client.setup(serverC, port)
        elif (i + readerOffset) % 5 == 3:
            client.setup(serverD, port)
        elif (i + readerOffset) % 5 == 4:
            client.setup(serverE, port)
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
