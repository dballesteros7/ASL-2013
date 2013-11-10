#!/bin/bash
java -jar FTaB/FTaB.jar FTaB/config/config-server.xml &> logs/system.log &
PID=$!
echo $PID > server.pid
