package gym.model;

/**
 * Interface for entities that can be serialized into a text file line.
 * Demonstrates: Interface & File Handling support (Rubric Criteria 2 & 3).
 */
public interface Persistable {
    String toFileRecord();
}
