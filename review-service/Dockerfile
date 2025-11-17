### Simple Dockerfile multi-stage pour reviex-service
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only review-service module (standalone build)
COPY review-service/pom.xml pom.xml
COPY review-service/src src

# Build the review-service module independently
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]
