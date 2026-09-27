# Multi-stage build: builds the React frontend, then packages it into the
# Spring Boot jar via the frontend-maven-plugin (see backend/pom.xml), and
# finally runs the single jar — the whole app is served on port 8080.

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY backend ./backend
COPY frontend ./frontend
WORKDIR /workspace/backend
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/backend/target/qr-generator.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
