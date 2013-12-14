#!/bin/bash
sed -i 's/<WorkerThreads>.*<\/WorkerThreads>/<WorkerThreads>'"$1"'<\/WorkerThreads>/g' /home/user25/config/milestone_2/config-server.xml
USERS_PER_THREAD=$((2*$2/$1))
sed -i 's/<ClientsPerWorker>.*<\/ClientsPerWorker>/<ClientsPerWorker>'"$USERS_PER_THREAD"'<\/ClientsPerWorker>/g' /home/user25/config/milestone_2/config-server.xml
