FROM eclipse-temurin:21-jdk
FROM selenium/standalone-chrome:latest

WORKDIR /app

RUN apt-get update && apt-get install -y wget unzip chromium && \
     rm -rf /var/lib/apt/lists/*

COPY . .

RUN chmod +x ./gradlew

RUN ./gradlew shadowJar --no-daemon

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "build/libs/flat-watcher-1.0.0-all.jar"]

