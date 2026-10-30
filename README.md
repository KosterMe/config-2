# Визуализатор графа зависимостей пакетов Alpine Linux

Практическая работа №2 по дисциплине «Конфигурационное управление», вариант №1.

## Общее описание

Консольное приложение на Java для анализа и визуализации графа зависимостей
пакетов менеджера пакетов Alpine Linux (apk). Готовые менеджеры пакетов и
библиотеки для получения зависимостей не используются.

Приложение читает настройки из конфигурационного файла формата XML,
скачивает индекс пакетов репозитория Alpine Linux (`APKINDEX.tar.gz`),
самостоятельно распаковывает и разбирает его и строит граф зависимостей
заданного пакета с учётом транзитивных зависимостей. Граф строится
рекурсивным обходом в глубину (DFS), циклические зависимости
обнаруживаются и выводятся. Вместо реального репозитория можно указать
файл тестового репозитория, где пакеты называются большими латинскими
буквами.

## Этапы

| Этап | Что сделано | Тег |
|---|---|---|
| 1. Прототип с конфигурацией | чтение и проверка XML-конфига, вывод параметров | `stage-1` |
| 2. Сбор данных | загрузка и разбор `APKINDEX`, вывод прямых зависимостей | `stage-2` |
| 3. Основные операции | граф зависимостей (DFS с рекурсией), циклы, тестовый репозиторий | `stage-3` |

Каждый этап — отдельный коммит, отмеченный тегом. Состояние проекта на нужном
этапе: `git checkout stage-1` (вернуться обратно: `git checkout main`).
Вывод всех параметров в формате ключ-значение требовался только на этапе 1,
а вывод одних прямых зависимостей — только на этапе 2. Начиная с этапа 3
приложение выводит граф зависимостей целиком.

## Требования

- JDK 17 или новее (`javac` и `java` должны быть доступны в `PATH`).
- Доступ в интернет для загрузки индекса реального репозитория (для
  тестового репозитория и тестов сеть не нужна).
- Сторонние библиотеки не используются: XML разбирается стандартным
  `javax.xml.parsers`, загрузка идёт через `java.net.http`, gzip
  распаковывается `java.util.zip`, а формат tar и `APKINDEX` разбираются
  вручную.

## Сборка, запуск и тесты

| Действие | Linux / macOS | Windows |
|---|---|---|
| Сборка и запуск | `./run.sh [config.xml]` | `run.bat [config.xml]` |
| Сборка и запуск тестов | `./test.sh` | `test.bat` |
| Запуск с разными конфигурациями | `./scripts/stage1_config.sh` | `scripts\stage1_config.bat` |
| Демонстрация обработки ошибок | `./scripts/stage1_errors.sh` | `scripts\stage1_errors.bat` |
| Реальный репозиторий: пять пакетов | `./scripts/stage2_direct.sh` | `scripts\stage2_direct.bat` |
| Ошибки получения данных | `./scripts/stage2_errors.sh` | `scripts\stage2_errors.bat` |
| Тестовые репозитории: все случаи | `./scripts/stage3_test_repo.sh` | `scripts\stage3_test_repo.bat` |
| Ошибки в тестовых репозиториях | `./scripts/stage3_test_errors.sh` | `scripts\stage3_test_errors.bat` |

Скрипты сами компилируют исходники, классы складываются в папку `out/`
(она в `.gitignore`). Тесты написаны без внешних библиотек и запускаются
классом `TestRunner`: он печатает число успешных и проваленных проверок,
при провале код завершения — 1. Тесты не обращаются к сети: архивы
`APKINDEX.tar.gz` для них собираются в памяти.

Единственный аргумент командной строки — путь к конфигурационному файлу.
Если он не указан, используется `config.xml` из текущего каталога.

## Конфигурационный файл

Корневой элемент — `<config>`, каждый параметр записывается отдельным
дочерним элементом с текстовым значением. Пробелы по краям значения
отбрасываются, комментарии XML допускаются.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <package>curl</package>
    <repository>https://dl-cdn.alpinelinux.org/alpine/v3.20/main/x86_64</repository>
    <test-mode>false</test-mode>
    <ascii-tree>false</ascii-tree>
