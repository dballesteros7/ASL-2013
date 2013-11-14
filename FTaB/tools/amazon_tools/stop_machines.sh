#!/bin/bash
DB_INSTANCE="ec2-54-194-13-218.eu-west-1.compute.amazonaws.com"
SERVER_A="ec2-54-194-35-0.eu-west-1.compute.amazonaws.com"
SERVER_B="ec2-54-194-22-131.eu-west-1.compute.amazonaws.com"
CLIENT_A="ec2-54-194-13-53.eu-west-1.compute.amazonaws.com"

# Stop the servers
ssh $SERVER_A 'PID=$(cat server.pid); kill $PID'
ssh $SERVER_B 'PID=$(cat server.pid); kill $PID'

sleep 30

# Stop the database
ssh $DB_INSTANCE "/usr/pgsql-9.3/bin/pg_ctl -D postgresql_data/ stop"
