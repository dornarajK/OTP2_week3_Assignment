
FROM maven:3.9.6-eclipse-temurin-17


RUN apt-get update && apt-get install -y --no-install-recommends \
    libgtk-3-0 \
    libxtst6 \
    libxrender1 \
    libxi6 \
    libx11-6 \
    libgl1 \
    libasound2 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app


COPY pom.xml .
COPY src ./src


RUN mvn clean package -DskipTests


CMD ["mvn", "-Dfile.encoding=UTF-8", "javafx:run"]