</config>
```

| Параметр | Обязательный | Значение | Описание |
|---|---|---|---|
| `package` | да | буквы, цифры, `.` `_` `+` `-` | Имя анализируемого пакета |
| `repository` | да | URL или путь к файлу | URL-адрес репозитория или путь к файлу тестового репозитория |
| `test-mode` | нет, `false` | `true` / `false` | Режим работы с тестовым репозиторием |
| `ascii-tree` | нет, `false` | `true` / `false` | Режим вывода зависимостей в формате ASCII-дерева |

Значение `repository` проверяется в зависимости от режима:

- `test-mode = false` — URL со схемой `http` или `https` и указанным хостом;
- `test-mode = true` — путь к существующему файлу тестового репозитория
  (относительно текущего каталога), формат файла описан ниже.

URL репозитория — это каталог с индексом, например
`https://dl-cdn.alpinelinux.org/alpine/v3.20/main/x86_64`; к нему
добавляется `/APKINDEX.tar.gz`. Можно указать и полный адрес архива.
Параметр `ascii-tree` пока только проверяется, вывод в виде дерева
появится на этапе 5.

## Функции

### Загрузка индекса репозитория

Индекс `APKINDEX.tar.gz` скачивается по HTTP(S) (таймаут 30 секунд,
перенаправления поддерживаются). Архив состоит из нескольких склеенных
потоков gzip: в первом лежит подпись `.SIGN.RSA.*`, во втором — файлы
`DESCRIPTION` и `APKINDEX`. После распаковки архив tar разбирается
вручную: заголовки по 512 байт, имя файла — первые 100 байт заголовка,
размер — восьмеричное число по смещению 124, данные дополняются до
кратного 512 размера.

### Разбор APKINDEX

Индекс — текст из записей, разделённых пустыми строками. Каждая строка
записи имеет вид `X:значение`. Используются поля:

| Поле | Смысл | Пример |
|---|---|---|
| `P` | имя пакета | `P:curl` |
| `V` | версия | `V:8.14.1-r2` |
| `D` | зависимости через пробел | `D:libcurl=8.14.1-r2 so:libz.so.1` |
| `p` | что предоставляет пакет | `p:cmd:curl=8.14.1-r2` |
| `k` | приоритет поставщика | `k:100` |

Остальные поля пропускаются.

### Разрешение зависимостей

Запись в поле `D` превращается в имя пакета так:

1. Ограничение версии отбрасывается: `libssl3>=3.3.0` → `libssl3`,
   `python3~3.12` → `python3` (операторы `<`, `>`, `=`, `~`).
2. Записи `!имя` означают конфликт, а не зависимость, и пропускаются.
3. Если в индексе есть пакет с таким именем — это он.
4. Иначе ищется пакет, у которого имя есть в поле `p`. Так разрешаются
   виртуальные имена: библиотеки `so:libz.so.1`, команды `cmd:sh` и файлы
   `/bin/sh`. Если поставщиков несколько, берётся пакет с наибольшим
   приоритетом `k`, при равенстве — первый в индексе.
5. Если поставщика нет, зависимость попадает в граф как не найденная.

Несколько записей, которые разрешаются в один пакет (например,
`libcurl=8.14.1-r2` и `so:libcurl.so.4`), дают одно ребро графа.

### Тестовый репозиторий

Включается параметром `test-mode = true`, в `repository` указывается путь к
файлу описания графа. Каждая строка описывает один пакет и его прямые
зависимости:

```
# комментарий
A: B C
B: D
C: D, E
D:
E: A
```

- Имена пакетов — большие латинские буквы (`A`, `B`, `AB`, ...).
- Зависимости разделяются пробелами и/или запятыми, после `:` список
  может быть пустым.
- Пустые строки и строки, начинающиеся с `#`, пропускаются.
- Каждый пакет описывается один раз, зависимость в строке не повторяется.
- Пакет, который упомянут только как зависимость, считается отсутствующим
  в репозитории и отмечается в графе как не найденный.

