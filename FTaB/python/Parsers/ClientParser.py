'''
Created on Oct 31, 2013

@author: diegob
'''


import sys

from matplotlib.pyplot import legend
import numpy
from numpy.lib.scimath import sqrt
from scipy import stats

import matplotlib.pyplot as plt

def plotAverageResponseTime(logFile, windowSize = 10000, marker = 's', iteration = 50):
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
        error_low.append(min(numpy.mean(data[key]), numpy.std(data[key])))
        error_high.append(numpy.std(data[key]))
    p1, = plt.plot(x,y, marker = marker)
    ax = plt.gca()
    ax.errorbar(x, y, yerr=numpy.vstack([error_low, error_high]))#, marker = marker)
    ax.set_ylim([-10, 300])
    ax.set_xlabel('Experiment time (s)')
    ax.set_ylabel('Average response time (ms)')
    ax.set_title('Average response time to send a message \n (30 seconds window)')

    total = []
    for key in data:
        if((key[0] - globalMin)/1000. > 500):
            continue
        if((key[0] - globalMin)/1000. < 100):
            continue
        for val in data[key]:
            total.append(val)

    n, min_max, mean, var, _, _ = stats.describe(total)
    #print "Data samples: %d" % n
    #print "Average response time: %s ms" % mean
    #print "Sample standard deviation: %s ms" % sqrt(var)
    #print "95%% confidence interval for the mean (t-student test): (%s, %s) ms" % stats.t.interval(0.95, n - 1,loc = mean, scale = sqrt(var)/sqrt(n))
    chisquare = stats.chi2.interval(0.95, n - 1)
    #print "95%% confidence interval for the stdev (chi-square test): (%s, %s) ms" % (sqrt((n - 1)*var/chisquare[1]), sqrt((n - 1)*var/chisquare[0]))
    print "$%d$ & $%.1f$ & $%.1f$ & $%.1f-%.1f$ & $%.1f-%.1f$" % (iteration, mean, sqrt(var), stats.t.interval(0.95, n - 1, loc=mean, scale=sqrt(var) / sqrt(n))[0], 
                                                                  stats.t.interval(0.95, n - 1, loc=mean, scale=sqrt(var) / sqrt(n))[1],
                                                                  sqrt((n - 1)*var/chisquare[1]), 
                                                                   sqrt((n - 1)*var/chisquare[0]))
    fileHandle.close();
    return p1

def main():
    logList = '/home/dballesteros/dataWorkplace/Experiment_5/clients-senders-50-0.log'
    p1 = plotAverageResponseTime(logList, windowSize = 30*1000, marker = 'o')
    logList = '/home/dballesteros/dataWorkplace/Experiment_5/clients-senders-25-0.log'
    #print "Read with 25 readers:"
    p2 = plotAverageResponseTime(logList, windowSize = 30*1000, marker = 's', iteration = 25)
    logList = '/home/dballesteros/dataWorkplace/Experiment_5/clients-senders-16-0.log'
    #print "Read with 16 readers:"
    p3 = plotAverageResponseTime(logList, windowSize = 30*1000, marker = '^', iteration = 16)
    logList = '/home/dballesteros/dataWorkplace/Experiment_5/clients-senders-12-0.log'
    #print "Read with 12 readers:"
    p4 = plotAverageResponseTime(logList, windowSize = 30*1000, marker = 'D', iteration = 12)
    logList = '/home/dballesteros/dataWorkplace/Experiment_5/clients-senders-10-0.log'
    #print "Read with 10 readers:"
    p5 = plotAverageResponseTime(logList, windowSize = 30*1000, marker = '*', iteration = 10)
    plt.legend([p1,p2,p3,p4,p5], ["1:1", "1:2", "1:3", "1:4", "1:5" ])
    plt.show()
    
    #plotAverageResponseTime(logList, eventKeyword = 'READ', windowSize = 120).show()
    #_, p1 = plotThroughput(logList, eventKeyword = 'SEND', windowSize = 120)
    #plt, p2 = plotThroughput(logList, eventKeyword = 'READ', windowSize = 120)
    #plt.legend([p1,p2], ["Send message", "Read (pop) message"])
    #plt.show()

if __name__ == '__main__':
    sys.exit(main())
