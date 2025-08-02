FROM clojure:temurin-21-tools-deps AS build

ARG CLIENT_URL
ARG SERVER_URL
ARG SERVER_HOST
ARG SERVER_PORT

RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs

WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm install

COPY . .

RUN npx shadow-cljs release app

FROM node:20-slim

RUN npm install -g http-server

WORKDIR /app

COPY --from=build /app/resources/public .

# Expose default web port
EXPOSE 8080

# Serve app
CMD ["http-server", "-p", "8080"]
