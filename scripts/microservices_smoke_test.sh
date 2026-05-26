#!/usr/bin/env bash
set -euo pipefail

GATEWAY_URL="${GATEWAY_URL:-http://127.0.0.1:18090}"
FRONTEND_URL="${FRONTEND_URL:-http://127.0.0.1:18095}"
NACOS_URL="${NACOS_URL:-http://127.0.0.1:18098/nacos}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.microservices.yml}"
SUBMIT_ORDER="${SUBMIT_ORDER:-1}"
USER_USERNAME="${USER_USERNAME:-alice}"
USER_PASSWORD="${USER_PASSWORD:-123456}"
ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin123}"
PRODUCT_ID="${PRODUCT_ID:-2}"
QUANTITY="${QUANTITY:-1}"

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

pass() {
  printf 'PASS %s\n' "$1"
}

fail() {
  printf 'FAIL %s\n' "$1" >&2
  exit 1
}

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || fail "missing command: $1"
}

http_code() {
  local method="$1"
  local url="$2"
  shift 2
  curl -sS -o "$TMP_DIR/response.json" -w '%{http_code}' -X "$method" "$url" "$@"
}

get_json_string() {
  local key="$1"
  sed -n "s/.*\"$key\":\"\\([^\"]*\\)\".*/\\1/p" "$TMP_DIR/response.json" | head -n 1
}

get_json_number() {
  local key="$1"
  sed -n "s/.*\"$key\":\\([0-9][0-9]*\\).*/\\1/p" "$TMP_DIR/response.json" | head -n 1
}

assert_code() {
  local expected="$1"
  local actual="$2"
  local label="$3"
  if [ "$actual" != "$expected" ]; then
    printf 'Response body:\n' >&2
    cat "$TMP_DIR/response.json" >&2 || true
    printf '\n' >&2
    fail "$label expected HTTP $expected got $actual"
  fi
  pass "$label"
}

assert_body_contains() {
  local needle="$1"
  local label="$2"
  if ! grep -q "$needle" "$TMP_DIR/response.json"; then
    printf 'Response body:\n' >&2
    cat "$TMP_DIR/response.json" >&2 || true
    printf '\n' >&2
    fail "$label missing: $needle"
  fi
  pass "$label"
}

require_cmd curl
require_cmd grep
require_cmd sed

echo "Microservices smoke test"
echo "Gateway:  $GATEWAY_URL"
echo "Nacos:    $NACOS_URL"
echo "Frontend: $FRONTEND_URL"
echo

echo "[1/10] Nacos registration"
code="$(http_code GET "$NACOS_URL/v1/ns/catalog/services?pageNo=1&pageSize=20")"
assert_code 200 "$code" "nacos service catalog reachable"
for service in ecommerce-gateway ecommerce-auth-service ecommerce-catalog-service ecommerce-order-service ecommerce-admin-service; do
  grep -q "\"name\":\"$service\"" "$TMP_DIR/response.json" || fail "nacos missing service: $service"
  grep -q "\"healthyInstanceCount\":1" "$TMP_DIR/response.json" || true
  pass "nacos registered $service"
done

echo "[2/10] Frontend"
code="$(http_code GET "$FRONTEND_URL/")"
assert_code 200 "$code" "frontend index reachable"

echo "[3/10] Public product route"
code="$(http_code GET "$GATEWAY_URL/api/products?page=1&size=3")"
assert_code 200 "$code" "gateway product list"
assert_body_contains '"success":true' "product response success"

echo "[4/10] Unauthorized cart"
code="$(http_code GET "$GATEWAY_URL/api/cart")"
assert_code 401 "$code" "cart requires token"

echo "[5/10] User login"
code="$(http_code POST "$GATEWAY_URL/api/auth/login" -H 'Content-Type: application/json' -d "{\"username\":\"$USER_USERNAME\",\"password\":\"$USER_PASSWORD\"}")"
assert_code 200 "$code" "user login HTTP"
USER_TOKEN="$(get_json_string token)"
[ -n "$USER_TOKEN" ] || fail "user login did not return token"
pass "user token returned"

echo "[6/10] Authenticated cart"
code="$(http_code GET "$GATEWAY_URL/api/cart" -H "Authorization: Bearer $USER_TOKEN")"
assert_code 200 "$code" "cart with token"
assert_body_contains '"success":true' "cart response success"

echo "[7/10] Admin protection"
code="$(http_code GET "$GATEWAY_URL/api/admin/dashboard" -H "Authorization: Bearer $USER_TOKEN")"
assert_code 403 "$code" "normal user blocked from admin dashboard"

echo "[8/10] Admin login and dashboard"
code="$(http_code POST "$GATEWAY_URL/api/auth/admin/login" -H 'Content-Type: application/json' -d "{\"username\":\"$ADMIN_USERNAME\",\"password\":\"$ADMIN_PASSWORD\"}")"
assert_code 200 "$code" "admin login HTTP"
ADMIN_TOKEN="$(get_json_string token)"
[ -n "$ADMIN_TOKEN" ] || fail "admin login did not return token"
code="$(http_code GET "$GATEWAY_URL/api/admin/dashboard" -H "Authorization: Bearer $ADMIN_TOKEN")"
assert_code 200 "$code" "admin dashboard with admin token"
assert_body_contains '"userCount"' "admin dashboard data"

