### Multi-stage Dockerfile for API Gateway
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy gateway module (standalone build)
COPY gateway/pom.xml pom.xml
COPY gateway/src src

# Build the gateway module independently
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

# Gateway listens on 8080 by default
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
