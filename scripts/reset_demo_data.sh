#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TARGET="${1:-monolith}"
MYSQL_USER="${DB_USER:-root}"
MYSQL_PASSWORD="${DB_PASSWORD:-123456}"
DATABASE="${DB_NAME:-ecommerce_minimal}"
SCHEMA="$ROOT/apps/api/src/main/resources/schema.sql"
DATA="$ROOT/apps/api/src/main/resources/data.sql"

usage() {
  cat <<'USAGE'
Usage: scripts/reset_demo_data.sh [monolith|microservices|both]

Rebuilds the demo MySQL database from schema.sql/data.sql and clears Redis
sessions/cache. The resulting data is clean seed data only: normal user,
administrator, categories, products, stock, banners, promotions, coupons,
addresses, orders, reviews, announcements, feedback, and consultations.
It removes data created by QA/API/smoke/import test runs.
USAGE
}

mysql_exec_host() {
  local port="$1"
  local database_arg="${2:-}"
  mysql --host=127.0.0.1 --port="$port" --user="$MYSQL_USER" --password="$MYSQL_PASSWORD" --default-character-set=utf8mb4 $database_arg
}

mysql_exec_compose() {
  local compose_file="$1"
  local database_arg="${2:-}"
  if [ "$compose_file" = "-" ]; then
    (cd "$ROOT" && docker compose exec -T mysql mysql --user="$MYSQL_USER" --password="$MYSQL_PASSWORD" --default-character-set=utf8mb4 $database_arg)
  else
    (cd "$ROOT" && docker compose -f "$compose_file" exec -T mysql mysql --user="$MYSQL_USER" --password="$MYSQL_PASSWORD" --default-character-set=utf8mb4 $database_arg)
  fi
}

flush_redis() {
  local label="$1"
  local port="$2"
  local compose_file="$3"
  if command -v redis-cli >/dev/null 2>&1; then
    redis-cli -h 127.0.0.1 -p "$port" FLUSHDB >/dev/null
  elif command -v docker >/dev/null 2>&1; then
    if [ "$compose_file" = "-" ]; then
      (cd "$ROOT" && docker compose exec -T redis redis-cli FLUSHDB >/dev/null)
    else
      (cd "$ROOT" && docker compose -f "$compose_file" exec -T redis redis-cli FLUSHDB >/dev/null)
    fi
  else
    echo "[$label] redis-cli/docker not found; Redis cleanup skipped."
    return
  fi
  echo "[$label] Redis cache/session data cleared."
}

reset_stack() {
  local label="$1"
  local mysql_port="$2"
  local redis_port="$3"
  local compose_file="$4"

  echo "[$label] Resetting MySQL database '$DATABASE'..."
  if command -v mysql >/dev/null 2>&1; then
    mysql_exec_host "$mysql_port" < "$SCHEMA"
    mysql_exec_host "$mysql_port" "$DATABASE" < "$DATA"
  elif command -v docker >/dev/null 2>&1; then
    mysql_exec_compose "$compose_file" < "$SCHEMA"
    mysql_exec_compose "$compose_file" "$DATABASE" < "$DATA"
  else
    echo "mysql client or docker is required." >&2
    exit 1
  fi
  echo "[$label] Clean seed data loaded."
  flush_redis "$label" "$redis_port" "$compose_file"
}

case "$TARGET" in
  monolith)
    reset_stack "monolith" 13306 6380 "-"
    ;;
  microservices)
    reset_stack "microservices" 18096 18097 "docker-compose.microservices.yml"
    ;;
  both)
    reset_stack "monolith" 13306 6380 "-"
    reset_stack "microservices" 18096 18097 "docker-compose.microservices.yml"
    ;;
  -h|--help|help)
    usage
    exit 0
    ;;
  *)
    usage >&2
    exit 1
    ;;
esac

echo "Demo data reset complete."
