'''
Created on Oct 17, 2013

@author: Diego Ballesteros (diegob)
'''

import sys
import threading
import linecache
import random
import time

def sendMessagesToQueue(client, queueName, n):
    from org.ftab.communication.requests.SendMessageRequest import Context
    filename = sys.argv[3] + client.username
    handle = open(filename, 'w')
    s = linecache.getline(sys.argv[2], random.randint(1, 1800))
    for _ in xrange(n):
        start = time.time()
        client. SendMessage(s, random.randint(0,10), Context.fromByte(0), [queueName])
        end = time.time()
        handle.write("%s - %s" % (start, end))
    handle.close()
def main():
    from org.ftab.client import Client
    
    alices = []
    for i in xrange(500):
        alice = Client("Alice%d" % i)
        if alice.Connect("dryad06.ethz.ch", 34582):
            alices.append(alice)
    thread_list = []
    for i, alice in enumerate(alices):
        t = threading.Thread(target = sendMessagesToQueue, args = (alice, "NotOriginallyNamedQueue%d" % (i % 10), 1000))
        thread_list.append(t)
    for t in thread_list:
        t.start()
    for t in thread_list:
        t.join()
    for alice in alices:
        alice.Disconnect()

if __name__ == '__main__':
    if(len(sys.argv) == 2 and sys.argv[1] == 'help'):
        print 'Usage: jython %s <path-to-FTaB-jar> <path-to-msg-base> <path-to-base-log>' % sys.argv[0]
    if(len(sys.argv) < 4):
        sys.exit(1)
    sys.path.append(sys.argv[1])
    sys.exit(main())


if __name__ == '__main__':
    pass