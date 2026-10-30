package depviz.apk;

import depviz.repo.RepositoryException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/**
 * Извлечение файла {@code APKINDEX} из архива {@code APKINDEX.tar.gz}.
 *
 * <p>Архив Alpine — это несколько склеенных потоков gzip (подпись и сам
 * индекс), каждый из которых содержит фрагмент архива tar. Разбор tar
 * выполняется вручную: заголовки по 512 байт, имя файла в первых 100 байтах,
 * размер — восьмеричное число по смещению 124.
 */
public final class IndexArchive {

    /** Имя файла индекса внутри архива. */
    public static final String INDEX_FILE = "APKINDEX";

    private static final int BLOCK_SIZE = 512;
    private static final int NAME_LENGTH = 100;
    private static final int SIZE_OFFSET = 124;
    private static final int SIZE_LENGTH = 12;
    private static final int OCTAL = 8;
    private static final String CURRENT_DIR = "./";

    private IndexArchive() {
    }

    /**
     * Распаковывает архив и возвращает текст файла {@code APKINDEX}.
     *
     * @param archive содержимое {@code APKINDEX.tar.gz}
     * @return текст индекса
     * @throws RepositoryException если архив повреждён или не содержит индекса
     */
    public static String extractIndex(byte[] archive) throws RepositoryException {
        byte[] tar = gunzip(archive);
        int offset = 0;
        while (offset + BLOCK_SIZE <= tar.length) {
            if (isZeroBlock(tar, offset)) {
                offset += BLOCK_SIZE;
                continue;
            }
            String name = entryName(tar, offset);
            int size = entrySize(tar, offset, name);
            int data = offset + BLOCK_SIZE;
            if (size > tar.length - data) {
                throw new RepositoryException("truncated tar entry '" + name + "'");
            }
            if (name.equals(INDEX_FILE)) {
                return new String(tar, data, size, StandardCharsets.UTF_8);
            }
            offset = data + padded(size);
        }
        throw new RepositoryException(INDEX_FILE + " not found in index archive");
    }

    private static byte[] gunzip(byte[] archive) throws RepositoryException {
        try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(archive))) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new RepositoryException("invalid index archive: " + e.getMessage());
        }
    }

    private static boolean isZeroBlock(byte[] tar, int offset) {
        for (int i = offset; i < offset + BLOCK_SIZE; i++) {
            if (tar[i] != 0) {
                return false;
            }
        }
        return true;
    }

    private static String entryName(byte[] tar, int offset) {
        String name = field(tar, offset, NAME_LENGTH);
        return name.startsWith(CURRENT_DIR) ? name.substring(CURRENT_DIR.length()) : name;
    }

    private static int entrySize(byte[] tar, int offset, String name)
            throws RepositoryException {
        String text = field(tar, offset + SIZE_OFFSET, SIZE_LENGTH).trim();
        int size;
        try {
            size = Integer.parseInt(text, OCTAL);
        } catch (NumberFormatException e) {
            size = -1;
        }
        if (size < 0) {
            throw new RepositoryException("invalid size '" + text + "' of tar entry '"
                    + name + "'");
        }
        return size;
    }

    private static String field(byte[] tar, int offset, int length) {
        int end = offset;
        while (end < offset + length && tar[end] != 0) {
            end++;
        }
        return new String(tar, offset, end - offset, StandardCharsets.US_ASCII);
    }

    private static int padded(int size) {
        return (size + BLOCK_SIZE - 1) / BLOCK_SIZE * BLOCK_SIZE;
    }
}
