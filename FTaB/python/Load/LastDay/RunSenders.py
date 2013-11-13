'''
Script that starts N parallel senders and splits them evenly between two servers.
The senders run for a given time T sending messages to X queues chosen at random
from a list of valid queues. After the time is over, the clients disconnect and
the program finishes.

Created on Nov 9, 2013

@author: Diego Ballesteros (diegob)
'''

import ConfigParser
import sys
import time

from Load.BroadcastClient import BroadcastClient
from org.ftab.logging.client import ClientLogger
from org.ftab.logging.client.filters import SendRetrieveFilter
from org.ftab.logging.client.formatters import ClientDetailRTFormatter
from java.util.logging import Level 

def main():
    config = ConfigParser.RawConfigParser()
    config.readfp(open(sys.argv[1]))

    queueBaseName = config.get("System", "QueueBaseName") # Base name of the queues
    availableQueues = int(config.get("System", "ValidQueues")) # Number of valid queues in the system.
    serverA = config.get("System", "Server") # Server A
    portA = int(config.get("System", "Port")) # Port for server A
    
    runtime = int(config.get("Trace", "RunningTime")) # Time that the clients are allowed to run for.
    senderNumber = int(config.get("Trace", "SenderNumber")) # Number of clients to spawn.
    msgSize = 2000
    queuesNumber = 1
    logPath = config.get("Trace", "LogPath") # Path for the log output
    
    ClientLogger.addLogStream(logPath, SendRetrieveFilter(), ClientDetailRTFormatter())
    ClientLogger.setLevel(Level.ALL)
    
    possibleQueues = []
    for i in xrange(availableQueues):
        queue = "%s%d" % (queueBaseName, i)
        possibleQueues.append(queue)
    clients = []
    for i in xrange(senderNumber):
        client = BroadcastClient("Alice%d" % i, msgSize, possibleQueues, queuesNumber)
        client.setup(serverA, portA)
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
