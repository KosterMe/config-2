package depviz.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Параметры, настраиваемые пользователем в конфигурационном файле.
 *
 * @param packageName имя анализируемого пакета
 * @param repository URL-адрес репозитория или путь к файлу тестового репозитория
 * @param testMode режим работы с тестовым репозиторием
 * @param asciiTree режим вывода зависимостей в формате ASCII-дерева
 */
public record Config(String packageName, String repository, boolean testMode,
        boolean asciiTree) {

    /** Имя параметра с именем анализируемого пакета. */
    public static final String PACKAGE = "package";
    /** Имя параметра с адресом репозитория. */
    public static final String REPOSITORY = "repository";
    /** Имя параметра режима тестового репозитория. */
    public static final String TEST_MODE = "test-mode";
    /** Имя параметра режима вывода ASCII-дерева. */
    public static final String ASCII_TREE = "ascii-tree";

    /**
     * Возвращает параметры в виде пар ключ-значение в порядке их описания.
     *
     * @return упорядоченный набор параметров
     */
    public Map<String, String> toKeyValues() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(PACKAGE, packageName);
        values.put(REPOSITORY, repository);
        values.put(TEST_MODE, String.valueOf(testMode));
        values.put(ASCII_TREE, String.valueOf(asciiTree));
        return values;
    }
}
