
if [ -z "$CLIENT_URL" ]; then
  echo "Error: CLIENT_URL environment variable is not set."
  exit 1
fi

if [ -z "$SERVER_URL" ]; then
  echo "Error: SERVER_URL environment variable is not set."
  exit 1
fi

if [ -z "$SERVER_HOST" ]; then
  echo "Error: SERVER_HOST environment variable is not set."
  exit 1
fi

if [ -z "$SERVER_PORT" ]; then
  echo "Error: SERVER_PORT environment variable is not set."
  exit 1
fi

npx shadow-cljs release app
