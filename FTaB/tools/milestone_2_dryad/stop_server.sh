#!/bin/bash

if [[ -e "server.pid" ]]
then
    PID=`cat server.pid`
    kill $PID
    sleep 60
    echo "Done server."
else
    echo "No server running"
fi
