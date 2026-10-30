#!/bin/sh
# Этап 1: обработка ошибок для всех параметров.
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT" || exit 1

echo "=== Too many arguments ==="
./run.sh a.xml b.xml
echo "exit code: $?"

echo "=== Config file not found ==="
./run.sh examples/config/missing.xml
echo "exit code: $?"

for file in examples/errors/*.xml; do
    echo "=== $file ==="
    ./run.sh "$file"
    echo "exit code: $?"
done
