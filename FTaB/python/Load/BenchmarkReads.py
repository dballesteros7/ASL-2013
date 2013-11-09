'''
Setup a small local test to benchmark the speed of reading from the database under multiple concurrent connections.

Created on Nov 11, 2013

@author: Diego Ballesteros (diegob)
'''

import ConfigParser
import sys
import time

from Load.BroadcastClient import BroadcastClient
from Load.ReaderClient import ReaderClient

def main():
    config = ConfigParser.RawConfigParser()
    config.readfp(open(sys.argv[1]))
    queueBaseName = config.get("system", "queueName")
    availableQueues = int(config.get("system", "queueNumber"))
    logPath = config.get("trace", "logPath")
    serverA = config.get("system", "server1")
    serverB = config.get("system", "server2")
    portA = int(config.get("system", "port1"))
    portB = int(config.get("system", "port2"))
    traceTime = int(config.get("trace", "runningTime"))
    clientNumber = int(config.get("trace", "clients"))    
    possibleQueues = []
    for i in xrange(availableQueues):
        queue = "%s%d" % (queueBaseName, i)
        possibleQueues.append(queue)
    clients = []
    for i in xrange(clientNumber):
        client = BroadcastClient("Alice-6-%d" % i, 2000, possibleQueues, logPath)
        client.setup(serverA, portA)
        clients.append(client)
    for i in xrange(clientNumber):
        client = ReaderClient("Bob-6-%d" % i, possibleQueues, logPath)
        client.setup(serverB, portB)
        clients.append(client)
    for client in clients:
        client.start()

    startTime = time.time()
    endTime = time.time()
    while endTime < startTime + traceTime:
        endTime = time.time()

    for client in clients:
        client.running = False
    for client in clients:
        client.join()
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
