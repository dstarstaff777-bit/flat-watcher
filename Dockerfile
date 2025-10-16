FROM openjdk:21-jdk-slim
LABEL authors="Валерий"

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew

RUN ./gradlew shadowJar --no-daemon

RUN ls -la build/libs/
RUN find . -name "*jar"


CMD ["java", "-jar", "build/libs/flat-watcher-1.0.0-all.jar"]

