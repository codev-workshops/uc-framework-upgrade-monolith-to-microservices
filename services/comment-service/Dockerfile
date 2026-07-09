# syntax=docker/dockerfile:1
FROM eclipse-temurin:11-jdk AS build
WORKDIR /workspace
COPY gradle gradle
COPY gradlew settings.gradle build.gradle ./
RUN ./gradlew --no-daemon dependencies || true
COPY src src
RUN ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
