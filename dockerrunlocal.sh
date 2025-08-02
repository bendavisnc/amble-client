docker build \
  --build-arg CLIENT_URL=http://localhost:8080 \
  --build-arg SERVER_URL=http://localhost:3000 \
  --build-arg SERVER_HOST=localhost \
  --build-arg SERVER_PORT=3000 \
  -t ambleclientdocker .

docker run -p 8080:8080 ambleclientdocker:latest
