FROM alpine:latest

# 1. Устанавливаем Python 3
RUN apk add --no-cache python3

# 2. Устанавливаем корневую директорию для статических файлов
# Изображение будет доступно по пути: /my_image.jpg
WORKDIR /usr/share/image_root/

# 3. Копируем my_image.jpg прямо в корень рабочей директории
# Папки не создаются, и файл не будет иметь префикса "images/".
COPY my_image.jpg /usr/share/image_root/my_image.jpg

# 4. Запускаем Python HTTP-сервер на порту 80
CMD ["python3", "-m", "http.server", "80"]

EXPOSE 80