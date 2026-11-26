package gym.exception;

/**
 * Custom checked exception thrown when attempting to add an entity with a duplicate ID.
 * Demonstrates: Custom Exception Handling (Requirement 4 & Rubric Criterion 3).
 */
public class DuplicateEntityException extends Exception {
    public DuplicateEntityException(String message) {
        super(message);
    }
}
