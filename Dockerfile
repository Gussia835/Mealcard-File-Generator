FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

COPY gradle ./gradle
COPY gradlew build.gradle settings.gradle ./
COPY src ./src

RUN chmod +x gradlew
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*-all.jar app.jar

EXPOSE 90 6666

CMD ["java", "-jar", "app.jar"]