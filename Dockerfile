FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
COPY src/main/webapp/images /app/images
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]