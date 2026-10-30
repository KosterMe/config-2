package depviz;

import depviz.config.Config;
import depviz.config.ConfigException;
import depviz.config.ConfigLoader;
import java.util.Map;

/**
 * Точка входа инструмента визуализации графа зависимостей.
 */
public final class Main {

    private static final String DEFAULT_CONFIG = "config.xml";
    private static final String USAGE = "usage: depviz [config.xml]";
    private static final int MAX_ARGS = 1;
    private static final int EXIT_ERROR = 1;
    private static final int EXIT_USAGE = 2;

    private Main() {
    }

    /**
     * Загружает конфигурацию и выводит все параметры в формате ключ-значение.
     *
     * @param args путь к конфигурационному файлу (по умолчанию {@code config.xml})
     */
    public static void main(String[] args) {
        if (args.length > MAX_ARGS) {
            System.err.println("depviz: too many arguments");
            System.err.println(USAGE);
            System.exit(EXIT_USAGE);
        }
        Config config = loadConfig(args.length == 0 ? DEFAULT_CONFIG : args[0]);
        for (Map.Entry<String, String> entry : config.toKeyValues().entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }

    private static Config loadConfig(String path) {
        try {
            return ConfigLoader.load(path);
        } catch (ConfigException e) {
            System.err.println("depviz: " + e.getMessage());
            System.exit(EXIT_ERROR);
            return null;
        }
    }
}
