package gym.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a certified gym trainer.
 * Demonstrates:
 * - Inheritance: extends abstract class Person
 * - Polymorphism: overrides getRole(), calculateMonthlyAmount(), getSummary()
 * - Aggregation / Association: maintains List<Member> of assigned trainees
 */
public class Trainer extends Person {
    public static final double COMMISSION_PER_TRAINEE = 750.0;

    private String specialty;
    private double baseSalary;
    private int experienceYears;
    private final List<Member> assignedMembers; // True OOP Object Association

    public Trainer(String id, String name, String phone, String email,
                   String specialty, double baseSalary, int experienceYears) {
        super(id, name, phone, email);
        this.specialty = specialty;
        this.baseSalary = baseSalary;
        this.experienceYears = experienceYears;
        this.assignedMembers = new ArrayList<>();
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(double baseSalary) {
        this.baseSalary = baseSalary;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public void addAssignedMember(Member member) {
        if (member != null && !assignedMembers.contains(member)) {
            assignedMembers.add(member);
        }
    }

    public void removeAssignedMember(Member member) {
        assignedMembers.remove(member);
    }

    public void clearAssignedMembers() {
        assignedMembers.clear();
    }

    public List<Member> getAssignedMembers() {
        return new ArrayList<>(assignedMembers);
    }

    public int getAssignedMemberCount() {
        return assignedMembers.size();
    }

    @Override
    public String getRole() {
        return "Trainer (" + specialty + ")";
    }

    @Override
    public double calculateMonthlyAmount() {
        return baseSalary + (assignedMembers.size() * COMMISSION_PER_TRAINEE);
    }

    @Override
    public String getSummary() {
        return String.format("%s | Exp: %d yrs | Trainees: %d | Monthly Payout: BDT %.0f",
                super.getSummary(), experienceYears, assignedMembers.size(), calculateMonthlyAmount());
    }

    @Override
    public String toFileRecord() {
        return String.join("|",
                getId(),
                getName().replace("|", "/"),
                getPhone().replace("|", "/"),
                getEmail().replace("|", "/"),
                specialty.replace("|", "/"),
                String.valueOf(baseSalary),
                String.valueOf(experienceYears));
    }

    public static Trainer fromFileRecord(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 7) {
            throw new IllegalArgumentException("Malformed trainer record: " + line);
        }
        return new Trainer(
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim(),
                parts[3].trim(),
                parts[4].trim(),
                Double.parseDouble(parts[5].trim()),
                Integer.parseInt(parts[6].trim())
        );
    }

    @Override
    public String toString() {
        return getId() + " - " + getName() + " (" + specialty + ")";
    }
}
