'''
Created on 24.10.2013

@author: Diego
'''

import sys
import shlex
import subprocess
import time

def main():
    baseCommand = "ps -p %i -o pid,ppid,rss,vsize,pcpu,pmem,cmd | grep %i"
    pid = int(sys.argv[1])
    args = shlex.split(baseCommand % (pid, pid))
    logPath = open(sys.argv[2], 'w')
    while(1):
        output = subprocess.check_output(args)
        output = output.split()
        logPath.write("RSS: %s VSize: %s PCPU: %s PMEM: %s\n" % (output[2], output[3],
                                                                 output[4], output[5]))
        logPath.flush()
        time.sleep(60)
    logPath.close()
    
if __name__ == '__main__':
    if(len(sys.argv) == 2 and sys.argv[1] == 'help'):
        print 'Usage: jython %s <PID> <logBase>' % sys.argv[0]
    if(len(sys.argv) < 3):
        sys.exit(1)
    sys.exit(main())