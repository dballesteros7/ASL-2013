'''
Module to deploy a database schema.
Created on Oct 16, 2013
Modified on Dec 5, 2013

@author: Diego Ballesteros (diegob)
'''

import sys
import ConfigParser

from org.ftab.server import DBConnectionDispatcher
from org.ftab.database import Create, Destroy

def main(mode, configFile):
    # Retrieve a database connection dispatcher and read the config file.
    dispatch = DBConnectionDispatcher()
    config = ConfigParser.RawConfigParser()
    config.readfp(open(configFile))

    # Configure the database connection pool.
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
        print 'Action completed successfully.'
    except:
        if conn is not None:
            print 'Exception during database action, rolling back.'
            conn.rollback()
    finally:
        if conn is not None:
            conn.close()
        dispatch.closePool()

if __name__ == '__main__':
    if(len(sys.argv) == 2 and sys.argv[1] == 'help'):
        print 'Usage: jython %s Create/Destroy  dbconnect.ini' % sys.argv[0]
    if(len(sys.argv) < 3):
        sys.exit(1)
    sys.exit(main(sys.argv[1], sys.argv[2]))
