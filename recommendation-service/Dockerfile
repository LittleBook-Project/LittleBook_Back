FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /build

# Copy only the recommendation service sources into the builder
COPY recommendation-service/pom.xml ./pom.xml
COPY recommendation-service/src ./src

# Build the application JAR (skip tests for faster builds in dev)
RUN mvn -B package -DskipTests

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /build/target/recommendation-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8086
ENTRYPOINT ["java","-jar","/app/app.jar"]
