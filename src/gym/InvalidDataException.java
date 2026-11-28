package gym;

// Custom Exception 3: Thrown when user inputs fail validation rules.
public class InvalidDataException extends Exception {
    public InvalidDataException(String message) {
        super(message);
    }
}
