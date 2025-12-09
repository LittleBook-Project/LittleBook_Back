### Dockerfile multi-stage pour gateway (monolithe en transition)
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only gateway module
COPY gateway/pom.xml pom.xml
COPY gateway/src src

# Build the gateway module independently
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
