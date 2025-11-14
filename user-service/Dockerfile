### Multi-stage Dockerfile for user-service (build with Maven, run with Temurin JRE)
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only user-service module (standalone build)
COPY user-service/pom.xml pom.xml
COPY user-service/src src

# Build the user-service module independently
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

# user-service listens on 8083 by default
EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
