# Docker multi-stage build

# 1. Building the App with Maven
FROM maven:3.8.7-openjdk-18-slim AS builder

ADD . /worknote
WORKDIR /worknote

# Install Python3
RUN apt-get update && apt-get install python3 -y

# Set PATH env var
ENV PATH="/usr/bin/python3:${PATH}"

# Just echo so we can see, if everything is there :)
RUN ls -l

# Run Maven build
RUN mvn clean install


# Just using the build artifact and then removing the build-container
FROM openjdk:19-slim

MAINTAINER e1sordo

VOLUME /tmp

# Add Spring Boot app.jar to Container
COPY --from=builder "/worknote/backend/target/backend-0.0.1-SNAPSHOT.jar" app.jar

# Set the working directory
WORKDIR /

# Add the entrypoint script
COPY entrypoint.sh /

# Make the entrypoint script executable
RUN chmod +x /entrypoint.sh

ENV TZ=Europe/Moscow

# Define the command to run when the container starts
ENTRYPOINT ["/entrypoint.sh"]
