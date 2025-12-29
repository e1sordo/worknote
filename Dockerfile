# Just using the build artifact and then removing the build-container
FROM openjdk:19-slim

LABEL maintainer="e1sordo"

# Add Spring Boot app.jar to Container
COPY backend/target/backend-1.0.0.jar app.jar

# Set the working directory
WORKDIR /

# Add the entrypoint script
COPY entrypoint.sh /

# Make the entrypoint script executable
RUN chmod +x /entrypoint.sh

ENV TZ=Europe/Moscow

# Define the command to run when the container starts
ENTRYPOINT ["/entrypoint.sh"]
