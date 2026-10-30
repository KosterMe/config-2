package depviz.config;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Загрузка и проверка конфигурационного файла.
 *
 * <p>Обязательные параметры: {@code package} и {@code repository}.
 * Параметры {@code test-mode} и {@code ascii-tree} по умолчанию равны
 * {@code false}.
 */
public final class ConfigLoader {

    private static final Pattern PACKAGE_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._+-]*");
    private static final Set<String> URL_SCHEMES = Set.of("http", "https");
    private static final String TRUE = "true";
    private static final String FALSE = "false";

    private ConfigLoader() {
    }

    /**
     * Читает конфигурационный файл и проверяет параметры.
     *
     * @param path путь к файлу XML
     * @return проверенные параметры
     * @throws ConfigException если файл недоступен или параметры заданы неверно
     */
    public static Config load(String path) throws ConfigException {
        return parse(readFile(path));
    }

    /**
     * Разбирает содержимое конфигурационного файла и проверяет параметры.
     *
     * @param xml текст XML
     * @return проверенные параметры
     * @throws ConfigException если XML некорректен или параметры заданы неверно
     */
    public static Config parse(String xml) throws ConfigException {
        Map<String, String> values = XmlConfigReader.read(xml);
        String packageName = packageName(required(values, Config.PACKAGE));
        boolean testMode = flag(values, Config.TEST_MODE);
        String repository = required(values, Config.REPOSITORY);
        if (testMode) {
            checkTestRepository(repository);
        } else {
            checkUrl(repository);
        }
        boolean asciiTree = flag(values, Config.ASCII_TREE);
        return new Config(packageName, repository, testMode, asciiTree);
    }

    private static String readFile(String path) throws ConfigException {
        try {
            Path file = Path.of(path);
            if (Files.isDirectory(file)) {
                throw new ConfigException("config path is a directory: " + path);
            }
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            throw new ConfigException("config file not found: " + path);
        } catch (IOException | InvalidPathException e) {
            throw new ConfigException("cannot read config file '" + path + "': " + e);
        }
    }

    private static String required(Map<String, String> values, String key)
            throws ConfigException {
        String value = values.get(key);
        if (value == null) {
            throw new ConfigException("missing required parameter <" + key + ">");
        }
        if (value.isEmpty()) {
            throw new ConfigException("parameter <" + key + "> must not be empty");
        }
        return value;
    }

    private static String packageName(String value) throws ConfigException {
        if (!PACKAGE_NAME.matcher(value).matches()) {
            throw new ConfigException("invalid package name '" + value
                    + "': use letters, digits and . _ + - (first must be letter or digit)");
        }
        return value;
    }

    private static boolean flag(Map<String, String> values, String key)
            throws ConfigException {
        String value = values.getOrDefault(key, FALSE);
        if (!value.equals(TRUE) && !value.equals(FALSE)) {
            throw new ConfigException("parameter <" + key + "> must be '" + TRUE + "' or '"
                    + FALSE + "', got '" + value + "'");
        }
        return value.equals(TRUE);
    }

    private static void checkUrl(String value) throws ConfigException {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            if (scheme == null || !URL_SCHEMES.contains(scheme.toLowerCase())) {
                throw new ConfigException("repository must be an http or https URL, got '"
                        + value + "'");
            }
            if (uri.getHost() == null) {
                throw new ConfigException("repository URL has no host: '" + value + "'");
            }
        } catch (URISyntaxException e) {
            throw new ConfigException("invalid repository URL '" + value + "': "
                    + e.getReason());
        }
    }

    private static void checkTestRepository(String value) throws ConfigException {
        try {
            Path file = Path.of(value);
            if (!Files.exists(file)) {
                throw new ConfigException("test repository file not found: " + value);
            }
            if (!Files.isRegularFile(file)) {
                throw new ConfigException("test repository is not a file: " + value);
            }
        } catch (InvalidPathException e) {
            throw new ConfigException("invalid test repository path '" + value + "'");
        }
    }
}
