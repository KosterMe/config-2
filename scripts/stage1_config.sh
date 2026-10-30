#!/bin/sh
# Этап 1: вывод параметров из конфигурационных файлов.
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT" || exit 1

echo "=== 1. Default config.xml ==="
./run.sh
echo "exit code: $?"

echo "=== 2. Only required parameters ==="
./run.sh examples/config/minimal.xml
echo "exit code: $?"

echo "=== 3. Test repository mode ==="
./run.sh examples/config/test_simple.xml
echo "exit code: $?"
