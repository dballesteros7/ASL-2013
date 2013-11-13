#!/bin/bash

for readers in 25 16 12 10
do
    sed -i 's/ReaderNumber = .*/ReaderNumber = '"$readers"'/g' /home/user25/config/trace_dryad_read_13.11.ini
    sed -i 's/LogPath = .*/LogPath = \/home\/user25\/logs\/clients-readers-'"$readers"'-%g.log/g' /home/user25/config/trace_dryad_read_13.11.ini 
    sed -i 's/LogPath = .*/LogPath = \/home\/user25\/logs\/clients-senders-'"$readers"'-%g.log/g' /home/user25/config/trace_dryad_send_13.11.ini
    /home/user25/jython-2.5.3/bin/jython -Dpython.path=FTaB.jar:/home/user25/python/ /home/user25/python/Deploy/DatabaseDeploy.py Create /home/user25/config/dbconnect.ini
    /home/user25/jython-2.5.3/bin/jython -Dpython.path=FTaB.jar:/home/user25/python/ /home/user25/python/Scenarios/InitializedSystemNoMessages.py /home/user25/config/dbconnect.ini
    /home/user25/jython-2.5.3/bin/jython -Dpython.path=FTaB.jar:/home/user25/python/ /home/user25/python/Load/LastDay/RunSenders.py /home/user25/config/trace_dryad_send_13.11.ini &> /home/user25/logs/system-senders-${readers}.log &
    /home/user25/jython-2.5.3/bin/jython -Dpython.path=FTaB.jar:/home/user25/python/ /home/user25/python/Load/LastDay/RunReaders.py /home/user25/config/trace_dryad_read_13.11.ini &> /home/user25/logs/system-readers-${readers}.log &
    sleep 660
    /home/user25/jython-2.5.3/bin/jython -Dpython.path=FTaB.jar:/home/user25/python/ /home/user25/python/Deploy/DatabaseDeploy.py Destroy /home/user25/config/dbconnect.ini
done
