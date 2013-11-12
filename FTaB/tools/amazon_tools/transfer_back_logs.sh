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

mkdir -p $1

for instance in $SERVER_A $SERVER_B $SERVER_C $SERVER_D $SERVER_E $CLIENT_A $CLIENT_B $CLIENT_C $CLIENT_D $CLIENT_E
do
    ssh $instance "tar -czf logs.tgz logs/*"
    scp ${instance}:logs.tgz ${1}/logs-${instance}.tgz
done
