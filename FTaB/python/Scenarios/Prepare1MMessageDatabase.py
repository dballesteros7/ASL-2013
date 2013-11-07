'''
Created on Oct 31, 2013

@author: Diego Ballesteros (diegob)
'''

import ConfigParser
from random import randint, choice
import string
import sys

from org.ftab.communication.requests.SendMessageRequest import Context
from org.ftab.database.client import CreateClient
from org.ftab.database.message import CreateMessage
from org.ftab.server import DBConnectionDispatcher


def main(configFile):
    dispatch = DBConnectionDispatcher()
    config = ConfigParser.RawConfigParser()
    config.readfp(open(configFile))
    dispatch.configureDatabaseConnectionPool(config.get("dbconnection", 'user'),
                                             config.get("dbconnection", 'password'),
                                             config.get("dbconnection", 'server'),
                                             config.get("dbconnection", 'database'),
                                             1)
    conn = None
    try:
        conn = dispatch.retrieveDatabaseConnection()
        clientId = CreateClient.execute("MessengerGod", False, conn)
        msg = ''.join(choice(string.ascii_uppercase + string.digits) for _ in range(2000))
        for i in xrange(1000000):
            CreateMessage.execute(clientId, ["NotOriginallyNamedQueue%d" % randint(0, 20)], Context.valueOf("NONE").getByteValue(), randint(1,10), msg  , conn)
            if(i % 100 == 0):
                print "Progress %s%%" % (float(i)*100/1000000)
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
        print 'Usage: jython %s <path-to-dbconnect.ini>' % sys.argv[0]
    if(len(sys.argv) < 2):
        sys.exit(1)
    sys.exit(main(sys.argv[1]))
