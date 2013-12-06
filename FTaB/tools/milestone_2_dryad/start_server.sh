#!/bin/bash
java -jar dist/FTaB-server.jar config/milestone_2/config-server.xml &> logs/system.log &
PID=$!
echo $PID > server.pid
