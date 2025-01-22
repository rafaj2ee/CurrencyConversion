# Use a base image with JDK 8
FROM openjdk:8-jdk-alpine

# Environment variable for PostgreSQL password
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres

# Install PostgreSQL
RUN apk update && \
    apk add postgresql postgresql-contrib && \
    mkdir -p /var/lib/postgresql/data && \
    chown -R postgres:postgres /var/lib/postgresql/data && \
    su postgres -c "initdb -D /var/lib/postgresql/data"

# Add a volume pointing to the /tmp directory
VOLUME /tmp

# Expose the ports the application will run on
EXPOSE 5432 9090

# The 'jar' argument that can be passed during the Docker image build
ARG JAR_FILE=target/*.jar

# Copy the JAR file to the Docker container
COPY ${JAR_FILE} app.jar

# Change JAR file permissions 
RUN chmod +x app.jar

# Startup script
COPY start.sh /start.sh
RUN chmod +x /start.sh

# Configuration to start PostgreSQL and Spring Boot
CMD ["/start.sh"]