#!/bin/bash

COMMAND=$1
if [ "$COMMAND" == "deploy" ]
then
    mkdir ~/postgresql/
    cp -r /mnt/asl/user25/postgresql ~/
    exit $?
else
    hash pg_ctl 2>/dev/null
    PATH_READY=$? 
    if [ $PATH_READY -ne 0 ]
    then
	echo "No path available, check the PATH".
        exit 1
    else 
        if [ "$COMMAND" == "start" ]
        then
        pg_ctl -D ~/postgresql/data -l ~/postgresql/log/serverlog.log start
        exit $?
        else
            if [ "$COMMAND" == "stop" ]
            then
            pg_ctl -D ~/postgresql/data -l ~/postgresql/log/serverlog.log stop
            exit $?
	    elif [ "$COMMAND" = "store" ]
	    then
	    	if [ -e ~/postgresql/data/postmaster.pid ]
		then
		echo "Stop the server before storing it."
		exit 1
		else
		cp -r ~/postgresql /mnt/asl/user25/
		rm -rf ~/postgresql
		fi
            fi 
        fi
    fi
fi
exit 1
