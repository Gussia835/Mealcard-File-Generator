FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

COPY gradle ./gradle
COPY gradlew build.gradle settings.gradle ./
COPY src ./src

RUN chmod +x gradlew
RUN ./gradlew shadowJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN mkdir -p /app/config /app/logs /app/out/cards

COPY --from=builder /app/build/libs/mealcard-generator.jar app.jar

EXPOSE 90 6666

ENTRYPOINT ["java", \
            "-Dfile.encoding=UTF-8", \
            "-Dapp.config=/app/config/app.properties", \
            "-Dlogback.configurationFile=/app/config/logback.xml", \
            "-jar", "app.jar"]
