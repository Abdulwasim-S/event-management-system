# Use official OpenJDK 17 runtime image
FROM eclipse-temurin:17-jdk-jammy

# Set working directory in the container
WORKDIR /app

# Copy the packaged jar file (assuming it's named app.jar)
COPY target/*.jar app.jar

# Expose port (Spring Boot default is 8080)
EXPOSE 8080

# Command to run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
