#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
for service in backend/common backend/auth-service backend/product-service backend/order-service backend/admin-service backend/gateway-service; do
  echo "==> mvn package: $service"
  (cd "$ROOT/$service" && mvn -q -DskipTests package)
done

echo "Microservice packages are ready. Run:"
echo "docker compose -f docker/docker-compose.yml up -d --build"
