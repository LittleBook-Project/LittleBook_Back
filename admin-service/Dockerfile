### Simple Dockerfile multi-stage pour admin-service
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only admin-service module (standalone build)
COPY admin-service/pom.xml pom.xml
COPY admin-service/src src

# Build the admin-service module independently
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
