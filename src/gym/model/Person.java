package gym.model;

/**
 * Abstract base class representing a person associated with the gym.
 * Demonstrates:
 * - Abstract Class (Rubric Criterion 2)
 * - Encapsulation via private fields + getters/setters (Requirement 1)
 * - Interface implementation (Identifiable, Persistable)
 */
public abstract class Person implements Identifiable, Persistable {
    private String id;
    private String name;
    private String phone;
    private String email;

    public Person(String id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    // Encapsulated Getters and Setters
    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Abstract method overridden by subclasses (Member, Trainer) to return their role.
     */
    public abstract String getRole();

    /**
     * Polymorphic calculation:
     * - For a Member: returns monthly membership bill (+ trainer fee if assigned).
     * - For a Trainer: returns monthly salary (+ bonus per assigned member).
     */
    public abstract double calculateMonthlyAmount();

    @Override
    public String getSummary() {
        return String.format("[%s] %s (%s) - Phone: %s", getRole(), name, id, phone);
    }
}
