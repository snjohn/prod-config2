# syntax=docker/dockerfile:1
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent pom first
COPY pom.xml ./pom.xml

# Copy module pom.xml and download dependencies (layer caching)
COPY eurekaserver/pom.xml ./eurekaserver/pom.xml
WORKDIR /app/eurekaserver
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

# Copy source code and build
COPY eurekaserver/src ./src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the JAR from build stage
COPY --from=build /app/eurekaserver/target/*.jar app.jar

# Expose Eureka port
EXPOSE 8761

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
