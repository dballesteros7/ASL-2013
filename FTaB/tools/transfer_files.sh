#!/bin/bash

scp  FTaB/FTaB.jar user25@$1.ethz.ch:
scp -r FTaB/python user25@$1.ethz.ch:
scp -r FTaB/config user25@$1.ethz.ch:
scp -r FTaB/tools user25@$1.ethz.ch:
