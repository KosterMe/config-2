# Визуализатор графа зависимостей пакетов Alpine Linux

Практическая работа №2 по дисциплине «Конфигурационное управление», вариант №1.

## Общее описание

Консольное приложение на Java для анализа и визуализации графа зависимостей
пакетов менеджера пакетов Alpine Linux (apk). Готовые менеджеры пакетов и
библиотеки для получения зависимостей не используются.

Приложение читает настройки из конфигурационного файла формата XML,
скачивает индекс пакетов репозитория Alpine Linux (`APKINDEX.tar.gz`),
самостоятельно распаковывает и разбирает его и выводит прямые зависимости
заданного пакета.

## Этапы

| Этап | Что сделано | Тег |
|---|---|---|
| 1. Прототип с конфигурацией | чтение и проверка XML-конфига, вывод параметров | `stage-1` |
| 2. Сбор данных | загрузка и разбор `APKINDEX`, вывод прямых зависимостей | `stage-2` |

Каждый этап — отдельный коммит, отмеченный тегом. Состояние проекта на нужном
этапе: `git checkout stage-1` (вернуться обратно: `git checkout main`).
Вывод всех параметров в формате ключ-значение требовался только на этапе 1,
начиная с этапа 2 приложение выводит прямые зависимости пакета.

## Требования

- JDK 17 или новее (`javac` и `java` должны быть доступны в `PATH`).
- Доступ в интернет для загрузки индекса репозитория.
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
| Прямые зависимости пяти пакетов | `./scripts/stage2_direct.sh` | `scripts\stage2_direct.bat` |
| Ошибки получения данных | `./scripts/stage2_errors.sh` | `scripts\stage2_errors.bat` |

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
  (относительно текущего каталога). Режим тестового репозитория
  реализуется на этапе 3, пока приложение сообщает
  `test repository mode is not implemented yet`.

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
5. Если поставщика нет, зависимость выводится как не найденная.

Несколько записей, которые разрешаются в один пакет (например,
`libcurl=8.14.1-r2` и `so:libcurl.so.4`), выводятся одной строкой: слева
пакет, справа — исходные записи из индекса.

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

Ошибки получения данных (примеры в `examples/repo_errors/`):

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
| Включён тестовый режим | `test repository mode is not implemented yet` | 1 |

## Структура проекта

```
src/depviz/                       исходный код
  Main.java                       точка входа
  DirectDependenciesPrinter.java  вывод прямых зависимостей
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
    RepositoryException.java      ошибка получения данных
tests/depviz/                     тесты (запуск через TestRunner)
examples/config/                  корректные конфигурации
examples/errors/                  конфигурации с ошибками
examples/repo_errors/             конфигурации с ошибками репозитория
examples/repos/                   файлы тестовых репозиториев
scripts/                          демонстрационные скрипты
config.xml                        конфигурация по умолчанию
```

## Примеры использования

Версии пакетов зависят от текущего состояния репозитория и со временем
могут отличаться от приведённых ниже.

```
$ ./run.sh
Package: curl 8.14.1-r2
Direct dependencies (3):
  libcurl  <- libcurl=8.14.1-r2, so:libcurl.so.4
  musl     <- so:libc.musl-x86_64.so.1
  zlib     <- so:libz.so.1

$ ./run.sh examples/config/nginx.xml
Package: nginx 1.26.3-r0
Direct dependencies (6):
  busybox-binsh  <- /bin/sh
  musl           <- so:libc.musl-x86_64.so.1
  libcrypto3     <- so:libcrypto.so.3
  pcre           <- so:libpcre.so.1
  libssl3        <- so:libssl.so.3
  zlib           <- so:libz.so.1

$ ./run.sh examples/config/python3.xml
Package: python3 3.12.13-r0
Direct dependencies (14):
  libssl3      <- libssl3>=3.3.0, so:libssl.so.3
  libbz2       <- so:libbz2.so.1
  musl         <- so:libc.musl-x86_64.so.1
  libcrypto3   <- so:libcrypto.so.3
  libexpat     <- so:libexpat.so.1
  libffi       <- so:libffi.so.8
  gdbm         <- so:libgdbm_compat.so.4
  xz-libs      <- so:liblzma.so.5
  mpdecimal    <- so:libmpdec.so.4
  libncursesw  <- so:libncursesw.so.6
  libpanelw    <- so:libpanelw.so.6
  readline     <- so:libreadline.so.8
  sqlite-libs  <- so:libsqlite3.so.0
  zlib         <- so:libz.so.1
```

Ошибки получения данных (вывод `./scripts/stage2_errors.sh`):

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
