# Dockerfile for the Spring Boot Todo API.
# Jenkins will first run: mvn clean package
# Then this image copies the built JAR from target/ into a small Java 17 runtime image.

FROM eclipse-temurin:17-jre

# Keep application files in one directory inside the container.
WORKDIR /app

# Copy the Spring Boot executable JAR produced by Maven.
# The wildcard keeps the Dockerfile working even if the project version changes later.
COPY target/*.jar app.jar

# Spring Boot runs on port 8080 by default.
EXPOSE 8080

# Start the API when the container starts.
ENTRYPOINT ["java", "-jar", "app.jar"]
