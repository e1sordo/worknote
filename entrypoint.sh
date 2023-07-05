#!/bin/sh
set -e

export DB_PATH=../app/data.db

# Run the Java application
exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar
