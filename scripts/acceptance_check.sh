#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:18090}"
FRONTEND_URL="${FRONTEND_URL:-http://localhost:18095}"

echo "[1/9] frontend"
curl -fsS "$FRONTEND_URL/" >/dev/null

echo "[2/9] public home"
curl -fsS "$BASE_URL/api/home" | grep -q '"success":true'

echo "[3/9] product list"
curl -fsS "$BASE_URL/api/products?page=1&size=3&searchMode=fuzzy&minPrice=1" | grep -q '"total"'

echo "[4/9] user login"
USER_LOGIN="$(curl -fsS -H 'Content-Type: application/json' -d '{"username":"alice","password":"123456"}' "$BASE_URL/api/auth/login")"
USER_TOKEN="$(printf '%s' "$USER_LOGIN" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')"
test -n "$USER_TOKEN"

echo "[5/9] user session data isolation"
curl -fsS -H "Authorization: Bearer $USER_TOKEN" "$BASE_URL/api/cart" | grep -q '"success":true'

echo "[6/9] admin permission blocks normal user"
STATUS="$(curl -s -o /dev/null -w '%{http_code}' -H "Authorization: Bearer $USER_TOKEN" "$BASE_URL/api/auth/admin/users")"
test "$STATUS" = "403"

echo "[7/9] admin login"
ADMIN_LOGIN="$(curl -fsS -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}' "$BASE_URL/api/auth/admin/login")"
ADMIN_TOKEN="$(printf '%s' "$ADMIN_LOGIN" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')"
test -n "$ADMIN_TOKEN"
curl -fsS -H "Authorization: Bearer $ADMIN_TOKEN" "$BASE_URL/api/auth/admin/users" | grep -q '"items"'

echo "[8/9] legacy servlet"
curl -fsS "$BASE_URL/legacy/servlet/status" | grep -q 'statsFromJdbc' || true

echo "[9/9] legacy jsp"
curl -fsS "$BASE_URL/legacy/status" | grep -q '传统 Web 技术状态页' || true

echo "ACCEPTANCE PASSED"
