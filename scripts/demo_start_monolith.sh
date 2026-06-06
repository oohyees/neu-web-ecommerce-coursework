#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BASE_URL="${BASE_URL:-http://localhost:18080}"
FRONTEND_URL="${FRONTEND_URL:-http://localhost:18081}"

cd "$ROOT"

echo "[monolith] Starting existing Docker images without rebuild..."
docker compose up -d --no-build

echo "[monolith] Containers:"
docker compose ps

echo "[monolith] Checking frontend..."
curl -fsS "$FRONTEND_URL/" >/dev/null

echo "[monolith] Checking backend home API..."
curl -fsS "$BASE_URL/api/home" | grep -q '"success":true'

echo "[monolith] Checking product list API..."
curl -fsS "$BASE_URL/api/products?page=1&size=3" | grep -q '"total"'

echo "[monolith] Checking legacy evidence page..."
ADMIN_LOGIN="$(curl -fsS -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}' "$BASE_URL/api/auth/admin/login")"
ADMIN_TOKEN="$(printf '%s' "$ADMIN_LOGIN" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')"
test -n "$ADMIN_TOKEN"
curl -fsS -H "Authorization: Bearer $ADMIN_TOKEN" "$BASE_URL/legacy/status" | grep -q '传统 Web 技术状态页'

echo "Monolith demo stack is reachable:"
echo "  Frontend: $FRONTEND_URL"
echo "  Backend:  $BASE_URL/api"
echo "  MailHog:  ${MAILHOG_URL:-http://localhost:18099}"
