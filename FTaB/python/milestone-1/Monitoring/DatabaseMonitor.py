'''
Created on Nov 12, 2013

@author: Diego Ballesteros (diegob)
'''

import ConfigParser
import sys
import time

from org.ftab.database.queue import RetrieveQueues
from org.ftab.server import DBConnectionDispatcher


def main(configFile):
    dispatch = DBConnectionDispatcher()
    config = ConfigParser.RawConfigParser()
    config.readfp(open(configFile))
    dispatch.configureDatabaseConnectionPool(config.get("dbconnection", 'user'),
                                             config.get("dbconnection", 'password'),
                                             config.get("dbconnection", 'server'),
                                             config.get("dbconnection", 'database'),
                                             2)
    runningTime = int(config.get("configuration", 'runningTime'))
    samplingPeriod = int(config.get("configuration", 'samplingPeriod'))
    logOutput = config.get("configuration", 'logPath')
    conn = None
    start = True
    outputHandle = open(logOutput, 'w')
    try:
        startTime = time.time();
        endTime = time.time();
        while endTime - startTime < runningTime:
            conn = dispatch.retrieveDatabaseConnection()
            result = RetrieveQueues.execute(conn)
            result = sorted(result, key = lambda x : int(x.getName()[23:]))
            outputHandle.write("%s " % int(time.time()*1000.0))
            if start:
                for queue in result[:-1]:
                    queueName = queue.getName()
                    outputHandle.write("%s " % queueName)
                outputHandle.write("%s\n" % result[-1].getName())
                start = False
            else:
                for queue in result[:-1]:
                    queueCount = queue.getMessageCount()
                    outputHandle.write("%s " % queueCount)
                outputHandle.write("%s\n" % result[-1].getMessageCount())
            conn.commit()
            conn.close()
            outputHandle.flush()
            time.sleep(samplingPeriod)
            endTime = time.time()
    except:
        import traceback
        traceback.print_exc()
        if conn is not None:
            print 'Exception during database action, rolling back.'
            conn.rollback()
    finally:
        if conn is not None:
            conn.close()
        dispatch.closePool()
    outputHandle.close()

if __name__ == '__main__':
    if(len(sys.argv) == 2 and sys.argv[1] == 'help'):
        print 'Usage: jython %s <path-to-dbconnect.ini>' % sys.argv[0]
    if(len(sys.argv) < 2):
        sys.exit(1)
    sys.exit(main(sys.argv[1]))