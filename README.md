# Визуализатор графа зависимостей пакетов Alpine Linux

Практическая работа №2 по дисциплине «Конфигурационное управление», вариант №1.

## Общее описание

Консольное приложение на Java для анализа и визуализации графа зависимостей
пакетов менеджера пакетов Alpine Linux (apk). Готовые менеджеры пакетов и
библиотеки для получения зависимостей не используются.

Это минимальный прототип: приложение читает настройки из конфигурационного
файла формата XML, проверяет их и выводит в формате ключ-значение.

## Этапы

| Этап | Что сделано | Тег |
|---|---|---|
| 1. Прототип с конфигурацией | чтение и проверка XML-конфига, вывод параметров | `stage-1` |

Каждый этап — отдельный коммит, отмеченный тегом `stage-N`.

## Требования

- JDK 17 или новее (`javac` и `java` должны быть доступны в `PATH`).
- Сторонние библиотеки не используются (XML разбирается стандартным
  `javax.xml.parsers`).

## Сборка, запуск и тесты

| Действие | Linux / macOS | Windows |
|---|---|---|
| Сборка и запуск | `./run.sh [config.xml]` | `run.bat [config.xml]` |
| Сборка и запуск тестов | `./test.sh` | `test.bat` |
| Демонстрация вывода параметров | `./scripts/stage1_config.sh` | `scripts\stage1_config.bat` |
| Демонстрация обработки ошибок | `./scripts/stage1_errors.sh` | `scripts\stage1_errors.bat` |

Скрипты сами компилируют исходники, классы складываются в папку `out/`
(она в `.gitignore`). Тесты написаны без внешних библиотек и запускаются
классом `TestRunner`: он печатает число успешных и проваленных проверок,
при провале код завершения — 1.

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
  (относительно текущего каталога). Содержимое файла используется начиная
  с этапа 3.

## Функции

### Вывод параметров

При запуске приложение выводит все параметры в формате `ключ = значение`
в порядке из таблицы выше, включая значения по умолчанию.

### Обработка ошибок

При любой ошибке сообщение выводится в stderr с префиксом `depviz:`.

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

## Структура проекта

```
src/depviz/                 исходный код
  Main.java                 точка входа
  config/                   конфигурация
    Config.java             параметры пользователя
    ConfigLoader.java       загрузка и проверка значений
    XmlConfigReader.java    разбор структуры XML
    ConfigException.java    ошибка конфигурации
tests/depviz/               тесты (запуск через TestRunner)
examples/config/            корректные конфигурации
examples/errors/            конфигурации с ошибками
examples/repos/             файлы тестовых репозиториев
scripts/                    демонстрационные скрипты
config.xml                  конфигурация по умолчанию
```

## Примеры использования

```
$ ./run.sh
package = curl
repository = https://dl-cdn.alpinelinux.org/alpine/v3.20/main/x86_64
test-mode = false
ascii-tree = false

$ ./run.sh examples/config/minimal.xml
package = busybox
repository = https://dl-cdn.alpinelinux.org/alpine/v3.20/main/x86_64
test-mode = false
ascii-tree = false

$ ./run.sh examples/config/test_simple.xml
package = A
repository = examples/repos/simple.txt
test-mode = true
ascii-tree = true
```

Примеры ошибок (фрагмент вывода `./scripts/stage1_errors.sh`):

```
=== examples/errors/bad_test_mode.xml ===
depviz: parameter <test-mode> must be 'true' or 'false', got 'yes'
exit code: 1
=== examples/errors/no_test_file.xml ===
depviz: test repository file not found: examples/repos/missing.txt
exit code: 1
=== examples/errors/not_xml.xml ===
depviz: invalid XML at line 5: XML document structures must start and end within the same entity.
exit code: 1
=== examples/errors/unknown_param.xml ===
depviz: unknown parameter <depth>
exit code: 1
```
