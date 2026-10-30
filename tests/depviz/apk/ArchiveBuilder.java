package depviz.apk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

/**
 * Сборка архивов tar.gz в памяти для тестов.
 */
final class ArchiveBuilder {

    private static final int BLOCK_SIZE = 512;
    private static final int SIZE_OFFSET = 124;
    private static final int END_BLOCKS = 2;

    private final ByteArrayOutputStream tar = new ByteArrayOutputStream();

    /**
     * Добавляет файл в архив.
     *
     * @param name имя файла
     * @param content содержимое
     * @return этот же построитель
     */
    ArchiveBuilder file(String name, String content) {
        byte[] data = content.getBytes(StandardCharsets.UTF_8);
        return entry(name, String.format("%011o", data.length), data);
    }

    /**
     * Добавляет запись с произвольным полем размера и данными.
     *
     * @param name имя файла
     * @param size текст поля размера
     * @param data данные записи
     * @return этот же построитель
     */
    ArchiveBuilder entry(String name, String size, byte[] data) {
        byte[] header = new byte[BLOCK_SIZE];
        put(header, 0, name);
        put(header, SIZE_OFFSET, size);
        tar.writeBytes(header);
        tar.writeBytes(data);
        tar.writeBytes(new byte[(BLOCK_SIZE - data.length % BLOCK_SIZE) % BLOCK_SIZE]);
        return this;
    }

    /**
     * Добавляет блоки конца архива.
     *
     * @return этот же построитель
     */
    ArchiveBuilder end() {
        tar.writeBytes(new byte[BLOCK_SIZE * END_BLOCKS]);
        return this;
    }

    /**
     * Возвращает архив tar без сжатия.
     *
     * @return байты архива
     */
    byte[] tar() {
        return tar.toByteArray();
    }

    /**
     * Сжимает части в отдельные потоки gzip и склеивает их, как в APKINDEX.tar.gz.
     *
     * @param parts части архива
     * @return склеенные потоки gzip
     */
    static byte[] gzip(byte[]... parts) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        for (byte[] part : parts) {
            try (OutputStream out = new GZIPOutputStream(result)) {
                out.write(part);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        return result.toByteArray();
    }

    private static void put(byte[] header, int offset, String text) {
        byte[] bytes = text.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(bytes, 0, header, offset, bytes.length);
    }
}
