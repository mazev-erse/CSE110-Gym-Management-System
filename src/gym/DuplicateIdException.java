package gym;

// Custom Exception 1: Thrown when adding a record with an already-existing ID.
public class DuplicateIdException extends Exception {
    public DuplicateIdException(String message) {
        super(message);
    }
}
