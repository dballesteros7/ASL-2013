#!/bin/bash
DB_INSTANCE="ec2-54-194-13-218.eu-west-1.compute.amazonaws.com"
SERVER_A="ec2-54-194-35-0.eu-west-1.compute.amazonaws.com"
SERVER_B="ec2-54-194-22-131.eu-west-1.compute.amazonaws.com"
CLIENT_A="ec2-54-194-13-53.eu-west-1.compute.amazonaws.com"

mkdir -p $1

for instance in $DB_INSTANCE $SERVER_A $SERVER_B $CLIENT_A
do
    ssh $instance "tar -czf logs.tgz logs/*"
    scp ${instance}:logs.tgz ${1}/logs-${instance}.tgz
done
