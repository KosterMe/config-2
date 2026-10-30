#!/bin/sh
# Этап 3: граф зависимостей для разных тестовых репозиториев.
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT" || exit 1

for file in examples/config/test_*.xml; do
    repo=$(sed -n 's:.*<repository>\(.*\)</repository>.*:\1:p' "$file")
    package=$(sed -n 's:.*<package>\(.*\)</package>.*:\1:p' "$file")
    echo "=== $file: package $package, repository $repo ==="
    cat "$repo"
    echo "---"
    ./run.sh "$file"
    echo "exit code: $?"
done
