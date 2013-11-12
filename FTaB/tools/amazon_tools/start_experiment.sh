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

# Create a tarred distribution of the project
cd $1
tar -czf FTaB.tgz FTaB/
cd -

# Copy the necessary files to the servers
for server in $DB_INSTANCE $SERVER_A $SERVER_B $SERVER_C $SERVER_D $SERVER_E $CLIENT_A $CLIENT_B $CLIENT_C $CLIENT_D $CLIENT_E
do
    echo $server
    scp $1/FTaB.tgz ${server}:
    ssh $server "tar -xzf FTaB.tgz"
done

# Setup the binaries
ssh -t $DB_INSTANCE "source FTaB/tools/amazon_tools/setup_db.sh"
ssh $DB_INSTANCE "source FTaB/tools/amazon_tools/setup_jython.sh"
ssh $CLIENT_A "source FTaB/tools/amazon_tools/setup_jython.sh"
ssh $CLIENT_B "source FTaB/tools/amazon_tools/setup_jython.sh"
ssh $CLIENT_C "source FTaB/tools/amazon_tools/setup_jython.sh"
ssh $CLIENT_D "source FTaB/tools/amazon_tools/setup_jython.sh"
ssh $CLIENT_E "source FTaB/tools/amazon_tools/setup_jython.sh"

# Setup the schema
ssh $DB_INSTANCE "jython-2.5.3/bin/jython -Dpython.path=FTaB/FTaB.jar:FTaB/python FTaB/python/Deploy/DatabaseDeploy.py Create FTaB/config/dbconnect.ini"
ssh $DB_INSTANCE "jython-2.5.3/bin/jython -Dpython.path=FTaB/FTaB.jar:FTaB/python FTaB/python/Scenarios/InitializedSystemNoMessages.py FTaB/config/dbconnect.ini"

# Setup the servers
ssh -t $SERVER_A "source FTaB/tools/amazon_tools/setup_server.sh"
ssh -t $SERVER_B "source FTaB/tools/amazon_tools/setup_server.sh"
ssh -t $SERVER_C "source FTaB/tools/amazon_tools/setup_server.sh"
ssh -t $SERVER_D "source FTaB/tools/amazon_tools/setup_server.sh"
ssh -t $SERVER_E "source FTaB/tools/amazon_tools/setup_server.sh"

# Start the servers
ssh $SERVER_A "bash FTaB/tools/amazon_tools/start_server.sh"
ssh $SERVER_B "bash FTaB/tools/amazon_tools/start_server.sh"
ssh $SERVER_C "bash FTaB/tools/amazon_tools/start_server.sh"
ssh $SERVER_D "bash FTaB/tools/amazon_tools/start_server.sh"
ssh $SERVER_E "bash FTaB/tools/amazon_tools/start_server.sh"

sleep 30

# Setup the clients
ssh $CLIENT_A "bash FTaB/tools/amazon_tools/setup_client.sh"
ssh $CLIENT_B "bash FTaB/tools/amazon_tools/setup_client.sh"
ssh $CLIENT_C "bash FTaB/tools/amazon_tools/setup_client.sh"
ssh $CLIENT_D "bash FTaB/tools/amazon_tools/setup_client.sh"
ssh $CLIENT_E "bash FTaB/tools/amazon_tools/setup_client.sh"

# Start the clients
ssh $CLIENT_A "bash FTaB/tools/amazon_tools/start_client.sh 1"
ssh $CLIENT_B "bash FTaB/tools/amazon_tools/start_client.sh 2"
ssh $CLIENT_C "bash FTaB/tools/amazon_tools/start_client.sh 3"
ssh $CLIENT_D "bash FTaB/tools/amazon_tools/start_client.sh 4"
ssh $CLIENT_E "bash FTaB/tools/amazon_tools/start_client.sh 5"
