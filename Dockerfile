FROM openjdk:17-jdk-slim
LABEL authors="Валерий"

RUN apt-get update && apt-get install -y wget unzip && \
    wget https://services.gradle.org/distributions/gradle-8.7-bin.zip -P /tmp && \
    unzip -d /opt/gradle /tmp/gradle-8.7-bin.zip && \
    In -s /opt/gradle/gradle-8.7/bin/gradle /usr/bin/gradle && \
    rm -rf /var/lib/apt/lists/* /tmp/*

ENV GRADLE_HOME=/opt/gradle-8.7
ENV PATH=$PATH:$GRADLE_HOME/bin

WORKDIR /app

COPY . .

RUN gradle clean build -x test

CMD ["java", "-jar", "build/libs/flat-watcher.jar"]

