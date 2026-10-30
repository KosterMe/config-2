#!/bin/sh
# Этап 2: прямые зависимости пакетов из репозитория Alpine Linux.
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT" || exit 1

for file in config.xml examples/config/minimal.xml examples/config/git.xml \
        examples/config/python3.xml examples/config/nginx.xml; do
    echo "=== $file ==="
    ./run.sh "$file"
    echo "exit code: $?"
done
