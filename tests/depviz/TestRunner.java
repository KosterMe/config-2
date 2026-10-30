package depviz;

import depviz.apk.ApkIndexParserTest;
import depviz.apk.ApkIndexTest;
import depviz.apk.IndexArchiveTest;
import depviz.apk.IndexDownloaderTest;
import depviz.config.ConfigLoaderTest;

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
        DirectDependenciesPrinterTest.run();
        System.out.println("Tests passed: " + Check.passed() + ", failed: " + Check.failed());
        if (Check.failed() > 0) {
            System.exit(1);
        }
    }
}
