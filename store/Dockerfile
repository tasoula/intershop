FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache redis

COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]


