package gym;

import java.util.ArrayList;

// Subclass of Person representing a gym member.
// Demonstrates: Inheritance, Polymorphism (Overriding + Overloading), Association & Composition.
public class Member extends Person {
    private String plan; // "Basic", "Standard", or "Premium"
    private double weightKg;
    private double heightCm;
    private Trainer assignedTrainer; // Association (Aggregation)
    private ArrayList<WorkoutLog> workouts; // Composition

    public Member(String id, String name, String phone, String email,
                  String plan, double weightKg, double heightCm, Trainer assignedTrainer) {
        super(id, name, phone, email);
        this.plan = plan;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.assignedTrainer = assignedTrainer;
        this.workouts = new ArrayList<>();
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(double heightCm) {
        this.heightCm = heightCm;
    }

    public Trainer getAssignedTrainer() {
        return assignedTrainer;
    }

    public void setAssignedTrainer(Trainer assignedTrainer) {
        this.assignedTrainer = assignedTrainer;
    }

    public String getAssignedTrainerId() {
        return assignedTrainer != null ? assignedTrainer.getId() : "NONE";
    }

    public String getAssignedTrainerName() {
        return assignedTrainer != null ? assignedTrainer.getName() : "None";
    }

    public ArrayList<WorkoutLog> getWorkouts() {
        return workouts;
    }

    public double calculateBMI() {
        double hMeters = heightCm / 100.0;
        return weightKg / (hMeters * hMeters);
    }

    public String getBmiStatus() {
        double bmi = calculateBMI();
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    // --- Method Overloading (Compile-Time Polymorphism) ---

    public void addWorkout(WorkoutLog log) {
        workouts.add(log);
    }

    // Overloaded method for Strength workouts
    public WorkoutLog addWorkout(String logId, String date, String exercise,
                                 int sets, int reps, double weightKg) {
        WorkoutLog log = new WorkoutLog(logId, getId(), date, exercise, sets, reps, weightKg);
        workouts.add(log);
        return log;
    }

    // Overloaded method for Cardio workouts
    public WorkoutLog addWorkout(String logId, String date, String exercise,
                                 int durationMinutes, int caloriesBurned) {
        WorkoutLog log = new WorkoutLog(logId, getId(), date, exercise, durationMinutes, caloriesBurned);
        workouts.add(log);
        return log;
    }

    // --- Method Overriding (Runtime Polymorphism) ---

    @Override
    public String getRole() {
        return "Member (" + plan + ")";
    }

    @Override
    public double calculateMonthlyAmount() {
        double fee = 1500.0; // Basic
        if ("Standard".equalsIgnoreCase(plan)) {
            fee = 2500.0;
        } else if ("Premium".equalsIgnoreCase(plan)) {
            fee = 4000.0;
        }
        if (assignedTrainer != null) {
            fee += 1500.0; // Personal trainer addon
        }
        return fee;
    }

    @Override
    public String getSummary() {
        return super.getSummary() + String.format(" | BMI: %.1f (%s) | Monthly Fee: BDT %.0f",
                calculateBMI(), getBmiStatus(), calculateMonthlyAmount());
    }

    public String toFileLine() {
        return getId() + "|" + getName() + "|" + getPhone() + "|" + getEmail() + "|"
                + plan + "|" + weightKg + "|" + heightCm + "|" + getAssignedTrainerId();
    }
}
