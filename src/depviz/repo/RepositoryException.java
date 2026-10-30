package depviz.repo;

/**
 * Ошибка получения или разбора данных репозитория.
 */
public class RepositoryException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Создаёт исключение с описанием ошибки.
     *
     * @param message текст ошибки
     */
    public RepositoryException(String message) {
        super(message);
    }
}
