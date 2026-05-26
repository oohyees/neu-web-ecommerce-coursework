#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
for service in ecommerce-common ecommerce-auth-service ecommerce-product-service ecommerce-order-service ecommerce-admin-service ecommerce-gateway; do
  echo "==> mvn package: $service"
  (cd "$ROOT/$service" && mvn -q -DskipTests package)
done

echo "Microservice packages are ready. Run:"
echo "docker compose -f docker-compose.microservices.yml up -d --build"
