#!/bin/sh
# Этап 2: ошибки получения данных из репозитория.
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT" || exit 1

for file in examples/repo_errors/*.xml; do
    echo "=== $file ==="
    ./run.sh "$file"
    echo "exit code: $?"
done
