#!/bin/bash
DB_INSTANCE="ec2-54-194-28-204.eu-west-1.compute.amazonaws.com"
SERVER_A="ec2-54-194-28-29.eu-west-1.compute.amazonaws.com"
SERVER_B="ec2-54-194-28-202.eu-west-1.compute.amazonaws.com"
CLIENT_A="ec2-54-194-26-44.eu-west-1.compute.amazonaws.com"

# Delete everything on the servers
for server in $DB_INSTANCE $SERVER_A $SERVER_B $CLIENT_A
do
    echo $server
    ssh -t $server "mkdir /home/ec2-user/instance_store; sudo mount -t ext3 /dev/xvdf /home/ec2-user/instance_store/;sudo chown -R ec2-user:ec2-user /home/ec2-user/instance_store"
done
