package depviz;

import depviz.apk.ApkIndexParserTest;
import depviz.apk.ApkIndexTest;
import depviz.apk.IndexArchiveTest;
import depviz.apk.IndexDownloaderTest;
import depviz.config.ConfigLoaderTest;
import depviz.graph.GraphBuilderTest;
import depviz.graph.GraphPrinterTest;
import depviz.repo.TestRepositoryTest;

/**
 * Запуск всех тестов проекта.
 */
public final class TestRunner {

    private TestRunner() {
    }

    /**
     * Выполняет тесты и печатает итог. Запускать из корня репозитория.
     *
     * @param args не используются
     */
    public static void main(String[] args) {
        ConfigLoaderTest.run();
        IndexDownloaderTest.run();
        IndexArchiveTest.run();
        ApkIndexParserTest.run();
        ApkIndexTest.run();
        TestRepositoryTest.run();
        GraphBuilderTest.run();
        GraphPrinterTest.run();
        System.out.println("Tests passed: " + Check.passed() + ", failed: " + Check.failed());
        if (Check.failed() > 0) {
            System.exit(1);
        }
    }
}
