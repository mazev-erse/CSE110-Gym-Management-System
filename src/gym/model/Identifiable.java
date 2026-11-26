package gym.model;

/**
 * Interface representing any domain entity that has a unique ID and summary.
 * Demonstrates: Interface (Rubric Criterion 2).
 */
public interface Identifiable {
    String getId();
    String getSummary();
}
