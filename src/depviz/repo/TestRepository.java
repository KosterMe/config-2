package depviz.repo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Тестовый репозиторий, описанный текстовым файлом.
 *
 * <p>Каждая непустая строка описывает один пакет и его прямые зависимости:
 * {@code A: B C}. Имена пакетов состоят из больших латинских букв. Пустые
 * строки и строки, начинающиеся с {@code #}, пропускаются. Пакет, который
 * упомянут только как зависимость, считается отсутствующим в репозитории.
 */
public final class TestRepository implements PackageSource {

    private static final Pattern NAME = Pattern.compile("[A-Z]+");
    private static final String COMMENT = "#";
    private static final String SEPARATOR = ":";

    private final Map<String, List<String>> packages;

    private TestRepository(Map<String, List<String>> packages) {
        this.packages = packages;
    }

    /**
     * Читает тестовый репозиторий из файла.
     *
     * @param path путь к файлу описания графа
     * @return тестовый репозиторий
     * @throws RepositoryException если файл недоступен или имеет неверный формат
     */
    public static TestRepository load(String path) throws RepositoryException {
        try {
            return parse(Files.readString(Path.of(path), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RepositoryException("cannot read test repository '" + path + "': " + e);
        }
    }

    /**
     * Разбирает описание тестового репозитория.
     *
     * @param text содержимое файла
     * @return тестовый репозиторий
     * @throws RepositoryException если строка имеет неверный формат
     */
    public static TestRepository parse(String text) throws RepositoryException {
        Map<String, List<String>> packages = new LinkedHashMap<>();
        Map<String, Integer> definedAt = new HashMap<>();
        String[] lines = text.split("\\R", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].strip();
            if (line.isEmpty() || line.startsWith(COMMENT)) {
                continue;
            }
            int number = i + 1;
            String name = definition(line, number);
            if (definedAt.containsKey(name)) {
                throw error(number, "package '" + name + "' is already defined at line "
                        + definedAt.get(name));
            }
            definedAt.put(name, number);
            packages.put(name, dependencies(line, number));
        }
        return new TestRepository(packages);
    }

    @Override
    public boolean contains(String name) {
        return packages.containsKey(name);
    }

    @Override
    public List<String> dependencies(String name) {
        return packages.getOrDefault(name, List.of());
    }

    private static String definition(String line, int number) throws RepositoryException {
        int colon = line.indexOf(SEPARATOR);
        if (colon < 0) {
            throw error(number, "expected 'NAME: DEPENDENCIES', got '" + line + "'");
        }
        return checkName(line.substring(0, colon).strip(), number);
    }

    private static List<String> dependencies(String line, int number)
            throws RepositoryException {
        String rest = line.substring(line.indexOf(SEPARATOR) + 1).strip();
        List<String> result = new ArrayList<>();
        if (rest.isEmpty()) {
            return result;
        }
        for (String dependency : Arrays.asList(rest.split("[\\s,]+"))) {
            checkName(dependency, number);
            if (result.contains(dependency)) {
                throw error(number, "dependency '" + dependency + "' is listed twice");
            }
            result.add(dependency);
        }
        return result;
    }

    private static String checkName(String name, int number) throws RepositoryException {
        if (!NAME.matcher(name).matches()) {
            throw error(number, "invalid package name '" + name
                    + "': use capital Latin letters A-Z");
        }
        return name;
    }

    private static RepositoryException error(int line, String message) {
        return new RepositoryException("test repository line " + line + ": " + message);
    }
}