Готовые тестовые репозитории лежат в `examples/repos/`, конфигурации для
них — `examples/config/test_*.xml`:

| Файл | Случай |
|---|---|
| `simple.txt` | общая зависимость (ромб `A → B, C → D`) |
| `chain.txt` | цепочка `A → B → C → D → E` |
| `cycle.txt` | цикл `A → B → C → A` |
| `self_loop.txt` | пакет зависит сам от себя |
| `multi_cycle.txt` | два цикла, общие зависимости и недостижимый пакет `G` |
| `missing_deps.txt` | зависимости, которых нет в репозитории |
| `bad_*.txt` | файлы с ошибками формата |

### Построение графа (DFS)

Граф строится рекурсивным обходом в глубину от заданного пакета. Каждая
вершина находится в одном из состояний: не посещена, «в работе» (лежит на
текущем пути обхода) или обработана.

1. При входе в вершину она помечается «в работе» и добавляется в путь.
2. Для каждой зависимости: если она не посещена — рекурсивный вызов;
   если она «в работе» — найдено обратное ребро, то есть цикл; если уже
   обработана — она не обходится повторно.
3. При выходе вершина удаляется из пути и помечается обработанной.

Цикл — это участок текущего пути от вершины, в которую ведёт обратное
ребро, до текущей вершины. Он записывается в граф, а обход по этому ребру
не продолжается, поэтому циклические зависимости не приводят к
бесконечной рекурсии. Каждая вершина обрабатывается один раз, поэтому
время работы линейно от числа вершин и рёбер.

### Вывод графа

```
Dependency graph of A (packages: 4, edges: 4):
  A -> B
  B -> C
  C -> A, D
  D (no dependencies)
Cycles (1):
  A -> B -> C -> A
```

- В заголовке — число пакетов (включая не найденные) и рёбер графа.
- Пакеты перечислены в порядке обнаружения обходом в глубину, для каждого
  указаны его прямые зависимости.
- `(no dependencies)` — у пакета нет зависимостей, `(not found in
  repository)` — пакет упомянут как зависимость, но его нет в репозитории.
- В конце перечислены найденные циклы или `Cycles: none`.

### Обработка ошибок

При любой ошибке сообщение выводится в stderr с префиксом `depviz:`.
Ошибки конфигурации:

| Ситуация | Сообщение | Код |
|---|---|---|
| Больше одного аргумента | `too many arguments` и подсказка | 2 |
| Файл конфигурации не найден | `config file not found: <путь>` | 1 |
| Путь к конфигурации — каталог | `config path is a directory: <путь>` | 1 |
| Файл не является корректным XML | `invalid XML at line N: ...` | 1 |
| Корневой элемент не `<config>` | `root element must be <config>, got <...>` | 1 |
| Неизвестный параметр | `unknown parameter <depth>` | 1 |
| Параметр указан дважды | `parameter <package> is specified twice` | 1 |
| Вложенные элементы в параметре | `parameter <package> must contain only text` | 1 |
| Текст прямо внутри `<config>` | `unexpected text inside <config>: '...'` | 1 |
| Нет обязательного параметра | `missing required parameter <package>` | 1 |
| Пустое значение | `parameter <package> must not be empty` | 1 |
| Недопустимое имя пакета | `invalid package name 'curl@8': ...` | 1 |
| URL не `http`/`https` | `repository must be an http or https URL, got '...'` | 1 |
| Синтаксически неверный URL | `invalid repository URL '...': ...` | 1 |
| URL без хоста | `repository URL has no host: '...'` | 1 |
| Файл тестового репозитория не найден | `test repository file not found: <путь>` | 1 |
| Тестовый репозиторий — не файл | `test repository is not a file: <путь>` | 1 |
| Флаг не `true`/`false` | `parameter <test-mode> must be 'true' or 'false', got 'yes'` | 1 |

Примеры файлов с каждой ошибкой лежат в `examples/errors/`.

Ошибки получения данных из реального репозитория (примеры в
`examples/repo_errors/`):

