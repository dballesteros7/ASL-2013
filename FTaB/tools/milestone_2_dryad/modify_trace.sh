#!/bin/bash
sed -i 's/readernumber = .*/readernumber = '"$1"'/g' /home/user25/config/milestone_2/trace_06_12.ini
sed -i 's/sendernumber = .*/sendernumber = '"$1"'/g' /home/user25/config/milestone_2/trace_06_12.ini
sed -i 's/WAIT_TIME = .*/WAIT_TIME = '"$2"'/g' /home/user25/python/Milestone2/Constants.py
