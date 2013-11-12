#!/bin/bash
DB_INSTANCE="ec2-54-194-28-204.eu-west-1.compute.amazonaws.com"
SERVER_A="ec2-54-194-28-29.eu-west-1.compute.amazonaws.com"
SERVER_B="ec2-54-194-28-202.eu-west-1.compute.amazonaws.com"
CLIENT_A="ec2-54-194-26-44.eu-west-1.compute.amazonaws.com"

# Stop the servers
ssh $SERVER_A 'PID=$(cat server.pid); kill $PID'
ssh $SERVER_B 'PID=$(cat server.pid); kill $PID'

sleep 30

# Stop the database
ssh $DB_INSTANCE "/usr/pgsql-9.3/bin/pg_ctl -D postgresql_data/ stop"
