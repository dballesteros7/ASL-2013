#!/bin/bash
wget http://search.maven.org/remotecontent?filepath=org/python/jython-installer/2.5.3/jython-installer-2.5.3.jar -O jython-installer-2.5.3.jar
java -jar jython-installer-2.5.3.jar -s -d jython-2.5.3/
export PATH=$PATH:~/jython-2.5.3/bin/
