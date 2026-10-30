package depviz.config;

/**
 * Ошибка в конфигурационном файле или в значении параметра.
 */
public class ConfigException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Создаёт исключение с описанием ошибки.
     *
     * @param message текст ошибки
     */
    public ConfigException(String message) {
        super(message);
    }
}
