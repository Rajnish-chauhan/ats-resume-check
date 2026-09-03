# Step 1: Build stage (Maven + JDK 21)
# maven:3.9.6-eclipse-temurin-21 lightweight aur stable hai
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Run stage (Lightweight Alpine image)
# eclipse-temurin:21-jdk-alpine sabse best lightweight runtime hai
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
# Build stage se sirf JAR file uthao
COPY --from=build /target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]