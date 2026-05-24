#!/bin/sh
set -e
CERT=/etc/nginx/ssl/localhost.pem
KEY=/etc/nginx/ssl/localhost-key.pem
if [ ! -f "$CERT" ] || [ ! -f "$KEY" ]; then
  echo "TLS: не найдены $CERT и $KEY." >&2
  echo "Сгенерируй их на хосте (mkcert): ./scripts/gen-docker-ssl-mkcert.sh" >&2
  echo "Затем: docker compose up --build" >&2
  exit 1
fi
exec /docker-entrypoint.sh "$@"
