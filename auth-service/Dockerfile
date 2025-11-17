FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only auth-service module (standalone build)
COPY auth-service/pom.xml pom.xml
COPY auth-service/src src

# Build the auth-service module independently
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar


EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
