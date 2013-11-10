#!/bin/bash
set -e
# For each parameter, we edit the trace config files accordingly and run a experiment_cycle.
for charNumber in 100 2000
do
    for queues in 1 10
    do
        for senders in 120 250
        do
            for readers in 120 250
            do
                sed -i 's/ReaderNumber = .*/ReaderNumber = '"$readers"'/g' $1/config/trace_pilot_clients_1_reads_10.11.ini
                sed -i 's/ReaderNumber = .*/ReaderNumber = '"$readers"'/g' $1/config/trace_pilot_clients_2_reads_10.11.ini
                sed -i 's/ReaderOffset = .*/ReaderOffset = '"$readers"'/g' $1/config/trace_pilot_clients_2_reads_10.11.ini
                sed -i 's/SenderNumber = .*/SenderNumber = '"$senders"'/g' $1/config/trace_pilot_clients_1_sends_10.11.ini
                sed -i 's/SenderNumber = .*/SenderNumber = '"$senders"'/g' $1/config/trace_pilot_clients_2_sends_10.11.ini
                sed -i 's/SenderOffset = .*/SenderOffset = '"$senders"'/g' $1/config/trace_pilot_clients_2_sends_10.11.ini
                sed -i 's/MessageSize = .*/MessageSize = '"$charNumber"'/g' $1/config/trace_pilot_clients_1_sends_10.11.ini
                sed -i 's/MessageSize = .*/MessageSize = '"$charNumber"'/g' $1/config/trace_pilot_clients_2_sends_10.11.ini
                sed -i 's/NumberOfQueues = .*/NumberOfQueues = '"$queues"'/g' $1/config/trace_pilot_clients_1_sends_10.11.ini
                sed -i 's/NumberOfQueues = .*/NumberOfQueues = '"$queues"'/g' $1/config/trace_pilot_clients_2_sends_10.11.ini
                $1/tools/amazon_tools/run_experiment_cycle.sh $1/tools/amazon_tools/ ~/logs/log-$charNumber-$queues-$senders-$readers
            done
        done
    done
done
