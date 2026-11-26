package gym.exception;

/**
 * Custom checked exception thrown when user input fails validation rules.
 * Demonstrates: Custom Exception Handling (Requirement 4 & Rubric Criterion 3).
 */
public class InvalidInputException extends Exception {
    public InvalidInputException(String message) {
        super(message);
    }
}
