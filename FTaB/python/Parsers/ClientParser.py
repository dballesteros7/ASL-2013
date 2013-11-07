'''
Created on Oct 31, 2013

@author: diegob
'''

import sys
import tarfile
import tempfile
import glob
import numpy

from matplotlib.pyplot import legend
import matplotlib.pyplot as plt


def plotAverageResponseTime(tarredLogs, clientType = 'Alice', eventKeyword = 'SEND', windowSize = 10):
    fileHandleList = []
    for path in tarredLogs.getnames():
        if clientType in path:
            handle = tarredLogs.extractfile(path)
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
        print breakCount
        breakCount = 0
        for handle in fileHandleList:
            secondDone = False
            while not secondDone:
                last_pos = handle.tell()
                line = handle.readline()
                if not line:
                    breakCount += 1
                    break
                if line.startswith("DISCONNECT"):
                    print "DISCONNECT"
                    #handle.seek(last_pos)
                    #breakCount += 1
                    #break
                if line.startswith(eventKeyword):
                    min_val = float(line.split()[1])
                    max_val = float(line.split()[2])
                    if min_val > currentEnd or max_val > currentEnd:
                        secondDone = True
                        handle.seek(last_pos)
                    else:
                        if(currentStart, currentEnd) not in data:
                            data[(currentStart, currentEnd)] = []
                        data[(currentStart, currentEnd)].append(max_val - min_val)
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
        #y.append(len(data[key])/windowSize)
    plt.plot(x,y, label= '200 clients\n2 servers\n5 threads per server\n20 clients per thread\n5 database connections per server')
    ax = plt.gca()
    ax.errorbar(x, y, yerr=numpy.vstack([error_low, error_high]), label='Stdev')
    ax.set_ylim([0,3])
    ax.set_xlabel('Experiment time (s)')
    ax.set_ylabel('Average response time (s)')
    ax.set_title('Trace: Average response time')
    legend()
    plt.show()
        
    for handle in fileHandleList:
        handle.close()

def main():
    tarLogs = tarfile.open('/home/dballesteros/Dropbox/asl_data/Experiment_3/trace_31_10_clients.tgz', 'r:gz')
    plotAverageResponseTime(tarLogs)

if __name__ == '__main__':
    sys.exit(main())