'''
Created on Oct 16, 2013

@author: Diego Ballesteros (diegob)
'''

import sys
import ConfigParser

def main():
    from org.ftab.server import DBConnectionDispatcher
    from org.ftab.database.queue import CreateQueue
    dispatch = DBConnectionDispatcher()
    config = ConfigParser.RawConfigParser()
    config.readfp(open(sys.argv[2]))
    dispatch.configureDatabaseConnectionPool(config.get("dbconnection", 'user'),
                                             config.get("dbconnection", 'password'),
                                             config.get("dbconnection", 'server'),
                                             config.get("dbconnection", 'database'),
                                             5)
    conn = None
    try:
        conn = dispatch.retrieveDatabaseConnection()
        for i in xrange(100):
            CreateQueue.execute("NotOriginallyNamedQueue%d" % i, conn)
        conn.commit()
        print 'Action completed successfully'
    except Exception:
        if conn is not None:     
            import traceback
            traceback.print_exc()  
            print 'Exception during database action, rolling back.'
            conn.rollback()
    finally:
        if conn is not None:
            conn.close()
        dispatch.closePool()

if __name__ == '__main__':
    if(len(sys.argv) == 2 and sys.argv[1] == 'help'):
        print 'Usage: jython %s <path-to-FTaB-jar> <path-to-dbconnect.ini>' % sys.argv[0]
    if(len(sys.argv) < 3):
        sys.exit(1)
    sys.path.append(sys.argv[1])
    sys.exit(main())
