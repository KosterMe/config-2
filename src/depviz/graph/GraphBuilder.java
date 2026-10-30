package depviz.graph;

import depviz.repo.PackageSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Построение графа зависимостей рекурсивным обходом в глубину (DFS).
 *
 * <p>Каждая вершина находится в одном из состояний: не посещена, в работе
 * (лежит на текущем пути обхода) или обработана. Ребро в вершину «в работе»
 * означает цикл: он записывается в граф, а обход по этому ребру не
 * продолжается, поэтому циклические зависимости не приводят к бесконечной
 * рекурсии. В обработанные вершины обход повторно не заходит.
 */
public final class GraphBuilder {

    private final PackageSource source;
    private final DependencyGraph graph;
    private final Map<String, State> states = new HashMap<>();
    private final List<String> path = new ArrayList<>();

    private GraphBuilder(PackageSource source, String root) {
        this.source = source;
        this.graph = new DependencyGraph(root);
    }

    /**
     * Строит граф всех прямых и транзитивных зависимостей пакета.
     *
     * @param source источник сведений о пакетах
     * @param root анализируемый пакет
     * @return граф зависимостей
     */
    public static DependencyGraph build(PackageSource source, String root) {
        GraphBuilder builder = new GraphBuilder(source, root);
        builder.visit(root);
        return builder.graph;
    }

    private void visit(String name) {
        states.put(name, State.IN_PROGRESS);
        path.add(name);
        if (source.contains(name)) {
            List<String> dependencies = source.dependencies(name);
            graph.addPackage(name, dependencies);
            for (String dependency : dependencies) {
                follow(dependency);
            }
        } else {
            graph.addMissing(name);
        }
        path.remove(path.size() - 1);
        states.put(name, State.DONE);
    }

    private void follow(String dependency) {
        State state = states.get(dependency);
        if (state == null) {
            visit(dependency);
        } else if (state == State.IN_PROGRESS) {
            List<String> cycle = new ArrayList<>(path.subList(path.indexOf(dependency),
                    path.size()));
            cycle.add(dependency);
            graph.addCycle(cycle);
        }
    }

    /**
     * Состояние вершины при обходе.
     */
    private enum State {
        /** Вершина на текущем пути обхода. */
        IN_PROGRESS,
        /** Вершина и все её зависимости обработаны. */
        DONE
    }
}
