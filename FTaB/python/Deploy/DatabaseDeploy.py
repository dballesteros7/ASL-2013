'''
Created on Oct 16, 2013

@author: Diego Ballesteros (diegob)
'''

import sys
import ConfigParser

def main():
    from org.ftab.server import DBConnectionDispatcher
    from org.ftab.database import Create, Destroy
    mode = sys.argv[2]
    dispatch = DBConnectionDispatcher()
    config = ConfigParser.RawConfigParser()
    config.readfp(open(sys.argv[3]))
    dispatch.configureDatabaseConnectionPool(config.get("dbconnection", 'user'),
                                             config.get("dbconnection", 'password'),
                                             config.get("dbconnection", 'server'),
                                             config.get("dbconnection", 'database'),
                                             5)
    conn = None
    try:
        conn = dispatch.retrieveDatabaseConnection()
        if mode == 'Create':
            Create.execute(True, True, True, conn)
        else:
            Destroy.execute(True, True, True, conn)
        conn.commit()
        print 'Action completed successfully'
    except:
        if conn is not None:
            print 'Exception during database action, rolling back.'
            conn.rollback()
    finally:
        if conn is not None:
            conn.close()

if __name__ == '__main__':
    if(len(sys.argv) == 2 and sys.argv[1] == 'help'):
        print 'Usage: jython %s <path-to-FTaB-jar> <Create/Destroy> <path-to-dbconnect.ini>' % sys.argv[0]
    if(len(sys.argv) < 4):
        sys.exit(1)
    sys.path.append(sys.argv[1])
    sys.exit(main())