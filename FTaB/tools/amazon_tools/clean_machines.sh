#!/bin/bash
DB_INSTANCE="ec2-54-194-27-120.eu-west-1.compute.amazonaws.com"
SERVER_A="ec2-54-194-31-15.eu-west-1.compute.amazonaws.com"
SERVER_B="ec2-54-194-30-206.eu-west-1.compute.amazonaws.com"
CLIENT_A="ec2-54-194-1-47.eu-west-1.compute.amazonaws.com"
CLIENT_B="ec2-54-194-31-21.eu-west-1.compute.amazonaws.com"

# Delete everything on the servers
for server in $DB_INSTANCE $SERVER_A $SERVER_B $CLIENT_A $CLIENT_B
do
    echo $server
    ssh $server "rm -rf *"
done
