package depviz.apk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Индекс пакетов репозитория Alpine Linux с поиском пакетов и разрешением
 * зависимостей.
 *
 * <p>Зависимость может быть именем пакета ({@code musl}), именем с
 * ограничением версии ({@code libssl3>=3.3.0}) или виртуальным именем
 * ({@code so:libz.so.1}, {@code cmd:sh}, {@code /bin/sh}). Виртуальное имя
 * разрешается в пакет, который его предоставляет (поле {@code p:}); если
 * таких пакетов несколько, выбирается пакет с наибольшим приоритетом
 * {@code k:}, при равенстве — первый в индексе. Записи вида {@code !pkg}
 * означают конфликт, а не зависимость, и пропускаются.
 */
public final class ApkIndex {

    private static final String CONFLICT = "!";

    private final Map<String, PackageInfo> packages = new LinkedHashMap<>();
    private final Map<String, PackageInfo> providers = new HashMap<>();

    /**
     * Строит индекс по списку пакетов.
     *
     * @param list пакеты в порядке следования в {@code APKINDEX}
     */
    public ApkIndex(List<PackageInfo> list) {
        for (PackageInfo info : list) {
            packages.putIfAbsent(info.name(), info);
            for (String provided : info.provides()) {
                providers.merge(provided, info, ApkIndex::preferred);
            }
        }
    }

    /**
     * Скачивает и разбирает индекс репозитория.
     *
     * @param repository URL каталога репозитория
     * @return индекс пакетов
     * @throws RepositoryException при ошибке загрузки или разбора
     */
    public static ApkIndex fetch(String repository) throws RepositoryException {
        byte[] archive = IndexDownloader.download(repository);
        return new ApkIndex(ApkIndexParser.parse(IndexArchive.extractIndex(archive)));
    }

    /**
     * Возвращает число пакетов в индексе.
     *
     * @return количество пакетов
     */
    public int size() {
        return packages.size();
    }

    /**
     * Ищет пакет по точному имени.
     *
     * @param name имя пакета
     * @return описание пакета, если он есть в индексе
     */
    public Optional<PackageInfo> find(String name) {
        return Optional.ofNullable(packages.get(name));
    }

    /**
     * Находит пакет, который удовлетворяет зависимости с заданным именем.
     *
     * @param name имя пакета или виртуальное имя без версии
     * @return имя пакета, если он найден
     */
    public Optional<String> resolve(String name) {
        if (packages.containsKey(name)) {
            return Optional.of(name);
        }
        return Optional.ofNullable(providers.get(name)).map(PackageInfo::name);
    }

    /**
     * Возвращает прямые зависимости пакета в порядке из индекса.
     *
     * @param info пакет
     * @return зависимости без записей-конфликтов
     */
    public List<Dependency> directDependencies(PackageInfo info) {
        List<Dependency> result = new ArrayList<>();
        for (String spec : info.dependencies()) {
            if (spec.startsWith(CONFLICT)) {
                continue;
            }
            String name = ApkIndexParser.baseName(spec);
            Optional<String> target = resolve(name);
            result.add(new Dependency(spec, target.orElse(name), target.isPresent()));
        }
        return result;
    }

    private static PackageInfo preferred(PackageInfo current, PackageInfo candidate) {
        return candidate.priority() > current.priority() ? candidate : current;
    }
}
