# syntax=docker/dockerfile:1

# ---------- Stage 1: build the JAR with Maven + JDK ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only the POM first so the dependency layer is cached until pom.xml changes
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Tests run in the Jenkins "Test" stage, so skip them here
RUN mvn -B -q package -DskipTests

# ---------- Stage 2: slim JRE runtime ----------
FROM eclipse-temurin:17-jre-alpine

# curl is used by the container health check
RUN apk add --no-cache curl \
    && addgroup -S app && adduser -S app -G app

WORKDIR /app
COPY --from=build --chown=app:app /app/target/calculator.jar app.jar

USER app
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -fs http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
