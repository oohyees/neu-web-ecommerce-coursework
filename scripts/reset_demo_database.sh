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

SCHEMA="$ROOT/backend/legacy-web/src/main/resources/schema.sql"
DATA="$ROOT/backend/legacy-web/src/main/resources/data.sql"

if [[ ! -f "$SCHEMA" || ! -f "$DATA" ]]; then
  echo "schema.sql or data.sql was not found." >&2
  exit 1
fi

if ! command -v mysql >/dev/null 2>&1; then
  echo "mysql command was not found. Install MySQL client or reinitialize the database with docker compose." >&2
  exit 1
fi

MYSQL_ARGS=(
  --host="$HOST"
  --port="$PORT"
  --user="$USER"
  --password="$PASSWORD"
  --default-character-set=utf8mb4
)

echo "Resetting database '$DATABASE' on $HOST:$PORT ..."
mysql "${MYSQL_ARGS[@]}" < "$SCHEMA"
mysql "${MYSQL_ARGS[@]}" "$DATABASE" < "$DATA"

echo "Demo database reset complete."

if command -v redis-cli >/dev/null 2>&1; then
  redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" FLUSHDB >/dev/null
  echo "Redis cache/session data cleared."
elif command -v docker >/dev/null 2>&1 && [[ -n "${COMPOSE_FILE:-}" && -n "${REDIS_SERVICE:-}" ]]; then
  docker compose -f "$COMPOSE_FILE" exec -T "$REDIS_SERVICE" redis-cli FLUSHDB >/dev/null
  echo "Redis cache/session data cleared through Docker Compose."
elif command -v docker >/dev/null 2>&1; then
  docker compose exec -T redis redis-cli FLUSHDB >/dev/null
  echo "Redis cache/session data cleared through Docker Compose."
else
  echo "redis-cli not found; skip Redis cache cleanup."
fi
