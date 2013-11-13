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
        if(key[0] - globalMin < 200):
            continue
        total.append(data[key]/windowSize)

    n, min_max, mean, var, _, _ = stats.describe(total)
    print n
    print mean
    print sqrt(var)
    print min_max
    print stats.t.interval(0.999, n - 1,loc = mean, scale = sqrt(var)/sqrt(n))
        
    for handle in fileHandleList:
        handle.close()
    return plt, p1

def plotAverageResponseTime(logList, eventKeyword = 'SEND', windowSize = 10, marker = 's'):
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
    for idx, key in enumerate(sorted(data.keys())):
        x.append((key[1] + key[0])/2.0 - globalMin)
        mean = numpy.mean(data[key])
        stdev = numpy.std(data[key])
        y.append(mean)
        error_low.append(min(mean, stdev))
        error_high.append(stdev)
        #if idx == 2:
        #    print "%s +- %s" % (mean, stdev)
        #if idx == len(data.keys()) - 2:
        #    print "%s +- %s" % (mean, stdev)
    p1, = plt.plot(x,y, marker = marker)
    ax = plt.gca()
    ax.errorbar(x, y, yerr=numpy.vstack([error_low, error_high]))#, marker = marker)
    #ax.set_ylim([-100, 200])
    #ax.set_xlim([0, 7400])
    ax.set_xlabel('Experiment time (s)')
    ax.set_ylabel('Average response time (ms)')
    ax.set_title('Average response time to read a message for N clients \n (30 seconds window)')

    total = []
    for key in data:
        if(key[0] - globalMin < 100):
            continue
        if(key[0] - globalMin > 500):
            continue
        for val in data[key]:
            total.append(val)

    n, min_max, mean, var, _, _ = stats.describe(total)
    print "Data samples: %d" % n
    print "Average response time: %s ms" % mean
    print "Sample standard deviation: %s ms" % sqrt(var)
    print "95%% confidence interval for the mean (t-student test): (%s, %s) ms" % stats.t.interval(0.95, n - 1,loc = mean, scale = sqrt(var)/sqrt(n))
    chisquare = stats.chi2.interval(0.95, n - 1)
    print "95%% confidence interval for the stdev (chi-square test): (%s, %s) ms" % (sqrt((n - 1)*var/chisquare[1]), sqrt((n - 1)*var/chisquare[0]))
        
    for handle in fileHandleList:
        handle.close()
    return plt, p1

def main():
    print "20 clients send:"
    logList = glob.glob(os.path.join('/home/dballesteros/dataWorkplace/Experiment_4/Bob*-1'))
    plt, p1 = plotAverageResponseTime(logList, eventKeyword = 'READ', windowSize = 30, marker = 's')
    print "\n40 clients send:"
    logList = glob.glob(os.path.join('/home/dballesteros/dataWorkplace/Experiment_4/Bob*-4'))
    plt, p2 = plotAverageResponseTime(logList, eventKeyword = 'READ', windowSize = 30, marker = 'o')
    print "60 clients send:"
    logList = glob.glob(os.path.join('/home/dballesteros/dataWorkplace/Experiment_4/Bob*-7'))
    plt, p3 = plotAverageResponseTime(logList, eventKeyword = 'READ', windowSize = 30, marker = '*')
    print "80 clients send:"
    logList = glob.glob(os.path.join('/home/dballesteros/dataWorkplace/Experiment_4/Bob*-10'))
    plt, p4 = plotAverageResponseTime(logList, eventKeyword = 'READ', windowSize = 30, marker = '^')
    print "100 clients send:"
    logList = glob.glob(os.path.join('/home/dballesteros/dataWorkplace/Experiment_4/Bob*-12'))
    plt, p5 = plotAverageResponseTime(logList, eventKeyword = 'READ', windowSize = 30, marker = 'D')
#     print "120 clients send:"
#     logList = glob.glob(os.path.join('/home/dballesteros/dataWorkplace/Experiment_4/Alice*-6'))
#     plt, p5 = plotAverageResponseTime(logList, eventKeyword = 'SEND', windowSize = 30, marker = 'D')
#     print "140 clients send:"
#     logList = glob.glob(os.path.join('/home/dballesteros/dataWorkplace/Experiment_4/Alice*-7'))
#     plt, p5 = plotAverageResponseTime(logList, eventKeyword = 'SEND', windowSize = 30, marker = 'D')
#     print "160 clients send:"
#     logList = glob.glob(os.path.join('/home/dballesteros/dataWorkplace/Experiment_4/Alice*-8'))
#     plt, p5 = plotAverageResponseTime(logList, eventKeyword = 'SEND', windowSize = 30, marker = 'D')
#     print "180 clients send:"
#     logList = glob.glob(os.path.join('/home/dballesteros/dataWorkplace/Experiment_4/Alice*-9'))
#     plt, p5 = plotAverageResponseTime(logList, eventKeyword = 'SEND', windowSize = 30, marker = 'D')
#     print "200 clients send:"
#     logList = glob.glob(os.path.join('/home/dballesteros/dataWorkplace/Experiment_4/Alice*-10'))
#     plt, p5 = plotAverageResponseTime(logList, eventKeyword = 'SEND', windowSize = 30, marker = 'D')
#     print "220 clients send:"
#     logList = glob.glob(os.path.join('/home/dballesteros/dataWorkplace/Experiment_4/Alice*-11'))
#     plt, p5 = plotAverageResponseTime(logList, eventKeyword = 'SEND', windowSize = 30, marker = 'D')
#     print "240 clients send:"
#     logList = glob.glob(os.path.join('/home/dballesteros/dataWorkplace/Experiment_4/Alice*-12'))
#     plt, p5 = plotAverageResponseTime(logList, eventKeyword = 'SEND', windowSize = 30, marker = 'D')

    #plt.show()
    #logList = glob.glob(os.path.join('/home/dballesteros/dataWorkplace/Experiment_3/Bob*log-1'))
    #plt, p2 = plotAverageResponseTime(logList, eventKeyword = 'READ', windowSize = 120)
    plt.legend([p1, p2, p3, p4 ,p5], ["20", "80", "140", "200", "240"])
    plt.show()
    #plotAverageResponseTime(logList, eventKeyword = 'READ', windowSize = 120).show()
    #_, p1 = plotThroughput(logList, eventKeyword = 'SEND', windowSize = 120)
    #plt, p2 = plotThroughput(logList, eventKeyword = 'READ', windowSize = 120)
    #plt.legend([p1,p2], ["Send message", "Read & pop message"])
   # plt.show()

if __name__ == '__main__':
    sys.exit(main())