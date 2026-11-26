package gym.exception;

/**
 * Custom checked exception thrown when reading or writing persistence files fails
 * or encounters corrupted records.
 * Demonstrates: Custom Exception Handling + File I/O error recovery.
 */
public class DataStorageException extends Exception {
    public DataStorageException(String message) {
        super(message);
    }

    public DataStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
