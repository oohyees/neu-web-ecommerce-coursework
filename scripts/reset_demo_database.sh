#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TARGET="${1:-monolith}"

echo "scripts/reset_demo_database.sh is kept for compatibility."
echo "Use scripts/reset_demo_data.sh [monolith|microservices|both] for new runs."
exec "$ROOT/scripts/reset_demo_data.sh" "$TARGET"
