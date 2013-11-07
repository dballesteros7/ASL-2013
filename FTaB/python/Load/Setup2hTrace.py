'''
Setup a 2h trace with:
 
50 OneWayClients

Setup of experiment: 22nd October, 2013

Created on Oct 22, 2013

@author: Diego Ballesteros (diegob)
'''

import sys
import time

from Load.OneWayClient import OneWayClient

def main():
    possibleQueues = []
    for i in xrange(20):
        queue = "NotOriginallyNamedQueue%d" % i
        possibleQueues.append(queue)
    larries = []
    for i in xrange(50):
        larry = OneWayClient("Larry%d" % i, possibleQueues, 50, sys.argv[3])
        larry.setup(sys.argv[1], int(sys.argv[2]))
        larries.append(larry)

    for larry in larries:
        larry.start()

    startTime = time.time()
    endTime = time.time()
    while endTime < startTime + 2.0*3600.0:
        endTime = time.time()

    for larry in larries:
        larry.running = False
    for larry in larries:
        larry.join()
    for larry in larries:
        larry.disconnect()

if __name__ == '__main__':
    if(len(sys.argv) == 2 and sys.argv[1] == 'help'):
        print 'Usage: jython %s <server-address> <server-port> <path-to-log-base>' % sys.argv[0]
    if(len(sys.argv) < 4):
        sys.exit(1)
    sys.exit(main())
