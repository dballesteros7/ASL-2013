'''
Setup a running trace where the number of both reading and writing clients
increase every 10 min, it lasts for 2h until we have 240 clients in the
system.

Created on Oct 27, 2013

@author: Diego Ballesteros (diegob)
'''

import ConfigParser
import sys
import time

from Load.BroadcastClient import BroadcastClient
from Load.ReaderClient import ReaderClient

from Deploy.DatabaseDeploy import main as databaseDeploy
from Scenarios.InitializedSystemNoMessages import main as deployQueues
from Scenarios.Prepare1MMessageDatabase import main as stuffDatabase

def main():
    config = ConfigParser.RawConfigParser()
    config.readfp(open(sys.argv[1]))
    queueBaseName = config.get("system", "queueName")
    availableQueues = int(config.get("system", "queueNumber"))
    logPath = config.get("trace", "logPath")
    serverA = config.get("system", "server1")
    serverB = config.get("system", "server2")
    port = int(config.get("system", "port"))
    traceTime = int(config.get("trace", "runningTime"))
    stepTime = int(config.get("trace", "stepTime"))
    stepSize = int(config.get("trace", "stepSize"))
    clientsZero = int(config.get("trace", "initialClients"))
    iterations = traceTime/stepTime
    possibleQueues = []
    for i in xrange(availableQueues):
        queue = "%s%d" % (queueBaseName, i)
        possibleQueues.append(queue)
    for z in xrange(iterations):
        clients = []
        for i in xrange(clientsZero + z*stepSize):
            client = BroadcastClient("Alice%d" % i, 2000, possibleQueues, logPath)
            if(i % 2 == 0):
                client.setup(serverA, port)
            else:
                client.setup(serverB, port)
            clients.append(client)
        for i in xrange(clientsZero + z*stepSize):
            client = ReaderClient("Bob%d" % i, possibleQueues, logPath)
            if(i % 2 == 0):
                client.setup(serverA, port)
            else:
                client.setup(serverB, port)
            clients.append(client)
        for client in clients:
            client.start()
    
        startTime = time.time()
        endTime = time.time()
        while endTime < startTime + stepTime:
            endTime = time.time()
    
        for client in clients:
            client.running = False
        for client in clients:
            client.join()
        for client in clients:
            client.disconnect()
        # Wait a minute
        time.sleep(60)
        # Clean the database between runs
        databaseDeploy('Destroy', '/home/user25/config/dbconnect.ini')
        databaseDeploy('Create', '/home/user25/config/dbconnect.ini')
        deployQueues('/home/user25/config/dbconnect.ini')
        stuffDatabase('/home/user25/config/dbconnect.ini')
    return 0

if __name__ == '__main__':
    if(len(sys.argv) < 2):
        sys.exit(1)
    if(len(sys.argv) == 2 and sys.argv[1] == 'help'):
        print 'Usage: jython %s <trace_config_file>' % sys.argv[0]
        sys.exit(0)
    sys.exit(main())
