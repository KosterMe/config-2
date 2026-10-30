#!/bin/sh
# Сборка и запуск инструмента. Аргументы передаются программе.
ROOT="$(cd "$(dirname "$0")" && pwd)"
javac -encoding UTF-8 -d "$ROOT/out/classes" -sourcepath "$ROOT/src" \
    "$ROOT/src/depviz/Main.java" || exit 1
java -cp "$ROOT/out/classes" depviz.Main "$@"
