#!/bin/bash

jython -Dpython.path=FTaB.jar:python/ python/Load/2KExperiments/LoadParameters/RunSenders.py config/trace_test_send.ini &
jython -Dpython.path=FTaB.jar:python/ python/Load/2KExperiments/LoadParameters/RunReaders.py config/trace_test_reads.ini &
