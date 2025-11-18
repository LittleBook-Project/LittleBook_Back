### Simple Dockerfile multi-stage pour book-service
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only book-service module (standalone build)
COPY book-service/pom.xml pom.xml
COPY book-service/src src

# Build the book-service module independently
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java","-jar","/app/app.jar"]
