'''
Module to create several queues in the system.

Created on Oct 16, 2013
Modified on Dec 5, 2013

@author: Diego Ballesteros (diegob)
'''

import sys
import traceback
import ConfigParser

from org.ftab.server import DBConnectionDispatcher
from org.ftab.database.queue import CreateQueue

def main(configFile):
    # Retrieve a connection dispatcher and read the configuration file
    dispatch = DBConnectionDispatcher()
    config = ConfigParser.RawConfigParser()
    config.readfp(open(configFile))

    # Configure the dispatcher
    dispatch.configureDatabaseConnectionPool(config.get("dbconnection", 'user'),
                                             config.get("dbconnection", 'password'),
                                             config.get("dbconnection", 'server'),
                                             config.get("dbconnection", 'database'),
                                             1)

    # Read the queue settings
    queueBaseName = config.get("queuesettings", "queuebasename")
    queueNumber = int(config.get("queuesettings", "numberofqueues"))

    conn = None
    try:
        conn = dispatch.retrieveDatabaseConnection()
        for i in xrange(queueNumber):
            CreateQueue.execute("%s%d" % (queueBaseName, i), conn)
        conn.commit()
        print 'Action completed successfully.'
    except Exception:
        if conn is not None:     
            print 'Exception during database action, rolling back.'
            traceback.print_exc()
            conn.rollback()
    finally:
        if conn is not None:
            conn.close()
        dispatch.closePool()

if __name__ == '__main__':
    if(len(sys.argv) == 2 and sys.argv[1] == 'help'):
        print 'Usage: jython %s setup.ini' % sys.argv[0]
    if(len(sys.argv) < 2):
        sys.exit(1)
    sys.exit(main(sys.argv[1]))
