FROM openjdk:21-jdk-slim
LABEL authors="Валерий"

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew

RUN ./gradlew clean build -x test --no-daemon
RUN pwd
RUN ls -la
RUN find . -name "*.jar"
RUN ls -la build/
RUN ls -la build/libs/


CMD ["java", "-jar", "build/libs/flat-watcher-1.0.0.jar"]

