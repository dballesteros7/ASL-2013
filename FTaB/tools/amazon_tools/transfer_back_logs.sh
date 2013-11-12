#!/bin/bash
DB_INSTANCE="ec2-54-194-28-204.eu-west-1.compute.amazonaws.com"
SERVER_A="ec2-54-194-28-29.eu-west-1.compute.amazonaws.com"
SERVER_B="ec2-54-194-28-202.eu-west-1.compute.amazonaws.com"
CLIENT_A="ec2-54-194-26-44.eu-west-1.compute.amazonaws.com"

mkdir -p $1

for instance in $SERVER_A $SERVER_B $CLIENT_A
do
    ssh $instance "tar -czf logs.tgz logs/*"
    scp ${instance}:logs.tgz ${1}/logs-${instance}.tgz
done
