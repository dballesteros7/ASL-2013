'''
Created on Nov 7, 2013

@author: diegob
'''

import calendar
import glob
import re
import sys
import tarfile
import tempfile
import time

from matplotlib.pyplot import legend
import numpy

import matplotlib.pyplot as plt


def plotAverageResponseTime(logList, windowSize = 10):
    fileHandleList = []
    for path in logList:
        handle = open(path, 'r')
        fileHandleList.append(handle)
    
    requestsPerClient = {}
    for line in fileHandleList[0]:
        m = re.match(r'(.*) org\.ftab\.server\.ClientConnection.*Received request to retrieve a message by [A-Z]+ ordered by [A-Z]+ from \[(.*)\]\.$', line.strip())
        if m is not None:
            origin = m.group(2)
            if origin not in requestsPerClient:
                requestsPerClient[origin] = []
                realTime = calendar.timegm(time.strptime(":".join(m.group(1).split(":")[:-1]), '%m-%d-%Y %H:%M'))
                realTime += float(m.group(1).split()[1].split(":")[-1])/1000.0
                requestsPerClient[origin].append([realTime])
        else:
            m = re.match(r'(.*) org\.ftab\.server\.ClientConnection.*Found message [0-9]+ filtered by [A-Z]+ ordered by [A-Z]+ for \[(.*)\]\.$', line.strip())
            if m is not None:
                origin = m.group(2)
                if origin not in requestsPerClient:
                    continue
                realTime = calendar.timegm(time.strptime(":".join(m.group(1).split(":")[:-1]), '%m-%d-%Y %H:%M'))
                realTime += float(m.group(1).split()[1].split(":")[-1])/1000.0
                requestsPerClient[origin][-1].append(realTime)
    print requestsPerClient[requestsPerClient.keys()[0]]
    for handle in fileHandleList:
        handle.close()

def main():
    logList = ['/home/diegob/dataWorkplace/serverlog0.log']
    plotAverageResponseTime(logList)

if __name__ == '__main__':
    sys.exit(main())