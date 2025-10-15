FROM eclipse-temurin:17-jdk
LABEL authors="Валерий"

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew

RUN ./gradlew clean build -x test --no-daemon

CMD ["java", "-jar", "build/libs/flat-watcher.jar"]

