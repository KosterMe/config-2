package depviz.graph;

import java.io.PrintStream;
import java.util.List;

/**
 * Текстовый вывод графа зависимостей в виде списка смежности.
 */
public final class GraphPrinter {

    private static final String ARROW = " -> ";

    private GraphPrinter() {
    }

    /**
     * Печатает пакеты графа с их зависимостями и найденные циклы.
     *
     * @param out поток вывода
     * @param graph граф зависимостей
     */
    public static void print(PrintStream out, DependencyGraph graph) {
        out.println("Dependency graph of " + graph.root() + " (packages: "
                + graph.packages().size() + ", edges: " + graph.edgeCount() + "):");
        for (String name : graph.packages()) {
            out.println("  " + describe(graph, name));
        }
        List<List<String>> cycles = graph.cycles();
        if (cycles.isEmpty()) {
            out.println("Cycles: none");
            return;
        }
        out.println("Cycles (" + cycles.size() + "):");
        for (List<String> cycle : cycles) {
            out.println("  " + String.join(ARROW, cycle));
        }
    }

    private static String describe(DependencyGraph graph, String name) {
        if (graph.isMissing(name)) {
            return name + " (not found in repository)";
        }
        List<String> dependencies = graph.dependencies(name);
        if (dependencies.isEmpty()) {
            return name + " (no dependencies)";
        }
        return name + ARROW + String.join(", ", dependencies);
    }
}
