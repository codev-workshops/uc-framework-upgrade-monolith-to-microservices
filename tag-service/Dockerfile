FROM eclipse-temurin:11-jre-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
RUN apk add --no-cache curl
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
