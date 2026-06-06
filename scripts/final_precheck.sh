#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "[1/6] Frontend production build"
npm --prefix apps/web run build

echo "[2/6] Backend test suite"
mvn test

echo "[3/6] Monolith quick smoke"
./scripts/acceptance_check.sh

echo "[4/6] Monolith full API smoke"
python3 scripts/acceptance_api_smoke.py

echo "[5/6] Microservice smoke"
scripts/microservices_smoke_test.sh

echo "[6/6] Reset clean demo data after smoke tests"
scripts/reset_demo_data.sh both

echo "Final precheck passed. Demo data has been reset."
