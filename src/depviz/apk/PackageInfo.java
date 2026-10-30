package depviz.apk;

import java.util.List;

/**
 * Описание пакета из индекса {@code APKINDEX}.
 *
 * @param name имя пакета (поле {@code P:})
 * @param version версия пакета (поле {@code V:})
 * @param dependencies зависимости в исходной записи (поле {@code D:}),
 *     например {@code so:libz.so.1} или {@code libssl3>=3.3.0}
 * @param provides имена, которые предоставляет пакет, без версий (поле {@code p:})
 * @param priority приоритет среди пакетов с одинаковыми {@code provides} (поле {@code k:})
 */
public record PackageInfo(String name, String version, List<String> dependencies,
        List<String> provides, int priority) {

    /**
     * Создаёт описание пакета, копируя списки.
     */
    public PackageInfo {
        dependencies = List.copyOf(dependencies);
        provides = List.copyOf(provides);
    }
}
