# syntax=docker/dockerfile:1

# ---------- Stage 1: build the JAR with Maven + full JDK ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy only the POM first and download dependencies. This layer is cached and only
# rebuilt when pom.xml changes, so editing source code does not re-download everything.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Tests already ran in the Jenkins "Test" stage, so skip them here
RUN mvn -B -q package -DskipTests


# ---------- Stage 2: slim runtime with only a JRE and the JAR ----------
FROM eclipse-temurin:21-jre-alpine

# Run as an unprivileged user instead of root
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app
COPY --from=build --chown=spring:spring /workspace/target/calculator-devops.jar app.jar

USER spring:spring

EXPOSE 8080

# Exec form: java runs as PID 1 and receives SIGTERM directly, so graceful shutdown works
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
