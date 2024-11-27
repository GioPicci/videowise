#!/bin/bash


echo "OLLAMA_API_URL is set to $OLLAMA_API_URL"
echo "FILESYSTEM_API_URL is set to $FILESYSTEM_API_URL"
echo "WHISPER_API_URL is set to $WHISPER_API_URL"
echo "QUARKUS_DATASOURCE_JDBC_URL is set to $QUARKUS_DATASOURCE_JDBC_URL"

# Run the Java application
java -jar /app/quarkus-run.jar
