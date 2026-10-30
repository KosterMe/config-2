package depviz.graph;

import depviz.Check;
import depviz.repo.RepositoryException;
import depviz.repo.TestRepository;
import java.util.List;

/**
 * Тесты построения графа зависимостей обходом в глубину.
 */
public final class GraphBuilderTest {

    private GraphBuilderTest() {
    }

    /**
     * Запускает все тесты класса.
     */
    public static void run() {
        chain();
        sharedDependency();
        cycle();
        selfLoop();
        severalCycles();
        missingPackages();
        noDependencies();
        deepChain();
    }

    private static void chain() {
        DependencyGraph graph = build("A: B\nB: C\nC:", "A");
        Check.equal("graph: chain order", List.of("A", "B", "C"), graph.packages());
        Check.equal("graph: chain edges", 2, graph.edgeCount());
        Check.equal("graph: chain no cycles", List.of(), graph.cycles());
    }

    private static void sharedDependency() {
        DependencyGraph graph = build("A: B C\nB: D\nC: D\nD:\nE: A", "A");
        Check.equal("graph: dfs order", List.of("A", "B", "D", "C"), graph.packages());
        Check.equal("graph: shared edges", 4, graph.edgeCount());
        Check.equal("graph: diamond is not a cycle", List.of(), graph.cycles());
    }

    private static void cycle() {
        DependencyGraph graph = build("A: B\nB: C\nC: A D\nD:", "A");
        Check.equal("graph: cycle packages", List.of("A", "B", "C", "D"), graph.packages());
        Check.equal("graph: cycle found", List.of(List.of("A", "B", "C", "A")),
                graph.cycles());
        Check.equal("graph: cycle edge kept", List.of("A", "D"), graph.dependencies("C"));
    }

    private static void selfLoop() {
        DependencyGraph graph = build("A: A B\nB:", "A");
        Check.equal("graph: self loop", List.of(List.of("A", "A")), graph.cycles());
        Check.equal("graph: self loop packages", List.of("A", "B"), graph.packages());
    }

    private static void severalCycles() {
        DependencyGraph graph = build("A: B C\nB: D\nC: D E\nD: B F\nE: A\nF:", "A");
        Check.equal("graph: two cycles", List.of(List.of("B", "D", "B"),
                List.of("A", "C", "E", "A")), graph.cycles());
        Check.equal("graph: two cycles packages", 6, graph.packages().size());
    }

    private static void missingPackages() {
        DependencyGraph graph = build("A: B X\nB: Y", "A");
        Check.equal("graph: missing packages", List.of("A", "B", "Y", "X"), graph.packages());
        Check.equal("graph: X missing", true, graph.isMissing("X"));
        Check.equal("graph: B present", false, graph.isMissing("B"));
        Check.equal("graph: missing has no deps", List.of(), graph.dependencies("Y"));
    }

    private static void noDependencies() {
        DependencyGraph graph = build("A:\nB: A", "A");
        Check.equal("graph: single package", List.of("A"), graph.packages());
        Check.equal("graph: root", "A", graph.root());
    }

    private static void deepChain() {
        StringBuilder text = new StringBuilder();
        for (char c = 'A'; c < 'Z'; c++) {
            text.append(c).append(": ").append((char) (c + 1)).append('\n');
        }
        text.append("Z: A\n");
        DependencyGraph graph = build(text.toString(), "A");
        Check.equal("graph: 26 packages", 26, graph.packages().size());
        Check.equal("graph: long cycle length", 27, graph.cycles().get(0).size());
    }

    private static DependencyGraph build(String text, String root) {
        try {
            return GraphBuilder.build(TestRepository.parse(text), root);
        } catch (RepositoryException e) {
            throw new AssertionError("unexpected error: " + e.getMessage());
        }
    }
}
