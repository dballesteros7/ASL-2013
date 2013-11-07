#!/bin/bash

scp -i ~/.ssh/asl-key-nopwd ~/workspace/ASL-2013/FTaB/FTaB.jar user25@$1.ethz.ch:
scp -ri ~/.ssh/asl-key-nopwd ~/workspace/ASL-2013/FTaB/python user25@$1.ethz.ch:
scp -ri ~/.ssh/asl-key-nopwd ~/workspace/ASL-2013/FTaB/config user25@$1.ethz.ch:
