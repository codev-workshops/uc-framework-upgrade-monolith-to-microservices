# Build stage
FROM eclipse-temurin:11-jdk AS build
WORKDIR /workspace
COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle build.gradle ./
COPY src src
RUN ./gradlew --no-daemon clean bootJar

# Run stage
FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
