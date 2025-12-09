FROM eclipse-temurin:17-jdk

RUN apt-get update && apt-get install -y \
    xvfb \
    x11vnc \
    fluxbox \
    novnc \
    websockify \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY GardenAnimation.java .

RUN javac GardenAnimation.java

EXPOSE 6080

CMD bash -c "\
Xvfb :99 -screen 0 1280x800x24 & \
fluxbox & \
x11vnc -display :99 -nopw -forever -shared & \
websockify --web=/usr/share/novnc/ 6080 localhost:5900 & \
DISPLAY=:99 java GardenAnimation"