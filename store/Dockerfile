FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache redis

COPY build/libs/*.jar app.jar
EXPOSE 8080
#ENTRYPOINT ["java", "-jar", "app.jar"]
ENTRYPOINT ["sh", "-c", "sleep 30 && java -jar app.jar"]


