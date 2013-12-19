#!/bin/bash

SERVER_A="dryad02.ethz.ch"
SERVER_B="dryad03.ethz.ch"
SERVER_C="dryad04.ethz.ch"
SERVER_D="dryad05.ethz.ch"
SERVER_E="dryad06.ethz.ch"
CLIENT="dryad07.ethz.ch"

SERVER_COUNT=2

for THREAD_COUNT in 2 5 10
do
    for USER_COUNT in 20 40 60 100
    do
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_A "bash /home/user25/tools/milestone_2_dryad/modify_trace_server_msmt.sh $THREAD_COUNT $USER_COUNT $SERVER_COUNT"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_B "bash /home/user25/tools/milestone_2_dryad/modify_trace_server_msmt.sh $THREAD_COUNT $USER_COUNT $SERVER_COUNT"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_A "bash /home/user25/tools/milestone_2_dryad/start_server.sh"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_B "bash /home/user25/tools/milestone_2_dryad/start_server.sh"
        sleep 60
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $CLIENT "bash /home/user25/tools/milestone_2_dryad/modify_trace_client_msmt.sh $USER_COUNT $SERVER_COUNT"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $CLIENT "bash /home/user25/tools/milestone_2_dryad/start_client.sh"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_A "bash /home/user25/tools/milestone_2_dryad/stop_server.sh"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_B "bash /home/user25/tools/milestone_2_dryad/stop_server.sh"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $CLIENT "bash /home/user25/tools/milestone_2_dryad/tar_logs.sh trace_19_12_${THREAD_COUNT}_${USER_COUNT}.tgz"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_A "bash /home/user25/tools/milestone_2_dryad/tar_logs.sh trace_19_12_${THREAD_COUNT}_${USER_COUNT}_${SERVER_COUNT}_1.tgz"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_B "bash /home/user25/tools/milestone_2_dryad/tar_logs.sh trace_19_12_${THREAD_COUNT}_${USER_COUNT}_${SERVER_COUNT}_2.tgz"
    done
done

SERVER_COUNT=5

for THREAD_COUNT in 2 4 8 
do
    for USER_COUNT in 20 40 60 100
    do
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_A "bash /home/user25/tools/milestone_2_dryad/modify_trace_server_msmt.sh $THREAD_COUNT $USER_COUNT $SERVER_COUNT"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_B "bash /home/user25/tools/milestone_2_dryad/modify_trace_server_msmt.sh $THREAD_COUNT $USER_COUNT $SERVER_COUNT"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_C "bash /home/user25/tools/milestone_2_dryad/modify_trace_server_msmt.sh $THREAD_COUNT $USER_COUNT $SERVER_COUNT"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_D "bash /home/user25/tools/milestone_2_dryad/modify_trace_server_msmt.sh $THREAD_COUNT $USER_COUNT $SERVER_COUNT"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_E "bash /home/user25/tools/milestone_2_dryad/modify_trace_server_msmt.sh $THREAD_COUNT $USER_COUNT $SERVER_COUNT"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_A "bash /home/user25/tools/milestone_2_dryad/start_server.sh"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_B "bash /home/user25/tools/milestone_2_dryad/start_server.sh"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_C "bash /home/user25/tools/milestone_2_dryad/start_server.sh"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_D "bash /home/user25/tools/milestone_2_dryad/start_server.sh"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_E "bash /home/user25/tools/milestone_2_dryad/start_server.sh"
        sleep 60
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $CLIENT "bash /home/user25/tools/milestone_2_dryad/modify_trace_client_msmt.sh $USER_COUNT $SERVER_COUNT"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $CLIENT "bash /home/user25/tools/milestone_2_dryad/start_client.sh"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_A "bash /home/user25/tools/milestone_2_dryad/stop_server.sh"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_B "bash /home/user25/tools/milestone_2_dryad/stop_server.sh"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_C "bash /home/user25/tools/milestone_2_dryad/stop_server.sh"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_D "bash /home/user25/tools/milestone_2_dryad/stop_server.sh"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_E "bash /home/user25/tools/milestone_2_dryad/stop_server.sh"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $CLIENT "bash /home/user25/tools/milestone_2_dryad/tar_logs.sh trace_19_12_${THREAD_COUNT}_${USER_COUNT}_${SERVER_COUNT}.tgz"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_A "bash /home/user25/tools/milestone_2_dryad/tar_logs.sh trace_19_12_${THREAD_COUNT}_${USER_COUNT}_${SERVER_COUNT}_1.tgz"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_B "bash /home/user25/tools/milestone_2_dryad/tar_logs.sh trace_19_12_${THREAD_COUNT}_${USER_COUNT}_${SERVER_COUNT}_2.tgz"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_C "bash /home/user25/tools/milestone_2_dryad/tar_logs.sh trace_19_12_${THREAD_COUNT}_${USER_COUNT}_${SERVER_COUNT}_3.tgz"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_D "bash /home/user25/tools/milestone_2_dryad/tar_logs.sh trace_19_12_${THREAD_COUNT}_${USER_COUNT}_${SERVER_COUNT}_4.tgz"
        ssh -o UserKnownHostsFile=/home/user25/network/known_hosts -i /home/user25/network/asl-key-nopwd $SERVER_E "bash /home/user25/tools/milestone_2_dryad/tar_logs.sh trace_19_12_${THREAD_COUNT}_${USER_COUNT}_${SERVER_COUNT}_5.tgz"
    done
done
