#!/bin/bash


echo "OLLAMA_API_URL is set to $OLLAMA_API_URL"
echo "FILESYSTEM_API_URL is set to $FILESYSTEM_API_URL"
echo "WHISPER_API_URL is set to $WHISPER_API_URL"
echo "QUARKUS_DATASOURCE_JDBC_URL is set to $QUARKUS_DATASOURCE_JDBC_URL"
echo "WHISPER_MODEL is set to $WHISPER_MODEL"
echo "OLLAMA_MODEL is set to $OLLAMA_MODEL"
echo "OLLAMA_CTX_LEN is set to $OLLAMA_CTX_LEN"
echo "OLLAMA_MAX_PRED_LEN is set to $OLLAMA_MAX_PRED_LEN"

# Run the Java application
java -jar /app/quarkus-run.jar
