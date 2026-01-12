# Simple Dockerfile multi-stage pour notification-service
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only notification-service module (standalone build)
COPY notification-service/pom.xml pom.xml
COPY notification-service/src src

# Build the notification-service module independently
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java","-jar","/app/app.jar"]
