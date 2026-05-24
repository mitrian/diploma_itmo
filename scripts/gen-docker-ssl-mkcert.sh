#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SSL_DIR="$ROOT/docker/ssl"
mkdir -p "$SSL_DIR"
cd "$SSL_DIR"

if ! command -v mkcert >/dev/null 2>&1; then
  echo "mkcert не найден. Установи и один раз импортируй CA: mkcert -install" >&2
  echo "  macOS: brew install mkcert nss" >&2
  exit 1
fi

mkcert -install 2>/dev/null || true
mkcert -cert-file localhost.pem -key-file localhost-key.pem localhost 127.0.0.1 ::1

echo "OK: $SSL_DIR/localhost.pem + localhost-key.pem"
echo "Дальше: docker compose up --build"
