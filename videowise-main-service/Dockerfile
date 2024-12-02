FROM gradle:8.9-jdk22 AS build

LABEL authors="giovanni.piccinini"

# Set the working directory
WORKDIR /app

# Copy the Gradle wrapper and the rest of the project
COPY . /app

# Grant execution permission to gradlew and build the app
RUN chmod +x gradlew && ./gradlew clean build -x test

# Use a smaller base image for running the service
FROM openjdk:22-jdk-slim

# Set the working directory for the final image
WORKDIR /app

# Copy the compiled jar from the build stage
COPY --from=build /app/build/quarkus-app /app

COPY --from=build /app/entrypoint.sh /app/

RUN chmod +x /app/entrypoint.sh

# ENV variables for the deploy of the standalone service, before build
# --- External Services ---
#ENV OLLAMA_API_URL="http://<your_ollama_ip>:11434/api/chat" 
#ENV QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://<your_db_ip>:5432/video_transcriptions_db" 
#ENV FILESYSTEM_API_URL="http://<your_fs_service_ip>:8081" 
#ENV FILESYSTEM_STREAMING_API_URL="http://<your_fs_service_ip>:8081" 
#ENV WHISPER_API_URL="http://<your_python_service_ip>:8000"
# --- WhisperX config ---
#ENV WHISPER_MODEL="large-v2" 
# --- Ollama config ---
#ENV OLLAMA_MODEL="llama3.1:latest" 
#ENV OLLAMA_CTX_LEN=16000 
#ENV OLLAMA_MAX_PRED_LEN=4000 

# Run the Java application
CMD ["/app/entrypoint.sh"]