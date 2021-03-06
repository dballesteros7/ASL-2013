#!/bin/bash
set -e
# The first argument provides the directory where all the "amazon tools" are located.
# The second argument provides the directory where the logs should be stored.

bash $1/start_experiment.sh $1/../../../

sleep 7800

bash $1/stop_machines.sh

sleep 60

bash $1/transfer_back_logs.sh $2

sleep 30

bash $1/clean_machines.sh
