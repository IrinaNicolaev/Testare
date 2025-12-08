FROM nginx:alpine

COPY *.jpg /usr/share/nginx/html/
COPY *.png /usr/share/nginx/html/

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
