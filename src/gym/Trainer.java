package gym;

import java.util.ArrayList;

// Subclass of Person representing a gym trainer.
// Demonstrates: Inheritance, Method Overriding, and Association with Members.
public class Trainer extends Person {
    private String specialty;
    private double baseSalary;
    private int experienceYears;
    private ArrayList<Member> assignedMembers; // Association

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

    public ArrayList<Member> getAssignedMembers() {
        return assignedMembers;
    }

    public void addAssignedMember(Member member) {
        if (member != null && !assignedMembers.contains(member)) {
            assignedMembers.add(member);
        }
    }

    public void clearAssignedMembers() {
        assignedMembers.clear();
    }

    @Override
    public String getRole() {
        return "Trainer (" + specialty + ")";
    }

    @Override
    public double calculateMonthlyAmount() {
        // Base salary + BDT 750 commission per assigned member
        return baseSalary + (assignedMembers.size() * 750.0);
    }

    @Override
    public String getSummary() {
        return super.getSummary() + String.format(" | Exp: %d yrs | Trainees: %d | Salary: BDT %.0f",
                experienceYears, assignedMembers.size(), calculateMonthlyAmount());
    }

    public String toFileLine() {
        return getId() + "|" + getName() + "|" + getPhone() + "|" + getEmail() + "|"
                + specialty + "|" + baseSalary + "|" + experienceYears;
    }

    public static Trainer fromFileLine(String line) {
        String[] p = line.split("\\|");
        return new Trainer(
                p[0], p[1], p[2], p[3], p[4],
                Double.parseDouble(p[5]),
                Integer.parseInt(p[6])
        );
    }
}
