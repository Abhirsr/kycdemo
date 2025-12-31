# Build Stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Skip tests to speed up build and avoid environment dependency issues during build
RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Create uploads directory for temp storage (ephemeral)
RUN mkdir uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
