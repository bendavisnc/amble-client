docker build \
  --build-arg CLIENT_URL=http://localhost:8080 \
  --build-arg SERVER_URL=http://localhost:80 \
  --build-arg SERVER_HOST=localhost \
  --build-arg SERVER_PORT=80 \
  -t ambleclientdocker .

docker run -p 8080:8080 ambleclientdocker:latest
