FROM openjdk:21-jdk-slim
LABEL authors="Валерий"

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew

RUN ./gradlew shadowJar --no-daemon

RUN ls -la build/libs/
RUN find . -name "*jar"

RUN apt-get update && apt-get install -y wget unzip chromium && \
    rm -rf /var/lib/apt/lists/*

ENV PORT 8080

CMD ["sh","-c","echo '=== Starting Application ==='; java", "-jar", "build/libs/flat-watcher-1.0.0-all.jar"]

