#!/bin/bash
cd logs/
tar -czf $1 *log
rm *log
