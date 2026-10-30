package depviz;

import depviz.apk.ApkIndex;
import depviz.apk.PackageInfo;
import depviz.apk.RepositoryException;
import depviz.config.Config;
import depviz.config.ConfigException;
import depviz.config.ConfigLoader;

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
     * Загружает конфигурацию, скачивает индекс репозитория и выводит прямые
     * зависимости заданного пакета.
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
        if (config.testMode()) {
            fail("test repository mode is not implemented yet");
        }
        try {
            ApkIndex index = ApkIndex.fetch(config.repository());
            PackageInfo info = index.find(config.packageName())
                    .orElseThrow(() -> new RepositoryException("package '"
                            + config.packageName() + "' not found in repository"));
            DirectDependenciesPrinter.print(System.out, info, index.directDependencies(info));
        } catch (RepositoryException e) {
            fail(e.getMessage());
        }
    }

    private static Config loadConfig(String path) {
        try {
            return ConfigLoader.load(path);
        } catch (ConfigException e) {
            fail(e.getMessage());
            return null;
        }
    }

    private static void fail(String message) {
        System.err.println("depviz: " + message);
        System.exit(EXIT_ERROR);
    }
}
