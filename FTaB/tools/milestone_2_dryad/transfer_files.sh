#!/bin/bash

scp -r dist/ user25@$1.ethz.ch:
scp -r python/ user25@$1.ethz.ch:
scp -r config/ user25@$1.ethz.ch:
scp -r tools/ user25@$1.ethz.ch:
