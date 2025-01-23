# Use the openjdk:8-jdk base image
FROM openjdk:8-jdk

# Add a volume pointing to the /tmp directory
VOLUME /tmp

# Expose the port the application will run on
EXPOSE 9090

# The 'jar' argument that can be passed during the Docker image build
ARG JAR_FILE=*.jar

# Copy the JAR file to the Docker container
COPY ${JAR_FILE} app.jar

# Change JAR file permissions
RUN chmod +x app.jar

# Configuration to start the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app.jar"]
