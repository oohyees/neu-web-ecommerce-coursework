#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BASE_URL="${BASE_URL:-http://localhost:18080}"
FRONTEND_URL="${FRONTEND_URL:-http://localhost:18081}"
MICRO_GATEWAY_URL="${MICRO_GATEWAY_URL:-http://localhost:18090}"
MICRO_FRONTEND_URL="${MICRO_FRONTEND_URL:-http://localhost:18095}"

cd "$ROOT"

echo "[1/8] Monolith containers"
docker compose ps

echo "[2/8] Monolith frontend"
curl -fsS "$FRONTEND_URL/" >/dev/null

echo "[3/8] Monolith backend"
curl -fsS "$BASE_URL/api/home" | grep -q '"success":true'

echo "[4/8] Monolith legacy Servlet/JSP/JDBC evidence"
ADMIN_LOGIN="$(curl -fsS -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}' "$BASE_URL/api/auth/admin/login")"
ADMIN_TOKEN="$(printf '%s' "$ADMIN_LOGIN" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')"
test -n "$ADMIN_TOKEN"
curl -fsS -H "Authorization: Bearer $ADMIN_TOKEN" "$BASE_URL/legacy/status" | grep -q '传统 Web 技术状态页'

echo "[5/8] Microservice containers"
docker compose -f docker-compose.microservices.yml ps

echo "[6/8] Microservice frontend"
curl -fsS "$MICRO_FRONTEND_URL/" >/dev/null

echo "[7/8] Gateway API"
curl -fsS "$MICRO_GATEWAY_URL/api/products?page=1&size=3" | grep -q '"success":true'

echo "[8/8] Redis key evidence hints"
echo "  Monolith Redis keys:      docker compose exec -T redis redis-cli keys '*'"
echo "  Microservice Redis keys:  docker compose -f docker-compose.microservices.yml exec -T redis redis-cli keys '*'"
echo "  Session key pattern:      session:<token>"
echo "  Cache key examples:       home::SimpleKey [], productDetail::<id>"

echo "Demo service check passed."
