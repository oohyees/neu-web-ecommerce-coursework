#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TARGET="${1:-monolith}"
MYSQL_USER="${DB_USER:-root}"
MONOLITH_MYSQL_PASSWORD="${MONOLITH_DB_PASSWORD:-${DB_PASSWORD:-123456}}"
MICROSERVICE_MYSQL_PASSWORD="${MICROSERVICE_DB_PASSWORD:-${DB_PASSWORD:-root123}}"
DATABASE="${DB_NAME:-ecommerce_minimal}"
SCHEMA="$ROOT/backend/legacy-web/src/main/resources/schema.sql"
DATA="$ROOT/backend/legacy-web/src/main/resources/data.sql"
MICRO_INIT="$ROOT/docker/mysql/init.sql"
MICRO_SQL_FILES=(
  "$ROOT/sql/user/schema.sql"
  "$ROOT/sql/user/data.sql"
  "$ROOT/sql/product/schema.sql"
  "$ROOT/sql/product/data.sql"
  "$ROOT/sql/order/schema.sql"
  "$ROOT/sql/order/data.sql"
  "$ROOT/sql/content/schema.sql"
  "$ROOT/sql/content/data.sql"
  "$ROOT/sql/marketing/schema.sql"
  "$ROOT/sql/marketing/data.sql"
)

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
  local password="$2"
  local database_arg="${3:-}"
  mysql --host=127.0.0.1 --port="$port" --user="$MYSQL_USER" --password="$password" --default-character-set=utf8mb4 $database_arg
}

mysql_exec_compose() {
  local compose_file="$1"
  local password="$2"
  local database_arg="${3:-}"
  if [ "$compose_file" = "-" ]; then
    (cd "$ROOT" && docker compose exec -T mysql mysql --user="$MYSQL_USER" --password="$password" --default-character-set=utf8mb4 $database_arg)
  else
    (cd "$ROOT" && docker compose -f "$compose_file" exec -T mysql mysql --user="$MYSQL_USER" --password="$password" --default-character-set=utf8mb4 $database_arg)
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

reset_monolith() {
  local label="$1"
  local mysql_port="$2"
  local redis_port="$3"
  local compose_file="$4"
  local password="$5"

  echo "[$label] Resetting MySQL database '$DATABASE'..."
  if command -v mysql >/dev/null 2>&1; then
    mysql_exec_host "$mysql_port" "$password" < "$SCHEMA"
    mysql_exec_host "$mysql_port" "$password" "$DATABASE" < "$DATA"
  elif command -v docker >/dev/null 2>&1; then
    mysql_exec_compose "$compose_file" "$password" < "$SCHEMA"
    mysql_exec_compose "$compose_file" "$password" "$DATABASE" < "$DATA"
  else
    echo "mysql client or docker is required." >&2
    exit 1
  fi
  echo "[$label] Clean seed data loaded."
  flush_redis "$label" "$redis_port" "$compose_file"
}

reset_microservices() {
  local label="microservices"
  local mysql_port="18096"
  local redis_port="18097"
  local compose_file="docker/docker-compose.yml"
  local password="$MICROSERVICE_MYSQL_PASSWORD"
  local reset_sql="DROP DATABASE IF EXISTS ecommerce_auth; DROP DATABASE IF EXISTS ecommerce_product; DROP DATABASE IF EXISTS ecommerce_order; DROP DATABASE IF EXISTS ecommerce_admin;"

  echo "[$label] Resetting split MySQL databases..."
  if command -v mysql >/dev/null 2>&1; then
    printf '%s\n' "$reset_sql" | mysql_exec_host "$mysql_port" "$password"
    mysql_exec_host "$mysql_port" "$password" < "$MICRO_INIT"
    for file in "${MICRO_SQL_FILES[@]}"; do
      mysql_exec_host "$mysql_port" "$password" < "$file"
    done
  elif command -v docker >/dev/null 2>&1; then
    printf '%s\n' "$reset_sql" | mysql_exec_compose "$compose_file" "$password"
    mysql_exec_compose "$compose_file" "$password" < "$MICRO_INIT"
    for file in "${MICRO_SQL_FILES[@]}"; do
      mysql_exec_compose "$compose_file" "$password" < "$file"
    done
  else
    echo "mysql client or docker is required." >&2
    exit 1
  fi
  echo "[$label] Clean split seed data loaded."
  flush_redis "$label" "$redis_port" "$compose_file"
}

case "$TARGET" in
  monolith)
    reset_monolith "monolith" 13306 6380 "-" "$MONOLITH_MYSQL_PASSWORD"
    ;;
  microservices)
    reset_microservices
    ;;
  both)
    reset_monolith "monolith" 13306 6380 "-" "$MONOLITH_MYSQL_PASSWORD"
    reset_microservices
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
