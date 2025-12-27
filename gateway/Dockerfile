### Multi-stage Dockerfile for API Gateway
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy gateway pom and src
COPY gateway/pom.xml pom.xml
COPY gateway/src src

# Build the gateway
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/gateway-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
