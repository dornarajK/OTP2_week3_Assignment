# Run the JavaFX desktop application behind noVNC for browser access.
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM maven:3.9.9-eclipse-temurin-17
ENV DEBIAN_FRONTEND=noninteractive \
    DISPLAY=:1 \
    APP_DB_URL=jdbc:h2:/data/temperaturedb \
    JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8
RUN apt-get update && apt-get install -y --no-install-recommends \
    xvfb x11vnc novnc websockify fluxbox ca-certificates \
    libgtk-3-0t64 libxtst6 libxrender1 libxi6 libx11-6 libgl1 libasound2t64 \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /data
WORKDIR /app
COPY --from=build /app /app
COPY --from=build /root/.m2 /root/.m2
EXPOSE 6080
VOLUME ["/data"]
CMD ["bash", "-lc", "Xvfb :1 -screen 0 1280x800x24 & fluxbox & x11vnc -display :1 -forever -shared -nopw -rfbport 5900 & websockify --web=/usr/share/novnc/ 6080 localhost:5900 & exec mvn -o -q javafx:run"]