| Ситуация | Сообщение | Код |
|---|---|---|
| Сервер ответил не 200 | `cannot download <url>: HTTP 404` | 1 |
| Хост недоступен или сетевая ошибка | `cannot download <url>: <причина>` | 1 |
| Ответ не является архивом gzip | `invalid index archive: ...` | 1 |
| Повреждённый архив tar | `truncated tar entry '...'`, `invalid size '...' of tar entry '...'` | 1 |
| В архиве нет `APKINDEX` | `APKINDEX not found in index archive` | 1 |
| Строка индекса не вида `X:значение` | `invalid APKINDEX line N: '...'` | 1 |
| Запись без имени пакета | `APKINDEX record at line N has no package name (P:)` | 1 |
| Нечисловой приоритет | `invalid priority '...' at APKINDEX line N` | 1 |
| Пакета нет в репозитории | `package 'no-such-package' not found in repository` | 1 |

Ошибки тестового репозитория (примеры в `examples/test_errors/`):

| Ситуация | Сообщение | Код |
|---|---|---|
| Строка без `:` | `test repository line N: expected 'NAME: DEPENDENCIES', got '...'` | 1 |
| Имя не из букв `A-Z` | `test repository line N: invalid package name 'b': ...` | 1 |
| Пакет описан дважды | `test repository line N: package 'A' is already defined at line M` | 1 |
| Зависимость повторяется | `test repository line N: dependency 'B' is listed twice` | 1 |
| Файл не читается | `cannot read test repository '...': ...` | 1 |
| Пакета нет в репозитории | `package 'Z' not found in repository` | 1 |

## Структура проекта

```
src/depviz/                       исходный код
  Main.java                       точка входа
  config/                         конфигурация
    Config.java                   параметры пользователя
    ConfigLoader.java             загрузка и проверка значений
    XmlConfigReader.java          разбор структуры XML
    ConfigException.java          ошибка конфигурации
  apk/                            работа с репозиторием Alpine
    IndexDownloader.java          загрузка APKINDEX.tar.gz
    IndexArchive.java             распаковка gzip и разбор tar
    ApkIndexParser.java           разбор текста APKINDEX
    ApkIndex.java                 поиск пакетов и разрешение зависимостей
    PackageInfo.java              описание пакета
    Dependency.java               прямая зависимость
  repo/                           общие для источников пакетов классы
    PackageSource.java            источник сведений о пакетах
    TestRepository.java           тестовый репозиторий из файла
    RepositoryException.java      ошибка получения данных
  graph/                          граф зависимостей
    GraphBuilder.java             построение графа рекурсивным DFS
    DependencyGraph.java          вершины, рёбра, циклы
    GraphPrinter.java             текстовый вывод графа
tests/depviz/                     тесты (запуск через TestRunner)
examples/config/                  корректные конфигурации
examples/errors/                  конфигурации с ошибками
examples/repo_errors/             ошибки реального репозитория
examples/test_errors/             ошибки тестового репозитория
examples/repos/                   файлы тестовых репозиториев
scripts/                          демонстрационные скрипты
config.xml                        конфигурация по умолчанию
```

## Примеры использования

### Реальный репозиторий

Версии и состав пакетов зависят от текущего состояния репозитория и со
временем могут отличаться от приведённых ниже.

```
$ ./run.sh
Dependency graph of curl (packages: 16, edges: 33):
  curl -> libcurl, musl, zlib
  libcurl -> ca-certificates, brotli-libs, musl, c-ares, libcrypto3, libidn2, nghttp2-libs, libpsl, libssl3, zlib, zstd-libs
  ca-certificates -> busybox-binsh, musl, libcrypto3
  busybox-binsh -> busybox
  busybox -> musl
  musl (no dependencies)
  libcrypto3 -> musl
  brotli-libs -> musl
  c-ares -> musl
  libidn2 -> musl, libunistring
  libunistring -> musl
  nghttp2-libs -> musl
  libpsl -> musl, libidn2, libunistring
  libssl3 -> libcrypto3, musl
  zlib -> musl
  zstd-libs -> musl
Cycles: none

$ ./run.sh examples/config/nginx.xml
Dependency graph of nginx (packages: 8, edges: 13):
  nginx -> busybox-binsh, musl, libcrypto3, pcre, libssl3, zlib
  busybox-binsh -> busybox
  busybox -> musl
  musl (no dependencies)
  libcrypto3 -> musl
  pcre -> musl
  libssl3 -> libcrypto3, musl
  zlib -> musl
Cycles: none
```

