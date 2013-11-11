#!/bin/bash
DB_INSTANCE="ec2-54-194-33-48.eu-west-1.compute.amazonaws.com"
SERVER_A="ec2-54-194-33-19.eu-west-1.compute.amazonaws.com"
#SERVER_B="ec2-54-194-33-62.eu-west-1.compute.amazonaws.com"
CLIENT_A="ec2-54-194-33-6.eu-west-1.compute.amazonaws.com"
CLIENT_B="ec2-54-194-33-21.eu-west-1.compute.amazonaws.com"
CLIENT_C="ec2-54-194-32-171.eu-west-1.compute.amazonaws.com"
CLIENT_D="ec2-54-194-32-228.eu-west-1.compute.amazonaws.com"
CLIENT_E="ec2-54-194-5-224.eu-west-1.compute.amazonaws.com"

# Stop the servers
ssh $SERVER_A 'PID=$(cat server.pid); kill $PID'
#ssh $SERVER_B 'PID=$(cat server.pid); kill $PID'

sleep 30

# Stop the database
ssh $DB_INSTANCE "/usr/pgsql-9.3/bin/pg_ctl -D postgresql_data/ stop"
