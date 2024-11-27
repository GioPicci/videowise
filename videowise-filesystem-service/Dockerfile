FROM golang:1.18.1

LABEL authors="giovanni.piccinini"

WORKDIR /app
RUN apt-get update && apt install -y ffmpeg
COPY . .
RUN go build -o filesystem-service main.go
ENTRYPOINT ["./filesystem-service"]