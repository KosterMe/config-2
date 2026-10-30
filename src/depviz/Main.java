package depviz;

import depviz.apk.ApkIndex;
import depviz.config.Config;
import depviz.config.ConfigException;
import depviz.config.ConfigLoader;
import depviz.graph.DependencyGraph;
import depviz.graph.GraphBuilder;
import depviz.graph.GraphPrinter;
import depviz.repo.PackageSource;
import depviz.repo.RepositoryException;
import depviz.repo.TestRepository;

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
     * Загружает конфигурацию, строит граф зависимостей заданного пакета
     * (с учётом транзитивных зависимостей) и выводит его.
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
        try {
            PackageSource source = openSource(config);
            if (!source.contains(config.packageName())) {
                throw new RepositoryException("package '" + config.packageName()
                        + "' not found in repository");
            }
            DependencyGraph graph = GraphBuilder.build(source, config.packageName());
            GraphPrinter.print(System.out, graph);
        } catch (RepositoryException e) {
            fail(e.getMessage());
        }
    }

    private static PackageSource openSource(Config config) throws RepositoryException {
        if (config.testMode()) {
            return TestRepository.load(config.repository());
        }
        return ApkIndex.fetch(config.repository());
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
