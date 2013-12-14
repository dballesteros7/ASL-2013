#!/bin/bash

SERVER="dryad06.ethz.ch"
CLIENT="dryad07.ethz.ch"

for THREAD_COUNT in 2 5 10 20
do
    for USER_COUNT in 20 40 60 100
    do
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER "bash /home/user25/tools/milestone_2_dryad/modify_trace_server_ssmt.sh $THREAD_COUNT $USER_COUNT"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER "bash /home/user25/tools/milestone_2_dryad/start_server.sh"
        sleep 60
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $CLIENT "bash /home/user25/tools/milestone_2_dryad/modify_trace_client_ssmt.sh $USER_COUNT"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $CLIENT "bash /home/user25/tools/milestone_2_dryad/start_client.sh"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER "bash /home/user25/tools/milestone_2_dryad/stop_server.sh"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $CLIENT "bash /home/user25/tools/milestone_2_dryad/tar_logs.sh trace_06_12_${THREAD_COUNT}_${USER_COUNT}.tgz"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER "bash /home/user25/tools/milestone_2_dryad/tar_logs.sh trace_06_12_${THREAD_COUNT}_${USER_COUNT}.tgz"
    done
done
