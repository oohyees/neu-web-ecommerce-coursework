#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE_FILE="${COMPOSE_FILE:-docker/docker-compose.yml}"
GATEWAY_URL="${GATEWAY_URL:-http://localhost:18090}"
FRONTEND_URL="${FRONTEND_URL:-http://localhost:18095}"
NACOS_URL="${NACOS_URL:-http://localhost:18098/nacos}"

cd "$ROOT"

echo "[microservices] Starting existing Docker images without rebuild..."
docker compose -f "$COMPOSE_FILE" up -d --no-build

echo "[microservices] Containers:"
docker compose -f "$COMPOSE_FILE" ps

echo "[microservices] Checking frontend..."
curl -fsS "$FRONTEND_URL/" >/dev/null

echo "[microservices] Checking Gateway product API..."
curl -fsS "$GATEWAY_URL/api/products?page=1&size=3" | grep -q '"success":true'

echo "Microservice demo stack is reachable:"
echo "  Shop frontend:  $FRONTEND_URL"
echo "  Admin frontend: ${ADMIN_FRONTEND_URL:-http://localhost:18082}"
echo "  Gateway:  $GATEWAY_URL"
echo "  Nacos:    $NACOS_URL"
echo "  MailHog:  ${MAILHOG_URL:-http://localhost:18199}"
