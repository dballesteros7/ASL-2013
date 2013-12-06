#!/bin/bash
/home/user25/jython-2.5.3/bin/jython -Dpython.path=/home/user25/dist/FTaB-server.jar:/home/user25/python/ /home/user25/python/Milestone2/Setup/DatabaseDeploy.py Create /home/user25/config/milestone_2/dbconnect.ini
/home/user25/jython-2.5.3/bin/jython -Dpython.path=/home/user25/dist/FTaB-server.jar:/home/user25/python/ /home/user25/python/Milestone2/Setup/InitializeQueues.py /home/user25/config/milestone_2/dbconnect.ini
/home/user25/jython-2.5.3/bin/jython -Dpython.path=/home/user25/dist/FTaB-server.jar:/home/user25/python/ /home/user25/python/Milestone2/TraceGenerator.py /home/user25/config/milestone_2/trace_06_12.ini &> /home/user25/logs/system.log &
sleep 660
/home/user25/jython-2.5.3/bin/jython -Dpython.path=/home/user25/dist/FTaB-server.jar:/home/user25/python/ /home/user25/python/Milestone2/Setup/DatabaseDeploy.py Destroy /home/user25/config/milestone_2/dbconnect.ini
