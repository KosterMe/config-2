package depviz.apk;

import depviz.repo.RepositoryException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Загрузка индекса пакетов {@code APKINDEX.tar.gz} из репозитория Alpine Linux.
 */
public final class IndexDownloader {

    /** Имя архива с индексом в каталоге репозитория. */
    public static final String INDEX_ARCHIVE = "APKINDEX.tar.gz";

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private IndexDownloader() {
    }

    /**
     * Возвращает адрес архива индекса для URL репозитория.
     *
     * <p>Если URL уже указывает на {@code APKINDEX.tar.gz}, он не меняется,
     * иначе имя архива добавляется в конец пути.
     *
     * @param repository URL каталога репозитория, например
     *     {@code https://dl-cdn.alpinelinux.org/alpine/v3.20/main/x86_64}
     * @return адрес архива индекса
     */
    public static URI indexUri(String repository) {
        String base = repository;
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return URI.create(base.endsWith(INDEX_ARCHIVE) ? base : base + "/" + INDEX_ARCHIVE);
    }

    /**
     * Скачивает архив индекса репозитория.
     *
     * @param repository URL каталога репозитория
     * @return содержимое архива {@code APKINDEX.tar.gz}
     * @throws RepositoryException при сетевой ошибке или ответе сервера не 200
     */
    public static byte[] download(String repository) throws RepositoryException {
        URI uri = indexUri(repository);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(TIMEOUT).GET().build();
        try {
            HttpResponse<byte[]> response =
                    client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                throw new RepositoryException("cannot download " + uri + ": HTTP "
                        + response.statusCode());
            }
            return response.body();
        } catch (IOException e) {
            throw new RepositoryException("cannot download " + uri + ": " + reason(e));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RepositoryException("download of " + uri + " was interrupted");
        }
    }

    private static String reason(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        String name = root.getClass().getSimpleName();
        return root.getMessage() == null ? name : name + ": " + root.getMessage();
    }
}
