#!/bin/bash
DB_INSTANCE="ec2-54-194-32-29.eu-west-1.compute.amazonaws.com"
SERVER_A="ec2-54-194-32-46.eu-west-1.compute.amazonaws.com"
SERVER_B="ec2-54-194-31-26.eu-west-1.compute.amazonaws.com"
SERVER_C="ec2-54-194-4-85.eu-west-1.compute.amazonaws.com"
SERVER_D="ec2-54-194-10-100.eu-west-1.compute.amazonaws.com"
SERVER_E="ec2-54-194-1-70.eu-west-1.compute.amazonaws.com"
CLIENT_A="ec2-54-194-30-189.eu-west-1.compute.amazonaws.com"
CLIENT_B="ec2-54-194-32-70.eu-west-1.compute.amazonaws.com"
CLIENT_C="ec2-54-194-31-139.eu-west-1.compute.amazonaws.com"
CLIENT_D="ec2-54-194-31-192.eu-west-1.compute.amazonaws.com"
CLIENT_E="ec2-54-194-14-135.eu-west-1.compute.amazonaws.com"

# Stop the servers
ssh $SERVER_A 'PID=$(cat server.pid); kill $PID'
ssh $SERVER_B 'PID=$(cat server.pid); kill $PID'
ssh $SERVER_C 'PID=$(cat server.pid); kill $PID'
ssh $SERVER_D 'PID=$(cat server.pid); kill $PID'
ssh $SERVER_E 'PID=$(cat server.pid); kill $PID'

sleep 30

# Stop the database
ssh $DB_INSTANCE "/usr/pgsql-9.3/bin/pg_ctl -D postgresql_data/ stop"
