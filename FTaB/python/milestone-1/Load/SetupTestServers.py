'''
Setup a running trace with:
 
n BroadcastClients (a.k.a Alice)
n ReaderClients (a.k.a Bob)

Created on Oct 27, 2013

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
    maxClients = int(config.get("trace", "clients"))
    logPath = config.get("trace", "logPath")
    serverA = config.get("system", "server1")
    serverB = config.get("system", "server2")
    port = int(config.get("system", "port"))
    traceTime = int(config.get("trace", "runningTime"))

    possibleQueues = []
    for i in xrange(availableQueues):
        queue = "%s%d" % (queueBaseName, i)
        possibleQueues.append(queue)

    clients = []
    for i in xrange(maxClients):
        if(i % 2 == 0):
            client = BroadcastClient("Alice%d" % i, 2000, possibleQueues, logPath)
            if(i % 4 == 0):
                client.setup(serverA, port)
            else:
                client.setup(serverB, port)
            clients.append(client)
        else:
            client = ReaderClient("Bob%d" % i, possibleQueues, logPath)
            if(i % 4 == 1):
                client.setup(serverA, port)
            else:
                client.setup(serverB, port)
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

if __name__ == '__main__':
    if(len(sys.argv) < 2):
        sys.exit(1)
    if(len(sys.argv) == 2 and sys.argv[1] == 'help'):
        print 'Usage: jython %s <trace_config_file>' % sys.argv[0]
        sys.exit(0)
    sys.exit(main())
