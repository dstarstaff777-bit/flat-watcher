FROM openjdk:21-jdk-slim
LABEL authors="Валерий"

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew

RUN ./gradlew shadowJar --no-daemon

RUN ls -la build/libs/
RUN find . -name "*jar"

RUN apt-get update && apt-get install -y wget gnupg unzip \
    && wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && sh -c echo "deb [arch=amd64] https://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update && apt-get install -y google-chrome-stable \
    && rm -rf /var/lib/apt/lists/*

RUN CHROME_VERSION=$(google-chrome --version | awk '{print $3' | ct -d. -f1) && \
    wget -q -O /tmp/chromedrier.zip https://chromedriver.storage.googleapis.com/${CHROME_VERSION}.0.0/chromedriver_linux64.zip && \
    unzip /tmp/chromedriver.zip -d /usr/local/bin/ && rm /tmp/chromedriver.zip

CMD ["java", "-jar", "build/libs/flat-watcher-1.0.0-all.jar"]

