FROM gradle:8.4-jdk21 AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean shadowJar --no-daemon



FROM selenium/standalone-chrome:latest

USER root

RUN apt-get update && \
    apt-get install -y openjdk-21-jre && \
    rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH="$JAVA_HOME/bin:${PATH}"

ENV CHROME_BIN=/usr/bin/google-chrome
ENV CHROMEDRIVER=/usr/bin/chromedriver

WORKDIR /app

# copy final jar
COPY --from=builder /app/build/libs/*-all.jar app.jar

EXPOSE 8080
ENV PORT=8080

CMD ["java", "-jar", "app.jar"]


