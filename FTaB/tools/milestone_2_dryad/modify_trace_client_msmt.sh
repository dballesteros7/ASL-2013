#!/bin/bash
sed -i 's/numberofservers = .*/numberofservers = '"$2"'/g' /home/user25/config/milestone_2/trace_19_12.ini
sed -i 's/readernumber = .*/readernumber = '"$1"'/g' /home/user25/config/milestone_2/trace_19_12.ini
sed -i 's/sendernumber = .*/sendernumber = '"$1"'/g' /home/user25/config/milestone_2/trace_19_12.ini
