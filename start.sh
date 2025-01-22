#!/bin/sh

# Start PostgreSQL
su postgres -c 'pg_ctl start -D /var/lib/postgresql/data -l /var/lib/postgresql/data/logfile'

# Wait for PostgreSQL to be ready
until su postgres -c 'pg_isready -d /var/lib/postgresql/data'; do
  echo "Waiting for PostgreSQL to start..."
  sleep 2
done

echo "PostgreSQL started successfully"

# Print PostgreSQL log for debugging
cat /var/lib/postgresql/data/logfile

# Start the Spring Boot application
java -jar /app.jar
