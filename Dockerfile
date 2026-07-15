# --- Stage 1: Build ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copy pom.xml first so Maven's dependency layer is cached separately from source changes
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/salary-slip-management.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]