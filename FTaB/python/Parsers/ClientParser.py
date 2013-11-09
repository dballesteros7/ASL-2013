'''
Created on Oct 31, 2013

@author: diegob
'''

import glob
import os
import sys

import numpy
from numpy.lib.scimath import sqrt
from scipy import stats

import matplotlib.pyplot as plt

def plotThroughput(logList, eventKeyword = 'SEND', windowSize = 10):
    fileHandleList = []
    for path in logList:
        handle = open(path, 'r')
        fileHandleList.append(handle)
    
    globalMin = sys.float_info.max
    for handle in fileHandleList:
        found = False
        while not found:
            line = handle.readline()
            if line.startswith(eventKeyword):
                found = True
                min_val = float(line.split()[1])
                if min_val < globalMin:
                    globalMin = min_val
        handle.seek(0)
    currentStart = globalMin
    currentEnd = globalMin + windowSize
    data = {}
    breakCount = 0
    while breakCount < len(fileHandleList):
        breakCount = 0
        for handle in fileHandleList:
            secondDone = False
            while not secondDone:
                last_pos = handle.tell()
                line = handle.readline()
                if not line:
                    breakCount += 1
                    break
                if line.startswith(eventKeyword):
                    min_val = float(line.split()[1])
                    max_val = float(line.split()[2])
                    if min_val > currentEnd or max_val > currentEnd:
                        secondDone = True
                        handle.seek(last_pos)
                    else:
                        if(currentStart, currentEnd) not in data:
                            data[(currentStart, currentEnd)] = 0
                        data[(currentStart, currentEnd)] += 1
        currentStart = currentEnd
        currentEnd = currentStart + windowSize
    x = []
    y = []
    for key in sorted(data.keys()):
        x.append((key[1] + key[0])/2.0 - globalMin)
        y.append(data[key]/windowSize)
    marker = 's'
    if eventKeyword == 'SEND':
        marker = 'o'
    p1, = plt.plot(x,y, marker = marker)
    ax = plt.gca()
    ax.set_ylim([280, 350])
    ax.set_xlabel('Experiment time (s)')
    ax.set_ylabel('Throughput (messages/s)')
    ax.set_title('Throughput for intervals of 2 minutes')

    total = []
    for key in data:
        if(key[0] - globalMin > 7000):
            continue
        total.append(data[key]/windowSize)

    n, min_max, mean, var, _, _ = stats.describe(total)
    print mean
    print sqrt(var)
    print min_max
    print stats.t.interval(0.99, n - 1,loc = mean, scale = sqrt(var)/sqrt(mean))
        
    for handle in fileHandleList:
        handle.close()
    return plt, p1
def plotAverageResponseTime(logList, eventKeyword = 'SEND', windowSize = 10):
    fileHandleList = []
    for path in logList:
        handle = open(path, 'r')
        fileHandleList.append(handle)
    
    globalMin = sys.float_info.max
    for handle in fileHandleList:
        found = False
        while not found:
            line = handle.readline()
            if line.startswith(eventKeyword):
                found = True
                min_val = float(line.split()[1])
                if min_val < globalMin:
                    globalMin = min_val
        handle.seek(0)
    currentStart = globalMin
    currentEnd = globalMin + windowSize
    data = {}
    breakCount = 0
    while breakCount < len(fileHandleList):
        breakCount = 0
        for handle in fileHandleList:
            secondDone = False
            while not secondDone:
                last_pos = handle.tell()
                line = handle.readline()
                if not line:
                    breakCount += 1
                    break
                if line.startswith(eventKeyword):
                    min_val = float(line.split()[1])
                    max_val = float(line.split()[2])
                    if min_val > currentEnd or max_val > currentEnd:
                        secondDone = True
                        handle.seek(last_pos)
                    else:
                        if(currentStart, currentEnd) not in data:
                            data[(currentStart, currentEnd)] = []
                        data[(currentStart, currentEnd)].append(1000*(max_val - min_val))
        currentStart = currentEnd
        currentEnd = currentStart + windowSize
    x = []
    y = []
    error_low = []
    error_high = []
    for key in sorted(data.keys()):
        x.append((key[1] + key[0])/2.0 - globalMin)
        y.append(numpy.mean(data[key]))
        error_low.append(numpy.std(data[key]))
        error_high.append(numpy.std(data[key]))
    marker = 's'
    if eventKeyword == 'QUEUE_FOUND':
        marker = 'o'
    p1, = plt.plot(x,y)#, marker = marker)
    ax = plt.gca()
    ax.errorbar(x, y, yerr=numpy.vstack([error_low, error_high]))#, marker = marker)
    #ax.set_ylim([0, 80])
    ax.set_xlabel('Experiment time (s)')
    ax.set_ylabel('Average response time (ms)')
    ax.set_title('Average response time to send a message \n (2 minutes window)')

    total = []
    for key in data:
        if(key[0] - globalMin > 7000):
            continue
        for val in data[key]:
            total.append(val)

    n, min_max, mean, var, _, _ = stats.describe(total)
    print mean
    print sqrt(var)
    print min_max
    print stats.t.interval(0.99, n - 1,loc = mean, scale = sqrt(var)/sqrt(mean))
        
    for handle in fileHandleList:
        handle.close()
    return plt, p1

def main():
    logList = glob.glob(os.path.join('/home/diegob/dataWorkplace/Bob*log'))
    plotAverageResponseTime(logList, eventKeyword = 'READ', windowSize = 120)[0].show()
    #plotAverageResponseTime(logList, eventKeyword = 'READ', windowSize = 120).show()
    #_, p1 = plotThroughput(logList, eventKeyword = 'SEND', windowSize = 120)
    #plt, p2 = plotThroughput(logList, eventKeyword = 'READ', windowSize = 120)
    #plt.legend([p1,p2], ["Send message", "Read (pop) message"])
    #plt.show()

if __name__ == '__main__':
    sys.exit(main())