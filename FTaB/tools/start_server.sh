#!/bin/bash
java -jar FTaB.jar config/config-server.xml &> logs/system.log &
PID=$!
echo $PID > server.pid
