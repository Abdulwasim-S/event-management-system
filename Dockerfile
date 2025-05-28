# Use official OpenJDK as base image
FROM openjdk:17-jdk-slim

# Set app directory inside the container
WORKDIR /app

# Copy the built jar file into the container
COPY target/event-management-system-0.0.1-SNAPSHOT.jar app.jar

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
