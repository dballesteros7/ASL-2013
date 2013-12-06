#!/bin/bash

SERVER="dryad06.ethz.ch"
CLIENT="dryad07.ethz.ch"

USER_COUNT=10
FIXED_DISTRIBUTION_SIZE=0.2
for USER_COUNT in 5 10 20 30 40 50 70 80 100
do
    for FIXED_DISTRIBUTION_SIZE in 0.2 0.5 1 2
    do
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER "bash /home/user25/tools/milestone_2_dryad/start_server.sh"
        sleep 60
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $CLIENT "bash /home/user25/tools/milestone_2_dryad/modify_trace.sh $USER_COUNT $FIXED_DISTRIBUTION_SIZE"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $CLIENT "bash /home/user25/tools/milestone_2_dryad/start_client.sh"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER "bash /home/user25/tools/milestone_2_dryad/stop_server.sh"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $CLIENT "bash /home/user25/tools/milestone_2_dryad/tar_logs.sh trace_06_12_${USER_COUNT}_${FIXED_DISTRIBUTION_SIZE}.tgz"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER "bash /home/user25/tools/milestone_2_dryad/tar_logs.sh trace_06_12_${USER_COUNT}_${FIXED_DISTRIBUTION_SIZE}.tgz"
    done
done
