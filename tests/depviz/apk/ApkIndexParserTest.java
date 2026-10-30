package depviz.apk;

import depviz.Check;
import depviz.repo.RepositoryException;
import java.util.List;

/**
 * Тесты разбора текста APKINDEX.
 */
public final class ApkIndexParserTest {

    private static final String INDEX = String.join("\n",
            "C:Q1ToUq4n7YqY0UDIjNQz92uQtjxaM=",
            "P:curl",
            "V:8.14.1-r2",
            "T:URL retrival utility and library",
            "D:libcurl=8.14.1-r2 so:libc.musl-x86_64.so.1 so:libz.so.1",
            "p:wcurl=8.14.1-r2 cmd:curl=8.14.1-r2",
            "",
            "P:busybox-binsh",
            "V:1.36.1-r31",
            "k:100",
            "p:/bin/sh cmd:sh=1.36.1-r31",
            "",
            "");

    private ApkIndexParserTest() {
    }

    /**
     * Запускает все тесты класса.
     */
    public static void run() {
        records();
        windowsLineEnds();
        baseNames();
        errors();
    }

    private static void records() {
        List<PackageInfo> packages = parse(INDEX);
        Check.equal("index: count", 2, packages.size());
        PackageInfo curl = packages.get(0);
        Check.equal("index: name", "curl", curl.name());
        Check.equal("index: version", "8.14.1-r2", curl.version());
        Check.equal("index: depends", List.of("libcurl=8.14.1-r2",
                "so:libc.musl-x86_64.so.1", "so:libz.so.1"), curl.dependencies());
        Check.equal("index: provides", List.of("wcurl", "cmd:curl"), curl.provides());
        Check.equal("index: default priority", 0, curl.priority());
        Check.equal("index: priority", 100, packages.get(1).priority());
        Check.equal("index: no depends", List.of(), packages.get(1).dependencies());
    }

    private static void windowsLineEnds() {
        List<PackageInfo> packages = parse("P:a\r\nD:b c\r\n\r\nP:b\r\n");
        Check.equal("index: crlf count", 2, packages.size());
        Check.equal("index: crlf depends", List.of("b", "c"), packages.get(0).dependencies());
    }

    private static void baseNames() {
        Check.equal("base: plain", "musl", ApkIndexParser.baseName("musl"));
        Check.equal("base: >=", "libssl3", ApkIndexParser.baseName("libssl3>=3.3.0"));
        Check.equal("base: ~", "python3", ApkIndexParser.baseName("python3~3.12"));
        Check.equal("base: <", "a", ApkIndexParser.baseName("a<2"));
        Check.equal("base: so", "so:libcurl.so.4", ApkIndexParser.baseName("so:libcurl.so.4=4.8"));
    }

    private static void errors() {
        Check.contains("index: bad line", error("P:a\nhello\n"), "invalid APKINDEX line 2");
        Check.contains("index: no name", error("P:a\n\nV:1.0\nD:b\n"),
                "record at line 3 has no package name");
        Check.contains("index: bad priority", error("P:a\nk:high\n"),
                "invalid priority 'high' at APKINDEX line 2");
    }

    private static List<PackageInfo> parse(String text) {
        try {
            return ApkIndexParser.parse(text);
        } catch (RepositoryException e) {
            throw new AssertionError("unexpected error: " + e.getMessage());
        }
    }

    private static String error(String text) {
        try {
            ApkIndexParser.parse(text);
            return "no error";
        } catch (RepositoryException e) {
            return e.getMessage();
        }
    }
}
