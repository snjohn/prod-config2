# syntax=docker/dockerfile:1
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent pom first
COPY pom.xml ./pom.xml

# Copy module pom.xml and download dependencies (layer caching)
COPY backend/pom.xml ./backend/pom.xml
COPY backend/.mvn ./backend/.mvn
COPY backend/mvnw ./backend/mvnw
WORKDIR /app/backend
RUN chmod +x mvnw
RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline -B

# Copy source code and build
COPY backend/src ./src
RUN --mount=type=cache,target=/root/.m2 ./mvnw clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the JAR from build stage
COPY --from=build /app/backend/target/*.jar app.jar

# Expose port (default Spring Boot port)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
