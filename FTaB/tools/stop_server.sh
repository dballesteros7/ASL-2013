#!/bin/bash

if [[ -e "server.pid" ]]
then
    PID=`cat server.pid`
    kill $PID
    wait $PID
    echo $?
    echo "Done server."
else
    echo "No server running"
fi
