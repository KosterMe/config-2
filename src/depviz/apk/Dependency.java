package depviz.apk;

/**
 * Прямая зависимость пакета.
 *
 * @param spec запись зависимости из индекса, например {@code so:libz.so.1}
 * @param target имя пакета, который удовлетворяет зависимости, или имя
 *     зависимости без версии, если такого пакета в репозитории нет
 * @param found {@code true}, если пакет найден в репозитории
 */
public record Dependency(String spec, String target, boolean found) {
}
