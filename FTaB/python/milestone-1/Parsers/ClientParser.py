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
from numpy.lib.polynomial import polyfit


def plotThroughput(logFile, windowSize = 10000, marker = 's', iteration = 50):
    fileHandle = open(logFile, 'r')
    

    wholeFile = fileHandle.read();
    wholeFile = wholeFile.split("\n")[:-1]
    wholeFileSorted = sorted(wholeFile, key = lambda x : float(x.split()[2]));
    
    globalMin = float(wholeFileSorted[0].split()[2])
    currentStart = globalMin
    currentEnd = currentStart + windowSize
    data = {(currentStart, currentEnd) : 0}
    for line in wholeFileSorted:
        min_val = float(line.split()[2])
        if min_val > currentEnd:
            currentStart = currentEnd
            currentEnd = currentStart + windowSize
            data[(currentStart, currentEnd)] = 0
        data[(currentStart, currentEnd)] += 1

    x = []
    y = []
    for key in sorted(data.keys()):
        x.append(((key[1] + key[0])/2.0 - globalMin)/1000.0)
        y.append(data[key]*1000/(key[1] - key[0]))
    p1, = plt.plot(x,y, marker = marker)
    ax = plt.gca()
    #ax.set_ylim([-10, 300])
    ax.set_xlabel('Experiment time (s)')
    ax.set_ylabel('Messages read per second')
    ax.set_title('Throughput of read operations')

    total = []
    for key in data:
        if((key[0] - globalMin)/1000. > 1500):
            continue
        if((key[0] - globalMin)/1000. < 300):
            continue
        total.append(data[key]*1000/(key[1] - key[0]))

    n, min_max, mean, var, _, _ = stats.describe(total)
    t_test = stats.t.interval(0.95, n - 1, loc=mean, scale=sqrt(var) / sqrt(n))
    chisquare = stats.chi2.interval(0.95, n - 1)
    #print "Data samples: %d" % n
    #print "Average response time: %s ms" % mean
    #print "Sample standard deviation: %s ms" % sqrt(var)
   # print "95%% confidence interval for the mean (t-student test): (%s, %s) ms" % stats.t.interval(0.95, n - 1,loc = mean, scale = sqrt(var)/sqrt(n))
    #chisquare = stats.chi2.interval(0.95, n - 1)
    #print "95%% confidence interval for the stdev (chi-square test): (%s, %s) ms" % (sqrt((n - 1)*var/chisquare[1]), sqrt((n - 1)*var/chisquare[0]))
    #m,b =  polyfit(x, y, 1)
    #print "slope: %s, intercept: %s" % (m, b)
    #print "$%d$ & $%.1f$ & $%.1f$ & $%.1f-%.1f$ & $%.1f-%.1f$" % (iteration, mean, sqrt(var), stats.t.interval(0.95, n - 1, loc=mean, scale=sqrt(var) / sqrt(n))[0], 
    #                                                              stats.t.interval(0.95, n - 1, loc=mean, scale=sqrt(var) / sqrt(n))[1],
    #                                                              sqrt((n - 1)*var/chisquare[1]), 
    #                                                               sqrt((n - 1)*var/chisquare[0]))
    print "$%d$ & $%d$ & $%d$ & $%.1f$ & $%.1f$ & $%.1f-%.1f$ & $%.1f-%.1f$ \\\\" % (iteration[0], iteration[1], 
                                                                                       iteration[2], mean, sqrt(var), t_test[0], t_test[1], sqrt((n - 1)*var/chisquare[1]), sqrt((n - 1)*var/chisquare[0]))
    fileHandle.close();
    return p1

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
    #ax.set_ylim([-10, 300])
    ax.set_xlabel('Experiment time (s)')
    ax.set_ylabel('Average response time (ms)')
    ax.set_title('Average database response time to find a queue with messages (if none is found) \n (60 seconds window)')

    total = []
    for key in data:
        if((key[0] - globalMin)/1000. > 1500):
            continue
        if((key[0] - globalMin)/1000. < 300):
            continue
        for val in data[key]:
            total.append(val)

    n, min_max, mean, var, _, _ = stats.describe(total)
    #print "Data samples: %d" % n
    #print "Average response time: %s ms" % mean
    #print "Sample standard deviation: %s ms" % sqrt(var)
    #print "95%% confidence interval for the mean (t-student test): (%s, %s) ms" % stats.t.interval(0.95, n - 1,loc = mean, scale = sqrt(var)/sqrt(n))
    t_test = stats.t.interval(0.95, n - 1, loc=mean, scale=sqrt(var) / sqrt(n))
    chisquare = stats.chi2.interval(0.95, n - 1)
    #print "95%% confidence interval for the stdev (chi-square test): (%s, %s) ms" % (sqrt((n - 1)*var/chisquare[1]), sqrt((n - 1)*var/chisquare[0]))
    #m,b =  polyfit(x, y, 1)
    #print "slope: %s, intercept: %s" % (m, b)
    print "$%d$ & $%d$ & $%d$ & $%.1f$ & $%.1f$ & $%.1f-%.1f$ & $%.1f-%.1f$ \\\\" % (iteration[0], iteration[1], 
                                                                                       iteration[2], mean, sqrt(var), t_test[0], t_test[1], sqrt((n - 1)*var/chisquare[1]), sqrt((n - 1)*var/chisquare[0]))
    #print "$%d$ & $%.1f$ & $%.1f$ & $%.1f-%.1f$ & $%.1f-%.1f$" % (iteration, mean, sqrt(var), stats.t.interval(0.95, n - 1, loc=mean, scale=sqrt(var) / sqrt(n))[0], 
    #                                                              stats.t.interval(0.95, n - 1, loc=mean, scale=sqrt(var) / sqrt(n))[1],
    #                                                              sqrt((n - 1)*var/chisquare[1]), 
    #                                                               sqrt((n - 1)*var/chisquare[0]))
    fileHandle.close();
    return p1

