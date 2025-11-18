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

# Note: Firebase credentials should be provided at runtime via:
# - Volume mount: -v /path/to/littlebook.json:/app/littlebook.json
# - Docker secret: --secret firebase_credentials
# The image builds without it for CI/CD, but requires it at runtime

EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
