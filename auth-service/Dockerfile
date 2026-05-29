FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY ../gradlew .
COPY ../gradle gradle
COPY ../build.gradle ../settings.gradle ./
COPY build.gradle ./auth-service/
COPY src ./auth-service/src
RUN ./gradlew :auth-service:clean :auth-service:build -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/auth-service/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]