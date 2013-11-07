#!/bin/bash

scp  FTaB.jar user25@$1.ethz.ch:
scp -r python/ user25@$1.ethz.ch:
scp -r config/ user25@$1.ethz.ch:
scp -r tools/ user25@$1.ethz.ch:
