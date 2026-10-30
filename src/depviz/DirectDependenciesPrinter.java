package depviz;

import depviz.apk.Dependency;
import depviz.apk.PackageInfo;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Вывод прямых зависимостей пакета.
 *
 * <p>Несколько записей, которые разрешаются в один пакет (например,
 * {@code libcurl=8.14.1-r2} и {@code so:libcurl.so.4}), выводятся одной строкой.
 */
public final class DirectDependenciesPrinter {

    private static final String NOT_FOUND = "(not found in repository)";

    private DirectDependenciesPrinter() {
    }

    /**
     * Печатает пакет и список его прямых зависимостей.
     *
     * @param out поток вывода
     * @param info анализируемый пакет
     * @param dependencies прямые зависимости пакета
     */
    public static void print(PrintStream out, PackageInfo info, List<Dependency> dependencies) {
        Map<String, List<String>> specs = new LinkedHashMap<>();
        Map<String, Boolean> found = new LinkedHashMap<>();
        for (Dependency dependency : dependencies) {
            specs.computeIfAbsent(dependency.target(), k -> new ArrayList<>())
                    .add(dependency.spec());
            found.put(dependency.target(), dependency.found());
        }
        out.println("Package: " + info.name() + " " + info.version());
        out.println("Direct dependencies (" + specs.size() + "):");
        if (specs.isEmpty()) {
            out.println("  (none)");
        }
        int width = specs.keySet().stream().mapToInt(String::length).max().orElse(0);
        for (Map.Entry<String, List<String>> entry : specs.entrySet()) {
            String target = String.format("%-" + width + "s", entry.getKey());
            String source = found.get(entry.getKey())
                    ? "<- " + String.join(", ", entry.getValue()) : NOT_FOUND;
            out.println("  " + target + "  " + source);
        }
    }
}