### Тестовый репозиторий

Несколько циклов (`examples/repos/multi_cycle.txt`):

```
$ cat examples/repos/multi_cycle.txt
# Несколько циклов и общие зависимости
A: B C
B: D
C: D E
D: B F
E: A
F:
G: A

$ ./run.sh examples/config/test_multi_cycle.xml
Dependency graph of A (packages: 6, edges: 8):
  A -> B, C
  B -> D
  D -> B, F
  F (no dependencies)
  C -> D, E
  E -> A
Cycles (2):
  B -> D -> B
  A -> C -> E -> A
```

Пакет `G` зависит от `A`, но из `A` недостижим, поэтому в граф не попадает.
Тот же репозиторий для пакета `D`:

```
$ ./run.sh examples/config/test_subgraph.xml
Dependency graph of D (packages: 3, edges: 3):
  D -> B, F
  B -> D
  F (no dependencies)
Cycles (1):
  D -> B -> D
```

Пакет зависит сам от себя (`A: A B`):

```
$ ./run.sh examples/config/test_self_loop.xml
Dependency graph of A (packages: 2, edges: 2):
  A -> A, B
  B (no dependencies)
Cycles (1):
  A -> A
```

Отсутствующие пакеты (`A: B X`, `B: C Y`, `C:`):

```
$ ./run.sh examples/config/test_missing.xml
Dependency graph of A (packages: 5, edges: 4):
  A -> B, X
  B -> C, Y
  C (no dependencies)
  Y (not found in repository)
  X (not found in repository)
Cycles: none
```

### Ошибки

Ошибки тестового репозитория (вывод `./scripts/stage3_test_errors.sh`):

```
=== examples/test_errors/bad_dependency.xml ===
depviz: test repository line 1: invalid package name 'C1': use capital Latin letters A-Z
exit code: 1
=== examples/test_errors/duplicate.xml ===
depviz: test repository line 3: package 'A' is already defined at line 1
exit code: 1
=== examples/test_errors/lowercase.xml ===
depviz: test repository line 2: invalid package name 'b': use capital Latin letters A-Z
exit code: 1
=== examples/test_errors/no_colon.xml ===
depviz: test repository line 2: expected 'NAME: DEPENDENCIES', got 'B C'
exit code: 1
=== examples/test_errors/repeated_dependency.xml ===
depviz: test repository line 1: dependency 'B' is listed twice
exit code: 1
=== examples/test_errors/unknown_package.xml ===
depviz: package 'Z' not found in repository
exit code: 1
```

Ошибки реального репозитория (вывод `./scripts/stage2_errors.sh`):

```
=== examples/repo_errors/not_found_url.xml ===
depviz: cannot download https://dl-cdn.alpinelinux.org/alpine/v9.99/main/x86_64/APKINDEX.tar.gz: HTTP 404
exit code: 1
=== examples/repo_errors/unknown_host.xml ===
depviz: cannot download https://no-such-host.invalid/alpine/main/x86_64/APKINDEX.tar.gz: UnresolvedAddressException
exit code: 1
=== examples/repo_errors/unknown_package.xml ===
depviz: package 'no-such-package' not found in repository
exit code: 1
```

Ошибки конфигурации (фрагмент вывода `./scripts/stage1_errors.sh`):

```
=== examples/errors/bad_test_mode.xml ===
depviz: parameter <test-mode> must be 'true' or 'false', got 'yes'
exit code: 1
=== examples/errors/no_test_file.xml ===
depviz: test repository file not found: examples/repos/missing.txt
exit code: 1
=== examples/errors/unknown_param.xml ===
depviz: unknown parameter <depth>
exit code: 1
```
