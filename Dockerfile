FROM selenium/standalone-chrome:latest

USER root

# Устанавливаем JDK
RUN apt-get update && apt-get install -y openjdk-21-jdk && \
    rm -rf /var/lib/apt/lists/*

# Указываем пути браузера
ENV CHROME_BIN=/usr/bin/google-chrome
ENV CHROMEDRIVER=/usr/bin/chromedriver

WORKDIR /app

# Копируем СБОРКУ
COPY build/libs/*-all.jar app.jar

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "build/libs/flat-watcher-1.0.0-all.jar"]

