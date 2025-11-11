# -------- STAGE 1: BUILD JAR --------
FROM gradle:8.4-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build shadowJar --no-daemon


# -------- STAGE 2: RUNTIME --------
FROM selenium/standalone-chrome:latest

USER root

# Устанавливаем JDK для запуска приложения
RUN apt-get update && apt-get install -y openjdk-21-jdk && \
    rm -rf /var/lib/apt/lists/*

ENV CHROME_BIN=/usr/bin/google-chrome
ENV CHROMEDRIVER=/usr/bin/chromedriver

WORKDIR /app

# Берём JAR из builder-стадии
COPY --from=builder /app/build/libs/*-all.jar app.jar

EXPOSE 8080
ENV PORT=8080

CMD ["java", "-jar", "app.jar"]


