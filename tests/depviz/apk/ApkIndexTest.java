package depviz.apk;

import depviz.Check;
import java.util.List;
import java.util.Optional;

/**
 * Тесты поиска пакетов и разрешения зависимостей.
 */
public final class ApkIndexTest {

    private ApkIndexTest() {
    }

    /**
     * Запускает все тесты класса.
     */
    public static void run() {
        ApkIndex index = new ApkIndex(List.of(
                pkg("app", List.of("libz>=1.3", "so:libc.musl-x86_64.so.1", "/bin/sh",
                        "!old-app", "so:libmissing.so.1"), List.of(), 0),
                pkg("libz", List.of("so:libc.musl-x86_64.so.1"), List.of("so:libz.so.1"), 0),
                pkg("musl", List.of(), List.of("so:libc.musl-x86_64.so.1"), 0),
                pkg("dash-binsh", List.of(), List.of("/bin/sh"), 60),
                pkg("busybox-binsh", List.of(), List.of("/bin/sh", "cmd:sh"), 100),
                pkg("yash-binsh", List.of(), List.of("/bin/sh"), 100)));
        find(index);
        resolve(index);
        dependencies(index);
        packageSource(index);
    }

    private static void find(ApkIndex index) {
        Check.equal("apk: size", 6, index.size());
        Check.equal("apk: find", "musl", index.find("musl").map(PackageInfo::name).orElse(""));
        Check.equal("apk: find missing", Optional.empty(), index.find("nginx"));
    }

    private static void resolve(ApkIndex index) {
        Check.equal("apk: resolve name", Optional.of("libz"), index.resolve("libz"));
        Check.equal("apk: resolve so", Optional.of("libz"), index.resolve("so:libz.so.1"));
        Check.equal("apk: resolve by priority", Optional.of("busybox-binsh"),
                index.resolve("/bin/sh"));
        Check.equal("apk: resolve cmd", Optional.of("busybox-binsh"), index.resolve("cmd:sh"));
        Check.equal("apk: resolve missing", Optional.empty(), index.resolve("so:libx.so"));
    }

    private static void dependencies(ApkIndex index) {
        List<Dependency> deps = index.directDependencies(index.find("app").orElseThrow());
        Check.equal("apk: deps", List.of(
                new Dependency("libz>=1.3", "libz", true),
                new Dependency("so:libc.musl-x86_64.so.1", "musl", true),
                new Dependency("/bin/sh", "busybox-binsh", true),
                new Dependency("so:libmissing.so.1", "so:libmissing.so.1", false)), deps);
        List<Dependency> none = index.directDependencies(index.find("musl").orElseThrow());
        Check.equal("apk: no deps", List.of(), none);
    }

    private static void packageSource(ApkIndex index) {
        Check.equal("apk: contains", true, index.contains("libz"));
        Check.equal("apk: contains only real names", false, index.contains("so:libz.so.1"));
        Check.equal("apk: source deps", List.of("libz", "musl", "busybox-binsh",
                "so:libmissing.so.1"), index.dependencies("app"));
        ApkIndex twice = new ApkIndex(List.of(
                pkg("curl", List.of("libcurl=8.14", "so:libcurl.so.4"), List.of(), 0),
                pkg("libcurl", List.of(), List.of("so:libcurl.so.4"), 0)));
        Check.equal("apk: source deps distinct", List.of("libcurl"), twice.dependencies("curl"));
        Check.equal("apk: source unknown", List.of(), twice.dependencies("nginx"));
    }

    private static PackageInfo pkg(String name, List<String> deps, List<String> provides,
            int priority) {
        return new PackageInfo(name, "1.0-r0", deps, provides, priority);
    }
}
