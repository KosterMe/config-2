#!/bin/sh
# Сборка и запуск тестов.
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT" || exit 1
javac -encoding UTF-8 -d "$ROOT/out/classes" -sourcepath "$ROOT/src" \
    "$ROOT/src/depviz/Main.java" || exit 1
javac -encoding UTF-8 -d "$ROOT/out/test-classes" -cp "$ROOT/out/classes" \
    -sourcepath "$ROOT/tests" "$ROOT/tests/depviz/TestRunner.java" || exit 1
java -cp "$ROOT/out/classes:$ROOT/out/test-classes" depviz.TestRunner
