#!/bin/bash

# Install the PostgreSQL server
sudo rpm -iv pgdg-redhat93-9.3-1.noarch.rpm
sudo yum -qy install postgresql93-server.x86_64
mkdir postgresql_data
mkdir logs

# Init and start the database server
/usr/pgsql-9.3/bin/pg_ctl -D postgresql_data/ initdb
sed -i "s/#listen_addresses = 'localhost'/listen_addresses = '*'/g" postgresql_data/postgresql.conf
/usr/pgsql-9.3/bin/pg_ctl -D postgresql_data/ -l logs/database.log start

# Create the main database and set appropriate permissions
# Requires the create_db.sql file
sleep 5
psql -d postgres -f create_db.sql
