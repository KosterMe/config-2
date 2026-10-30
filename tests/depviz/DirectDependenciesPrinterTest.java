package depviz;

import depviz.apk.Dependency;
import depviz.apk.PackageInfo;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Тесты вывода прямых зависимостей.
 */
public final class DirectDependenciesPrinterTest {

    private static final PackageInfo CURL =
            new PackageInfo("curl", "8.14.1-r2", List.of(), List.of(), 0);

    private DirectDependenciesPrinterTest() {
    }

    /**
     * Запускает все тесты класса.
     */
    public static void run() {
        grouped();
        empty();
    }

    private static void grouped() {
        String text = print(List.of(
                new Dependency("libcurl=8.14.1-r2", "libcurl", true),
                new Dependency("so:libc.musl-x86_64.so.1", "musl", true),
                new Dependency("so:libcurl.so.4", "libcurl", true),
                new Dependency("so:libx.so.1", "so:libx.so.1", false)));
        Check.contains("print: header", text, "Package: curl 8.14.1-r2");
        Check.contains("print: count", text, "Direct dependencies (3):");
        Check.contains("print: grouped specs", text,
                "  libcurl       <- libcurl=8.14.1-r2, so:libcurl.so.4");
        Check.contains("print: single spec", text, "  musl          <- so:libc.musl-x86_64.so.1");
        Check.contains("print: not found", text, "  so:libx.so.1  (not found in repository)");
    }

    private static void empty() {
        String text = print(List.of());
        Check.contains("print: none count", text, "Direct dependencies (0):");
        Check.contains("print: none", text, "  (none)");
    }

    private static String print(List<Dependency> dependencies) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        DirectDependenciesPrinter.print(out, CURL, dependencies);
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
