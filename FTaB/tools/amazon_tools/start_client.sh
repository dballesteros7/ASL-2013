#!/bin/bash
jython-2.5.3/bin/jython -Dpython.path=FTaB/FTaB.jar:FTaB/python -c "print 'Loading Jython'"
jython-2.5.3/bin/jython -Dpython.path=FTaB/FTaB.jar:FTaB/python FTaB/python/Load/2KExperiments/LoadParameters/RunSenders.py FTaB/config/trace_pilot_clients_${1}_sends_10.11.ini &> logs/client_senders_system.log &
jython-2.5.3/bin/jython -Dpython.path=FTaB/FTaB.jar:FTaB/python FTaB/python/Load/2KExperiments/LoadParameters/RunReaders.py FTaB/config/trace_pilot_clients_${1}_reads_10.11.ini &> logs/client_readers_system.log &
