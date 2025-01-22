#!/bin/sh

# Initialize PostgreSQL data directory
su postgres -c '/usr/lib/postgresql/12/bin/initdb -D /var/lib/postgresql/data'

# Start PostgreSQL
su postgres -c '/usr/lib/postgresql/12/bin/pg_ctl -D /var/lib/postgresql/data -l /var/lib/postgresql/data/logfile start'

# Wait for PostgreSQL to be ready
until su postgres -c '/usr/lib/postgresql/12/bin/pg_isready -h localhost -p 5432'; do
  echo "Waiting for PostgreSQL to start..."
  sleep 2
done

echo "PostgreSQL started successfully"

# Print PostgreSQL log for debugging
cat /var/lib/postgresql/data/logfile

# Start the Spring Boot application
java -jar /app.jar
