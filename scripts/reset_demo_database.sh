#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
HOST="${DB_HOST:-127.0.0.1}"
PORT="${DB_PORT:-13306}"
USER="${DB_USER:-root}"
PASSWORD="${DB_PASSWORD:-123456}"
DATABASE="${DB_NAME:-ecommerce_minimal}"
REDIS_HOST="${REDIS_HOST:-127.0.0.1}"
REDIS_PORT="${REDIS_PORT:-6380}"

SCHEMA="$ROOT/apps/api/src/main/resources/schema.sql"
DATA="$ROOT/apps/api/src/main/resources/data.sql"

if [[ ! -f "$SCHEMA" || ! -f "$DATA" ]]; then
  echo "schema.sql or data.sql was not found." >&2
  exit 1
fi

if ! command -v mysql >/dev/null 2>&1; then
  echo "mysql command was not found. Install MySQL client or reinitialize the database with docker compose." >&2
  exit 1
fi

echo "Resetting database '$DATABASE' on $HOST:$PORT ..."
mysql \
  --host="$HOST" \
  --port="$PORT" \
  --user="$USER" \
  --password="$PASSWORD" \
  --default-character-set=utf8mb4 \
  "$DATABASE" < <(printf 'SOURCE %s;\nSOURCE %s;\n' "$SCHEMA" "$DATA")

echo "Demo database reset complete."

if command -v redis-cli >/dev/null 2>&1; then
  redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" FLUSHDB >/dev/null
  echo "Redis cache/session data cleared."
else
  echo "redis-cli not found; skip Redis cache cleanup."
fi