def plotSpecial(logFile, windowSize = 10000, marker = 's', iteration = 50):
    fileHandle = open(logFile, 'r')
    
    x = []
    y1 = []
    y2 = []
    y3 = []
    wholeFile = fileHandle.read();
    wholeFile = wholeFile.split("\n")[:-1]
    firstLine = wholeFile[0]
    globalMin = float(firstLine.strip().split()[0])
    for line in wholeFile[1:]:
        values = line.strip().split()
        timestamp = (float(values[0]) - globalMin)/1000.0
        x.append(timestamp)
        sizes = [int(k) for k in values[2:19]]
        y1.append(numpy.mean(sizes))
        y2.append(int(values[19]))
        y3.append(int(values[20]))


    p1, = plt.plot(x,y1, marker = 's')
    p2, = plt.plot(x,y2, marker = 'o')
    p3, = plt.plot(x,y3, marker = 'D')
    ax = plt.gca()
    ax.set_ylim([-100, 80000])
    ax.set_xlabel('Experiment time (s)')
    ax.set_ylabel('Number of messages stored per queue')
    ax.set_title('Number of messages stored per queue over time')
    plt.legend([p1,p2,p3], ["Larry queues", "Tola-Maruja queue", "Rick-Carl queue"])
    fileHandle.close();
    return p1

def main():
    
    #for x in [1, 50]:
    #    for y in [10, 20]:
    #        for z in [1, 5]:
    logList = '/home/diegob/dataWorkplace/Experiment_8/log-full-trace-2h-2/watchdog.log'
    p1 = plotSpecial(logList, windowSize = 60*1000, marker = 'o', iteration = [1, 20,5])
    #logList = '/home/diegob/dataWorkplace/Experiment_7/log-50-10-1/clients-reader-0.log'
    #p2 = plotAverageResponseTime(logList, windowSize = 60*1000, marker = 's', iteration = [1, 20,5])
                    #print "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s," % (x, y, z, m, x*y, x*z, x*m, y*z, y*m, z*m, x*y*z, x*y*m, x*z*m, y*z*m, x*y*z*m, 1)
    #plt.plot()
    #p1 = plotAverageResponseTime(logList, windowSize = 60*1000, marker = 'o')
    #p1 = plotThroughput(logList, windowSize = 60*1000, marker = 'o')
    #logList = '/home/diegob/dataWorkplace/Experiment_6/log-2000-1-125-125/clients-senders-0.log'
    #p2 = plotAverageResponseTime(logList, windowSize = 60*1000, marker = 's', iteration= [2000, 1, 125, 125])
#     p2 = plotThroughput(logList, windowSize = 60*1000, marker = 's')
#     logList = '/home/diegob/dataWorkplace/Experiment_6/log-2000-10-125-60/clients-reader-0.log'
#     #p2 = plotAverageResponseTime(logList, windowSize = 60*1000, marker = 's')
#     p3 = plotThroughput(logList, windowSize = 60*1000, marker = 'D')
#     logList = '/home/diegob/dataWorkplace/Experiment_6/log-2000-10-125-125/clients-reader-0.log'
#     #p2 = plotAverageResponseTime(logList, windowSize = 60*1000, marker = 's')
#     p4 = plotThroughput(logList, windowSize = 60*1000, marker = '*')
    #logList = '/home/diegob/dataWorkplace/Experiment_6/log-2000-10-125-60/clients-reader-0.log'
    #p3 = plotAverageResponseTime(logList, windowSize = 60*1000, marker = '*')
    #logList = '/home/diegob/dataWorkplace/Experiment_6/log-2000-10-60-60/clients-reader-0.log'
    #p4 = plotAverageResponseTime(logList, windowSize = 60*1000, marker = 'D')
    #plt.legend([p1,p2], ["Read operation", "Send operation"])
    plt.show()
    
    #plotAverageResponseTime(logList, eventKeyword = 'READ', windowSize = 120).show()
    #_, p1 = plotThroughput(logList, eventKeyword = 'SEND', windowSize = 120)
    #plt, p2 = plotThroughput(logList, eventKeyword = 'READ', windowSize = 120)
    #plt.legend([p1,p2], ["Send message", "Read (pop) message"])
    #plt.show()

if __name__ == '__main__':
    sys.exit(main())
