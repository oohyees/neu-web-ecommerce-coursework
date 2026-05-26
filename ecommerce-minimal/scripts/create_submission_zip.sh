#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT_DIR="${OUT_DIR:-$ROOT/../submission}"
NAME="${1:-学号-姓名-班级-大作业-工程压缩包.zip}"
mkdir -p "$OUT_DIR"

cd "$ROOT/.."
zip -qr "$OUT_DIR/$NAME" \
  ecommerce-minimal \
  ecommerce-common \
  ecommerce-gateway \
  ecommerce-auth-service \
  ecommerce-product-service \
  ecommerce-order-service \
  ecommerce-admin-service \
  ecommerce-frontend \
  scripts \
  docker-compose.microservices.yml \
  -x '*/node_modules/*' \
     'ecommerce-minimal/microservices/*' \
     '*/target/*' \
     '*/dist/*' \
     '*/.git/*' \
     '*/.idea/*' \
     '*/.vscode/*' \
     '*/.DS_Store' \
     '*/.chrome-*/*' \
     '*/.refs/*' \
     '*/uploads/*' \
     '*/logs/*'

du -sh "$OUT_DIR/$NAME"
echo "$OUT_DIR/$NAME"