echo "[9/10] Order cross-service chain"
if [ "$SUBMIT_ORDER" = "1" ]; then
  code="$(http_code GET "$GATEWAY_URL/api/products/$PRODUCT_ID")"
  assert_code 200 "$code" "product detail before order"
  BEFORE_STOCK="$(get_json_number stock)"
  [ -n "$BEFORE_STOCK" ] || fail "cannot read product stock before order"

  code="$(http_code POST "$GATEWAY_URL/api/cart/items" -H 'Content-Type: application/json' -H "Authorization: Bearer $USER_TOKEN" -d "{\"productId\":$PRODUCT_ID,\"quantity\":$QUANTITY,\"specText\":\"smoke-test\"}")"
  assert_code 200 "$code" "add product to cart"

  code="$(http_code GET "$GATEWAY_URL/api/cart" -H "Authorization: Bearer $USER_TOKEN")"
  assert_code 200 "$code" "reload cart"
  CART_ITEM_ID="$(sed -n "s/.*\"id\":\\([0-9][0-9]*\\),\"productId\":$PRODUCT_ID,.*\"specText\":\"smoke-test\".*/\\1/p" "$TMP_DIR/response.json" | head -n 1)"
  [ -n "$CART_ITEM_ID" ] || CART_ITEM_ID="$(get_json_number id)"
  [ -n "$CART_ITEM_ID" ] || fail "cannot find smoke-test cart item id"

  code="$(http_code GET "$GATEWAY_URL/api/addresses" -H "Authorization: Bearer $USER_TOKEN")"
  assert_code 200 "$code" "user addresses"
  ADDRESS_ID="$(get_json_number id)"
  [ -n "$ADDRESS_ID" ] || fail "cannot find user address id"

  code="$(http_code POST "$GATEWAY_URL/api/orders" -H 'Content-Type: application/json' -H "Authorization: Bearer $USER_TOKEN" -d "{\"addressId\":$ADDRESS_ID,\"cartItemIds\":[$CART_ITEM_ID],\"productIds\":[],\"paymentMethod\":\"MOCK_PAY\"}")"
  assert_code 200 "$code" "create order through gateway"
  ORDER_NO="$(get_json_string orderNo)"
  ORDER_ID="$(get_json_number id)"
  [ -n "$ORDER_NO" ] || fail "create order did not return orderNo"
  pass "created order $ORDER_NO id=$ORDER_ID"

  code="$(http_code GET "$GATEWAY_URL/api/orders" -H "Authorization: Bearer $USER_TOKEN")"
  assert_code 200 "$code" "query user orders"
  assert_body_contains "$ORDER_NO" "new order appears in order list"

  code="$(http_code GET "$GATEWAY_URL/api/products/$PRODUCT_ID")"
  assert_code 200 "$code" "product detail after order"
  AFTER_STOCK="$(get_json_number stock)"
  [ -n "$AFTER_STOCK" ] || fail "cannot read product stock after order"
  EXPECTED_STOCK=$((BEFORE_STOCK - QUANTITY))
  [ "$AFTER_STOCK" -eq "$EXPECTED_STOCK" ] || fail "stock deduction mismatch productId=$PRODUCT_ID before=$BEFORE_STOCK after=$AFTER_STOCK quantity=$QUANTITY"
  pass "stock deducted productId=$PRODUCT_ID before=$BEFORE_STOCK after=$AFTER_STOCK quantity=$QUANTITY"
else
  pass "order submission skipped because SUBMIT_ORDER=$SUBMIT_ORDER"
fi

echo "[10/10] Feign log evidence"
if command -v docker >/dev/null 2>&1 && [ "$SUBMIT_ORDER" = "1" ]; then
  (cd "$ROOT" && docker compose -f "$COMPOSE_FILE" logs --tail=240 order-service > "$TMP_DIR/order.log" 2>/dev/null) || fail "cannot read order-service logs"
  (cd "$ROOT" && docker compose -f "$COMPOSE_FILE" logs --tail=240 catalog-service > "$TMP_DIR/catalog.log" 2>/dev/null) || fail "cannot read catalog-service logs"
  grep -q "Feign call catalog-service productId=$PRODUCT_ID" "$TMP_DIR/order.log" || fail "order-service log missing Feign call evidence"
  grep -q "Internal order query catalog-service productId=$PRODUCT_ID" "$TMP_DIR/catalog.log" || fail "catalog-service log missing internal query evidence"
  grep -q "Internal order deduct stock catalog-service productId=$PRODUCT_ID" "$TMP_DIR/catalog.log" || fail "catalog-service log missing stock deduction evidence"
  pass "order-service Feign log evidence"
  pass "catalog-service internal endpoint log evidence"
else
  pass "docker log evidence skipped"
fi

echo
echo "MICROSERVICES SMOKE TEST PASSED"
