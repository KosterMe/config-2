package depviz.apk;

import depviz.Check;

/**
 * Тесты построения адреса индекса репозитория.
 */
public final class IndexDownloaderTest {

    private static final String REPO = "https://dl-cdn.alpinelinux.org/alpine/v3.20/main/x86_64";
    private static final String INDEX = REPO + "/APKINDEX.tar.gz";

    private IndexDownloaderTest() {
    }

    /**
     * Запускает все тесты класса.
     */
    public static void run() {
        Check.equal("url: directory", INDEX, IndexDownloader.indexUri(REPO).toString());
        Check.equal("url: trailing slash", INDEX,
                IndexDownloader.indexUri(REPO + "/").toString());
        Check.equal("url: archive itself", INDEX, IndexDownloader.indexUri(INDEX).toString());
    }
}
