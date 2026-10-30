package depviz.apk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Разбор текстового индекса пакетов {@code APKINDEX}.
 *
 * <p>Индекс состоит из записей, разделённых пустыми строками. Каждая строка
 * записи имеет вид {@code X:значение}, где {@code X} — однобуквенный ключ.
 * Используются ключи {@code P} (имя), {@code V} (версия), {@code D}
 * (зависимости), {@code p} (предоставляемые имена) и {@code k} (приоритет),
 * остальные пропускаются.
 */
public final class ApkIndexParser {

    private static final char NAME = 'P';
    private static final char VERSION = 'V';
    private static final char DEPENDS = 'D';
    private static final char PROVIDES = 'p';
    private static final char PRIORITY = 'k';
    private static final char SEPARATOR = ':';
    private static final int KEY_LENGTH = 2;
    private static final String VERSION_OPERATORS = "<>=~";

    private ApkIndexParser() {
    }

    /**
     * Разбирает текст индекса.
     *
     * @param text содержимое файла {@code APKINDEX}
     * @return пакеты в порядке их следования в индексе
     * @throws RepositoryException если строка или запись имеет неверный формат
     */
    public static List<PackageInfo> parse(String text) throws RepositoryException {
        List<PackageInfo> packages = new ArrayList<>();
        Record record = new Record();
        String[] lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].endsWith("\r") ? lines[i].stripTrailing() : lines[i];
            if (line.isEmpty()) {
                record.finishInto(packages);
                record = new Record();
            } else {
                record.add(line, i + 1);
            }
        }
        record.finishInto(packages);
        return packages;
    }

    /**
     * Возвращает имя зависимости без ограничения версии.
     *
     * <p>Например, {@code libssl3>=3.3.0} превращается в {@code libssl3},
     * а {@code so:libcurl.so.4=4.8.0} — в {@code so:libcurl.so.4}.
     *
     * @param spec запись зависимости или предоставляемого имени
     * @return имя без версии
     */
    public static String baseName(String spec) {
        for (int i = 0; i < spec.length(); i++) {
            if (VERSION_OPERATORS.indexOf(spec.charAt(i)) >= 0) {
                return spec.substring(0, i);
            }
        }
        return spec;
    }

    private static List<String> words(String value) {
        return Arrays.stream(value.trim().split("\\s+")).filter(w -> !w.isEmpty()).toList();
    }

    private static int priority(String value, int line) throws RepositoryException {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new RepositoryException("invalid priority '" + value + "' at APKINDEX line "
                    + line);
        }
    }

    /**
     * Накопитель полей одной записи индекса.
     */
    private static final class Record {

        private String name;
        private String version = "";
        private List<String> dependencies = List.of();
        private List<String> provides = List.of();
        private int priority;
        private int firstLine;

        void add(String line, int number) throws RepositoryException {
            if (line.length() < KEY_LENGTH || line.charAt(1) != SEPARATOR) {
                throw new RepositoryException("invalid APKINDEX line " + number + ": '"
                        + line + "'");
            }
            if (firstLine == 0) {
                firstLine = number;
            }
            String value = line.substring(KEY_LENGTH);
            switch (line.charAt(0)) {
                case NAME -> name = value.trim();
                case VERSION -> version = value.trim();
                case DEPENDS -> dependencies = words(value);
                case PROVIDES -> provides = words(value).stream()
                        .map(ApkIndexParser::baseName).toList();
                case PRIORITY -> priority = priority(value, number);
                default -> {
                }
            }
        }

        void finishInto(List<PackageInfo> packages) throws RepositoryException {
            if (firstLine == 0) {
                return;
            }
            if (name == null || name.isEmpty()) {
                throw new RepositoryException("APKINDEX record at line " + firstLine
                        + " has no package name (P:)");
            }
            packages.add(new PackageInfo(name, version, dependencies, provides, priority));
        }
    }
}
