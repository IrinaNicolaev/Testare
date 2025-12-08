FROM eclipse-temurin:17-jdk-jammy

LABEL maintainer="dediuc_anastasia"
LABEL description="Containerul pentru laboratorul 1 Java (aplicație GUI Swing)"

# Librării native necesare pentru AWT/Swing peste X11
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
      libx11-6 \
      libxext6 \
      libxrender1 \
      libxtst6 \
      libxi6 \
      libxrandr2 \
      libfreetype6 \
      fontconfig && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copiem și compilăm aplicația curentă (public class QuizTimersApp)
COPY QuizTimersApp.java /app/QuizTimersApp.java
RUN javac /app/QuizTimersApp.java

# Rulăm aplicația GUI; AWT nu trebuie să fie headless
CMD ["java", "-Djava.awt.headless=false", "QuizTimersApp"]
