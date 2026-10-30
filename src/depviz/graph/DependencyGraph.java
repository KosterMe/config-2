package depviz.graph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Граф зависимостей пакета с учётом транзитивных зависимостей.
 *
 * <p>Вершины хранятся в порядке их обнаружения обходом в глубину.
 * Отдельно хранятся пакеты, которых нет в репозитории, и найденные циклы.
 */
public final class DependencyGraph {

    private final String root;
    private final Map<String, List<String>> edges = new LinkedHashMap<>();
    private final Set<String> missing = new LinkedHashSet<>();
    private final List<List<String>> cycles = new ArrayList<>();

    /**
     * Создаёт пустой граф.
     *
     * @param root анализируемый пакет
     */
    DependencyGraph(String root) {
        this.root = root;
    }

    /**
     * Добавляет пакет из репозитория вместе с его прямыми зависимостями.
     *
     * @param name имя пакета
     * @param dependencies прямые зависимости
     */
    void addPackage(String name, List<String> dependencies) {
        edges.put(name, List.copyOf(dependencies));
    }

    /**
     * Добавляет пакет, которого нет в репозитории.
     *
     * @param name имя пакета
     */
    void addMissing(String name) {
        edges.put(name, List.of());
        missing.add(name);
    }

    /**
     * Добавляет найденный цикл.
     *
     * @param cycle пакеты цикла, первый и последний совпадают
     */
    void addCycle(List<String> cycle) {
        cycles.add(List.copyOf(cycle));
    }

    /**
     * Возвращает анализируемый пакет.
     *
     * @return имя корневого пакета
     */
    public String root() {
        return root;
    }

    /**
     * Возвращает все пакеты графа в порядке обнаружения.
     *
     * @return имена пакетов, включая отсутствующие в репозитории
     */
    public List<String> packages() {
        return List.copyOf(edges.keySet());
    }

    /**
     * Возвращает прямые зависимости пакета из графа.
     *
     * @param name имя пакета
     * @return зависимости или пустой список
     */
    public List<String> dependencies(String name) {
        return edges.getOrDefault(name, List.of());
    }

    /**
     * Проверяет, что пакета нет в репозитории.
     *
     * @param name имя пакета
     * @return {@code true}, если пакет упомянут как зависимость, но не найден
     */
    public boolean isMissing(String name) {
        return missing.contains(name);
    }

    /**
     * Возвращает число рёбер графа.
     *
     * @return количество пар «пакет — зависимость»
     */
    public int edgeCount() {
        return edges.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Возвращает найденные циклы.
     *
     * <p>Каждый цикл начинается и заканчивается одним и тем же пакетом,
     * например {@code [A, B, C, A]}.
     *
     * @return циклы в порядке обнаружения
     */
    public List<List<String>> cycles() {
        return List.copyOf(cycles);
    }
}
