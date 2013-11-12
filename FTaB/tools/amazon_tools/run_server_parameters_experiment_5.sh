#!/bin/bash
set -e
# For each parameter, we edit the trace and server config files accordingly and run a experiment_cycle.
for clientsPerWorker in 1 50 
do
    for workers in 10 20
    do
        clients=$(($clientsPerWorker*$workers/2))
        for traceFile in 1 2 3 4 5
        do
            offset=$(($clients*($traceFile - 1)))
            sed -i 's/ReaderNumber = .*/ReaderNumber = '"$clients"'/g' $1/config/trace_pilot_clients_${traceFile}_reads_12.11.ini
            sed -i 's/ReaderOffset = .*/ReaderOffset = '"$offset"'/g' $1/config/trace_pilot_clients_${traceFile}_reads_12.11.ini
            sed -i 's/SenderNumber = .*/SenderNumber = '"$clients"'/g' $1/config/trace_pilot_clients_${traceFile}_sends_12.11.ini
            sed -i 's/SenderOffset = .*/SenderOffset = '"$offset"'/g' $1/config/trace_pilot_clients_${traceFile}_sends_12.11.ini
        done
        sed -i 's/<WorkerThreads>.*<\/WorkerThreads>/<WorkerThreads>'"$workers"'<\/WorkerThreads>/g' $1/config/config-server.xml
        sed -i 's/<ClientsPerWorker>.*<\/ClientsPerWorker>/<ClientsPerWorker>'"$clientsPerWorker"'<\/ClientsPerWorker>/g' $1/config/config-server.xml
        sed -i 's/<DatabaseConnections>.*<\/DatabaseConnections>/<DatabaseConnections>'"$workers"'<\/DatabaseConnections>/g' $1/config/config-server.xml
        #cat $1/config/config-server.xml
        #echo "~/logs/log-$clientsPerWorker-$workers-1"
        $1/tools/amazon_tools/run_experiment_cycle.sh $1/tools/amazon_tools/ ~/logs/log-$clientsPerWorker-$workers-5
    done
done
