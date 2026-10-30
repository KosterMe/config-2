package depviz;

import java.util.Objects;

/**
 * Простейшие проверки для тестов (без внешних библиотек).
 */
public final class Check {

    private static int passed;
    private static int failed;

    private Check() {
    }

    /**
     * Проверяет, что условие истинно.
     *
     * @param name название проверки
     * @param condition проверяемое условие
     */
    public static void isTrue(String name, boolean condition) {
        if (condition) {
            passed++;
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }

    /**
     * Проверяет, что значения равны.
     *
     * @param name название проверки
     * @param expected ожидаемое значение
     * @param actual фактическое значение
     */
    public static void equal(String name, Object expected, Object actual) {
        boolean ok = Objects.equals(expected, actual);
        isTrue(name, ok);
        if (!ok) {
            System.out.println("  expected: " + expected);
            System.out.println("  actual:   " + actual);
        }
    }

    /**
     * Проверяет, что строка содержит подстроку.
     *
     * @param name название проверки
     * @param text проверяемая строка
     * @param part ожидаемая подстрока
     */
    public static void contains(String name, String text, String part) {
        boolean ok = text.contains(part);
        isTrue(name, ok);
        if (!ok) {
            System.out.println("  text: " + text);
            System.out.println("  must contain: " + part);
        }
    }

    /**
     * Возвращает число проваленных проверок.
     *
     * @return количество ошибок
     */
    public static int failed() {
        return failed;
    }

    /**
     * Возвращает число успешных проверок.
     *
     * @return количество успешных проверок
     */
    public static int passed() {
        return passed;
    }
}
