#!/bin/bash
DB_INSTANCE="ec2-54-194-13-218.eu-west-1.compute.amazonaws.com"
SERVER_A="ec2-54-194-35-0.eu-west-1.compute.amazonaws.com"
SERVER_B="ec2-54-194-22-131.eu-west-1.compute.amazonaws.com"
CLIENT_A="ec2-54-194-13-53.eu-west-1.compute.amazonaws.com"

# Create a tarred distribution of the project
cd $1
tar -czf FTaB.tgz FTaB/
cd -

# Copy the necessary files to the servers
for server in $DB_INSTANCE $SERVER_A $SERVER_B $CLIENT_A
do
    echo $server
    scp $1/FTaB.tgz ${server}:
    ssh $server "tar -xzf FTaB.tgz"
done

# Setup the binaries
ssh -t $DB_INSTANCE "source FTaB/tools/amazon_tools/setup_db.sh"
ssh $DB_INSTANCE "source FTaB/tools/amazon_tools/setup_jython.sh"
ssh $CLIENT_A "source FTaB/tools/amazon_tools/setup_jython.sh"

# Setup the schema
ssh $DB_INSTANCE "jython-2.5.3/bin/jython -Dpython.path=FTaB/FTaB.jar:FTaB/python FTaB/python/Deploy/DatabaseDeploy.py Create FTaB/config/dbconnect.ini"
ssh $DB_INSTANCE "jython-2.5.3/bin/jython -Dpython.path=FTaB/FTaB.jar:FTaB/python FTaB/python/Scenarios/InitializedSystemNoMessages.py FTaB/config/dbconnect.ini"

# Setup the DB monitoring
ssh $DB_INSTANCE "jython-2.5.3/bin/jython -Dpython.path=FTaB/FTaB.jar:FTaB/python FTaB/python/Monitoring/DatabaseMonitor.py FTaB/config/dbwatchdog.ini &> logs/system.log &"

# Setup the servers
ssh -t $SERVER_A "source FTaB/tools/amazon_tools/setup_server.sh"
ssh -t $SERVER_B "source FTaB/tools/amazon_tools/setup_server.sh"

# Start the servers
ssh $SERVER_A "bash FTaB/tools/amazon_tools/start_server.sh"
ssh $SERVER_B "bash FTaB/tools/amazon_tools/start_server.sh"

sleep 30

# Setup the clients
ssh $CLIENT_A "bash FTaB/tools/amazon_tools/setup_client.sh"

# Start the clients
ssh $CLIENT_A "bash FTaB/tools/amazon_tools/start_client.sh"
