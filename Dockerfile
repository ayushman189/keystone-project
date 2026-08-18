# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy Maven wrapper and pom first to leverage Docker layer caching
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Download dependencies (cached unless pom.xml changes)
RUN mvn -B dependency:go-offline

# Copy source and build the Spring Boot JAR (skip tests for a faster, reliable build)
COPY src src
RUN mvn -B -DskipTests clean package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as a non-root user for security
RUN groupadd -r keystone && useradd -r -g keystone keystone

# Copy the built JAR from the build stage
COPY --from=build /app/target/keystone-backend-0.0.1-SNAPSHOT.jar app.jar

USER keystone

# Render provides PORT; 8080 is the local/default fallback
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]