package depviz.repo;

import java.util.List;

/**
 * Источник сведений о пакетах: реальный репозиторий или тестовый файл.
 */
public interface PackageSource {

    /**
     * Проверяет, есть ли пакет в источнике.
     *
     * @param name имя пакета
     * @return {@code true}, если пакет описан в источнике
     */
    boolean contains(String name);

    /**
     * Возвращает имена прямых зависимостей пакета без повторов.
     *
     * <p>Зависимость, для которой в источнике нет пакета, возвращается под
     * своим именем: для неё {@link #contains(String)} вернёт {@code false}.
     *
     * @param name имя пакета, который есть в источнике
     * @return имена зависимостей в порядке их описания
     */
    List<String> dependencies(String name);
}
