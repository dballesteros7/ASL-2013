'''
Setup a running trace with:
 
n OneWayClients (a.k.a Larry)

Created on Oct 22, 2013

@author: Diego Ballesteros (diegob)
'''

import ConfigParser
import sys
import time

from Load.OneWayClient import OneWayClient

def main():
    config = ConfigParser.RawConfigParser()
    config.readfp(open(sys.argv[1]))
    queueBaseName = config.get("system", "queueName")
    availableQueues = int(config.get("system", "queueNumber"))
    maxClients = int(config.get("trace", "clients"))
    logPath = config.get("trace", "logPath")
    server = config.get("system", "server")
    port = int(config.get("system", "port"))
    traceTime = int(config.get("trace", "runningTime"))

    possibleQueues = []
    for i in xrange(availableQueues):
        queue = "%s%d" % (queueBaseName, i)
        possibleQueues.append(queue)

    larries = []
    for i in xrange(maxClients):
        larry = OneWayClient("Larry%d" % i, possibleQueues, maxClients, logPath)
        larry.setup(server, port)
        larries.append(larry)
    for larry in larries:
        larry.start()

    startTime = time.time()
    endTime = time.time()
    while endTime < startTime + traceTime:
        endTime = time.time()

    for larry in larries:
        larry.running = False
    for larry in larries:
        larry.join()
    for larry in larries:
        larry.disconnect()

if __name__ == '__main__':
    if(len(sys.argv) < 2):
        sys.exit(1)
    if(len(sys.argv) == 2 and sys.argv[1] == 'help'):
        print 'Usage: jython %s <trace_config_file>' % sys.argv[0]
        sys.exit(0)
    sys.exit(main())
