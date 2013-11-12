#!/bin/bash
jython-2.5.3/bin/jython -Dpython.path=FTaB/FTaB.jar:FTaB/python -c "print 'Loading Jython'"
jython-2.5.3/bin/jython -Dpython.path=FTaB/FTaB.jar:FTaB/python FTaB/python/Load/FullTrace/RunTrace.py FTaB/config/trace_full_2h_12.11.ini &> logs/clients_system.log &
