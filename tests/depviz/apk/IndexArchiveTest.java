package depviz.apk;

import depviz.Check;
import depviz.repo.RepositoryException;
import java.nio.charset.StandardCharsets;

/**
 * Тесты извлечения APKINDEX из архива.
 */
public final class IndexArchiveTest {

    private static final String INDEX = "P:musl\nV:1.2.5-r0\n\n";

    private IndexArchiveTest() {
    }

    /**
     * Запускает все тесты класса.
     */
    public static void run() {
        signedArchive();
        dotSlashName();
        noIndex();
        notGzip();
        truncatedEntry();
        badSize();
    }

    private static void signedArchive() {
        byte[] signature = new ArchiveBuilder().file(".SIGN.RSA.key.rsa.pub", "sig").tar();
        byte[] index = new ArchiveBuilder().file("DESCRIPTION", "v3.20")
                .file("APKINDEX", INDEX).end().tar();
        Check.equal("archive: index from second gzip member", INDEX,
                extract(ArchiveBuilder.gzip(signature, index)));
    }

    private static void dotSlashName() {
        byte[] tar = new ArchiveBuilder().file("./APKINDEX", INDEX).end().tar();
        Check.equal("archive: ./APKINDEX", INDEX, extract(ArchiveBuilder.gzip(tar)));
    }

    private static void noIndex() {
        byte[] tar = new ArchiveBuilder().file("DESCRIPTION", "v3.20").end().tar();
        Check.contains("archive: no index", error(ArchiveBuilder.gzip(tar)),
                "APKINDEX not found");
    }

    private static void notGzip() {
        byte[] text = "<html>Not Found</html>".getBytes(StandardCharsets.US_ASCII);
        Check.contains("archive: not gzip", error(text), "invalid index archive");
    }

    private static void truncatedEntry() {
        byte[] tar = new ArchiveBuilder().entry("APKINDEX", "00000010000", new byte[1]).tar();
        Check.contains("archive: truncated", error(ArchiveBuilder.gzip(tar)),
                "truncated tar entry 'APKINDEX'");
    }

    private static void badSize() {
        byte[] tar = new ArchiveBuilder().entry("APKINDEX", "12x", new byte[0]).end().tar();
        Check.contains("archive: bad size", error(ArchiveBuilder.gzip(tar)),
                "invalid size '12x'");
    }

    private static String extract(byte[] archive) {
        try {
            return IndexArchive.extractIndex(archive);
        } catch (RepositoryException e) {
            throw new AssertionError("unexpected error: " + e.getMessage());
        }
    }

    private static String error(byte[] archive) {
        try {
            IndexArchive.extractIndex(archive);
            return "no error";
        } catch (RepositoryException e) {
            return e.getMessage();
        }
    }
}
