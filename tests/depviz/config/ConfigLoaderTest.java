package depviz.config;

import depviz.Check;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Тесты загрузки и проверки конфигурационного файла.
 */
public final class ConfigLoaderTest {

    private static final String URL = "https://dl-cdn.alpinelinux.org/alpine/v3.20/main/x86_64";

    private ConfigLoaderTest() {
    }

    /**
     * Запускает все тесты класса.
     */
    public static void run() {
        allParameters();
        defaultFlags();
        testModeWithFile();
        keyValues();
        structureErrors();
        valueErrors();
        fileErrors();
    }

    private static void allParameters() {
        Config config = parse(xml("<package>curl</package>",
                "<repository>" + URL + "</repository>", "<test-mode>false</test-mode>",
                "<ascii-tree>true</ascii-tree>"));
        Check.equal("config: package", "curl", config.packageName());
        Check.equal("config: repository", URL, config.repository());
        Check.equal("config: test mode", false, config.testMode());
        Check.equal("config: ascii tree", true, config.asciiTree());
    }

    private static void defaultFlags() {
        Config config = parse(xml("<repository> " + URL + " </repository>",
                "<!-- comment --><package>musl</package>"));
        Check.equal("config: trimmed value", URL, config.repository());
        Check.equal("config: default test mode", false, config.testMode());
        Check.equal("config: default ascii tree", false, config.asciiTree());
    }

    private static void testModeWithFile() {
        Path file = tempFile();
        Config config = parse(xml("<package>A</package>", "<repository>" + file
                + "</repository>", "<test-mode>true</test-mode>"));
        Check.equal("config: test repository path", file.toString(), config.repository());
        Check.equal("config: test mode on", true, config.testMode());
    }

    private static void keyValues() {
        Config config = new Config("curl", URL, false, true);
        Check.equal("config: keys order", List.of("package", "repository", "test-mode",
                "ascii-tree"), List.copyOf(config.toKeyValues().keySet()));
        Check.equal("config: flag value", "true", config.toKeyValues().get("ascii-tree"));
    }

    private static void structureErrors() {
        Check.contains("error: not xml", error("hello"), "invalid XML at line 1");
        Check.contains("error: unclosed tag", error("<config><package>a</config>"),
                "invalid XML");
        Check.contains("error: root", error("<settings/>"), "root element must be <config>");
        Check.contains("error: unknown", error(xml("<depth>3</depth>")),
                "unknown parameter <depth>");
        Check.contains("error: duplicate", error(xml("<package>a</package>",
                "<package>b</package>")), "<package> is specified twice");
        Check.contains("error: nested", error(xml("<package><name>a</name></package>")),
                "must contain only text");
        Check.contains("error: text in root", error("<config>curl</config>"),
                "unexpected text inside <config>");
        Check.contains("error: doctype", error("<!DOCTYPE config []><config/>"),
                "invalid XML");
    }

    private static void valueErrors() {
        String repo = "<repository>" + URL + "</repository>";
        Check.contains("error: no package", error(xml(repo)),
                "missing required parameter <package>");
        Check.contains("error: empty package", error(xml("<package> </package>", repo)),
                "<package> must not be empty");
        Check.contains("error: bad package", error(xml("<package>curl lib</package>", repo)),
                "invalid package name 'curl lib'");
        Check.contains("error: no repository", error(xml("<package>a</package>")),
                "missing required parameter <repository>");
        Check.contains("error: ftp url", error(xml("<package>a</package>",
                "<repository>ftp://host/repo</repository>")), "must be an http or https URL");
        Check.contains("error: no scheme", error(xml("<package>a</package>",
                "<repository>alpine/main</repository>")), "must be an http or https URL");
        Check.contains("error: bad url", error(xml("<package>a</package>",
                "<repository>http://a b</repository>")), "invalid repository URL");
        Check.contains("error: test mode", error(xml("<package>a</package>", repo,
                "<test-mode>yes</test-mode>")), "<test-mode> must be 'true' or 'false'");
        Check.contains("error: ascii tree", error(xml("<package>a</package>", repo,
                "<ascii-tree>1</ascii-tree>")), "<ascii-tree> must be 'true' or 'false'");
    }

    private static void fileErrors() {
        Check.contains("error: no test file", error(xml("<package>A</package>",
                "<repository>no/such/file.txt</repository>", "<test-mode>true</test-mode>")),
                "test repository file not found: no/such/file.txt");
        Check.contains("error: test dir", error(xml("<package>A</package>",
                "<repository>.</repository>", "<test-mode>true</test-mode>")),
                "test repository is not a file");
        Check.contains("error: no config", loadError("no/such/config.xml"),
                "config file not found: no/such/config.xml");
        Check.contains("error: config dir", loadError("."), "config path is a directory");
    }

    private static String xml(String... parameters) {
        return "<?xml version=\"1.0\"?>\n<config>\n" + String.join("\n", parameters)
                + "\n</config>\n";
    }

    private static Path tempFile() {
        try {
            Path file = Files.createTempFile("depviz", ".txt");
            file.toFile().deleteOnExit();
            return file;
        } catch (IOException e) {
            throw new AssertionError("cannot create temp file: " + e);
        }
    }

    private static Config parse(String xml) {
        try {
            return ConfigLoader.parse(xml);
        } catch (ConfigException e) {
            throw new AssertionError("unexpected error: " + e.getMessage());
        }
    }

    private static String error(String xml) {
        try {
            ConfigLoader.parse(xml);
            return "no error";
        } catch (ConfigException e) {
            return e.getMessage();
        }
    }

    private static String loadError(String path) {
        try {
            ConfigLoader.load(path);
            return "no error";
        } catch (ConfigException e) {
            return e.getMessage();
        }
    }
}
