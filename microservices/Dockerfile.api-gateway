FROM openjdk:11-jre-slim
WORKDIR /app
COPY api-gateway/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
