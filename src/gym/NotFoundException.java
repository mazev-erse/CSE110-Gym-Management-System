package gym;

// Custom Exception 2: Thrown when searching or deleting a record that does not exist.
public class NotFoundException extends Exception {
    public NotFoundException(String message) {
        super(message);
    }
}
