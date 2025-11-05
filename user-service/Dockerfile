### Multi-stage Dockerfile for user-service (build with Maven, run with Temurin JRE)
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# copy only what we need for a module build
COPY pom.xml ./
COPY src ./src

# Build the application (skip tests for faster image build)
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

# copy the built jar (wildcard to match the artifact name)
COPY --from=build /workspace/target/*.jar app.jar

# user-service listens on 8082 by default
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
