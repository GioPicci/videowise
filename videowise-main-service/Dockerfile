FROM gradle:8.9-jdk22 AS build

LABEL authors="giovanni.piccinini"

# Set the working directory
WORKDIR /app

# Copy the Gradle wrapper and the rest of the project
COPY . /app

# ENV variables for the deploy of the standalone service, before build
#ENV OLLAMA_API_URL="http://192.168.202.45:11434/api/chat" \
#    QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://192.168.202.45:5432/video_transcriptions_db" \
#    FILESYSTEM_API_URL="http://192.168.202.45:8081" \
#    WHISPER_API_URL="http://192.168.202.45:8000"

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

# Expose the port your application will run on
# EXPOSE 8080

# ENV variables for the deploy of the standalone service, before launch
#ENV OLLAMA_API_URL="http://192.168.202.45:11434/api/chat" \
#    QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://192.168.202.45:5432/video_transcriptions_db" \
#    FILESYSTEM_API_URL="http://192.168.202.45:8081" \
#    WHISPER_API_URL="http://192.168.202.45:8000"

# Run the Java application
CMD ["/app/entrypoint.sh"]