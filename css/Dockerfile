FROM alpine:latest

# 1. Устанавливаем Python 3
RUN apk add --no-cache python3

# 2. Создаем директорию и делаем ее рабочей
WORKDIR /usr/share/css

# 3. Копируем файл CSS
COPY style.css /usr/share/css/

# 4. Запускаем Python HTTP-сервер на порту 80
CMD ["python3", "-m", "http.server", "80"]

EXPOSE 80