'''
Created on Oct 31, 2013

@author: diegob
'''

import glob
import os
import sys

from matplotlib.pyplot import legend
import numpy
from numpy.lib.scimath import sqrt
from scipy import stats

import matplotlib.pyplot as plt

def plotAverageResponseTime(logFile, windowSize = 10000):
    fileHandle = open(logFile, 'r')
    

    wholeFile = fileHandle.read();
    wholeFile = wholeFile.split("\n")[:-1]
    wholeFileSorted = sorted(wholeFile, key = lambda x : float(x.split()[2]));
    
    globalMin = float(wholeFileSorted[0].split()[2])
    currentStart = globalMin
    currentEnd = currentStart + windowSize
    data = {(currentStart, currentEnd) : []}
    for line in wholeFileSorted:
        min_val = float(line.split()[2])
        duration = float(line.split()[3])
        if min_val > currentEnd:
            currentStart = currentEnd
            currentEnd = currentStart + windowSize
            data[(currentStart, currentEnd)] = []
        data[(currentStart, currentEnd)].append(duration)

    x = []
    y = []
    error_low = []
    error_high = []
    for key in sorted(data.keys()):
        x.append(((key[1] + key[0])/2.0 - globalMin)/1000.0)
        y.append(numpy.mean(data[key]))
        error_low.append(numpy.std(data[key]))
        error_high.append(numpy.std(data[key]))
    marker = 's'
    p1, = plt.plot(x,y, marker = marker)
    ax = plt.gca()
    ax.errorbar(x, y, yerr=numpy.vstack([error_low, error_high]))#, marker = marker)
    #ax.set_ylim([0, 80])
    ax.set_xlabel('Experiment time (s)')
    ax.set_ylabel('Average response time (ms)')
    ax.set_title('Average response time to send a message \n (2 minutes window)')

    total = []
    for key in data:
        if((key[0] - globalMin)/1000. > 500):
            continue
        if((key[0] - globalMin)/1000. < 100):
            continue
        for val in data[key]:
            total.append(val)

    n, min_max, mean, var, _, _ = stats.describe(total)
    print mean
    print sqrt(var)
    print min_max
    print stats.t.interval(0.95, n - 1,loc = mean, scale = sqrt(var)/sqrt(mean))
    fileHandle.close();
    return plt, p1

def main():
    logList = '/home/diegob/logs/pilot-logs/logs/clients-senders-0.log'
    plotAverageResponseTime(logList, windowSize = 120*1000)[0]
    plt.show()
    
    #plotAverageResponseTime(logList, eventKeyword = 'READ', windowSize = 120).show()
    #_, p1 = plotThroughput(logList, eventKeyword = 'SEND', windowSize = 120)
    #plt, p2 = plotThroughput(logList, eventKeyword = 'READ', windowSize = 120)
    #plt.legend([p1,p2], ["Send message", "Read (pop) message"])
    #plt.show()

if __name__ == '__main__':
    sys.exit(main())