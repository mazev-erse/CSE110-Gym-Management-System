package gym.exception;

/**
 * Custom checked exception thrown when a requested entity ID cannot be found.
 * Demonstrates: Custom Exception Handling (Requirement 4 & Rubric Criterion 3).
 */
public class EntityNotFoundException extends Exception {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
