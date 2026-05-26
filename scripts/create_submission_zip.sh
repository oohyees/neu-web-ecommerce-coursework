#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT_DIR="${OUT_DIR:-$ROOT/submission}"
NAME="${1:-学号-姓名-班级-大作业-工程压缩包.zip}"
mkdir -p "$OUT_DIR"

cd "$ROOT"
zip -qr "$OUT_DIR/$NAME" \
  apps \
  services \
  libs \
  docs \
  scripts \
  pom.xml \
  README.md \
  CLAUDE.md \
  docker-compose.yml \
  docker-compose.microservices.yml \
  -x '*/node_modules/*' \
     '*/target/*' \
     '*/dist/*' \
     '*/dist2/*' \
     '*/.git/*' \
     '*/.idea/*' \
     '*/.vscode/*' \
     '*/.DS_Store' \
     '*/.chrome-*/*' \
     '*/.refs/*' \
     '*/uploads/*' \
     '*/logs/*' \
     'docs/archive/*' \
     'docs/db-backups/*' \
     'submission/*'

du -sh "$OUT_DIR/$NAME"
echo "$OUT_DIR/$NAME"
