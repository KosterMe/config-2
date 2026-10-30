#!/bin/sh
# Этап 3: ошибки в файлах тестового репозитория.
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT" || exit 1

for file in examples/test_errors/*.xml; do
    echo "=== $file ==="
    ./run.sh "$file"
    echo "exit code: $?"
done
