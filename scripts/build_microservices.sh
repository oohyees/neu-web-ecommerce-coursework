#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
for service in libs/common services/auth-service services/catalog-service services/order-service services/admin-service services/gateway; do
  echo "==> mvn package: $service"
  (cd "$ROOT/$service" && mvn -q -DskipTests package)
done

echo "Microservice packages are ready. Run:"
echo "docker compose -f docker-compose.microservices.yml up -d --build"
