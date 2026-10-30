package depviz.graph;

import depviz.Check;
import depviz.repo.RepositoryException;
import depviz.repo.TestRepository;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Тесты текстового вывода графа.
 */
public final class GraphPrinterTest {

    private GraphPrinterTest() {
    }

    /**
     * Запускает все тесты класса.
     */
    public static void run() {
        withCycle();
        withoutCycles();
    }

    private static void withCycle() {
        String text = print("A: B X\nB: A C\nC:", "A");
        Check.equal("print: graph with cycle", String.join("\n",
                "Dependency graph of A (packages: 4, edges: 4):",
                "  A -> B, X",
                "  B -> A, C",
                "  C (no dependencies)",
                "  X (not found in repository)",
                "Cycles (1):",
                "  A -> B -> A",
                ""), text);
    }

    private static void withoutCycles() {
        String text = print("A: B\nB:", "A");
        Check.contains("print: no cycles", text, "Cycles: none");
        Check.contains("print: counts", text, "Dependency graph of A (packages: 2, edges: 1):");
    }

    private static String print(String repository, String root) {
        try {
            DependencyGraph graph = GraphBuilder.build(TestRepository.parse(repository), root);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            GraphPrinter.print(new PrintStream(buffer, true, StandardCharsets.UTF_8), graph);
            return buffer.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        } catch (RepositoryException e) {
            throw new AssertionError("unexpected error: " + e.getMessage());
        }
    }
}
