#!/bin/bash
mkdir logs
sudo iptables -I INPUT -i eth0 -p tcp -m tcp --dport 34582 -j ACCEPT
