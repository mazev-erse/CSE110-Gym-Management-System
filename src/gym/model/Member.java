package gym.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a registered gym member.
 * Demonstrates:
 * - Inheritance: extends abstract class Person
 * - Polymorphism: overrides getRole(), calculateMonthlyAmount(), getSummary()
 * - Method Overloading: 3 overloaded logWorkout(...) methods
 * - Composition: owns List<WorkoutLog>
 * - Aggregation / Association: holds an object reference to Trainer and MembershipPlan
 */
public class Member extends Person {
    public static final double PERSONAL_TRAINER_FEE = 1500.0;

    private MembershipPlan plan;
    private double weightKg;
    private double heightCm;
    private Trainer assignedTrainer; // True OOP Object Association (Aggregation)
    private final List<WorkoutLog> workoutLogs; // True OOP Composition

    public Member(String id, String name, String phone, String email,
                  MembershipPlan plan, double weightKg, double heightCm, Trainer assignedTrainer) {
        super(id, name, phone, email);
        this.plan = plan;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.assignedTrainer = assignedTrainer;
        this.workoutLogs = new ArrayList<>();
    }

    public MembershipPlan getPlan() {
        return plan;
    }

    public void setPlan(MembershipPlan plan) {
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

    public boolean hasPersonalTrainer() {
        return assignedTrainer != null;
    }

    public String getAssignedTrainerId() {
        return assignedTrainer != null ? assignedTrainer.getId() : "NONE";
    }

    public String getAssignedTrainerName() {
        return assignedTrainer != null ? assignedTrainer.getName() : "Unassigned";
    }

    public List<WorkoutLog> getWorkoutLogs() {
        return new ArrayList<>(workoutLogs);
    }

    public void clearWorkoutLogs() {
        workoutLogs.clear();
    }

    // --- Health Analytics (Encapsulated Business Logic) ---

    public double calculateBMI() {
        if (heightCm <= 0) {
            return 0.0;
        }
        double heightMeters = heightCm / 100.0;
        return weightKg / (heightMeters * heightMeters);
    }

    public String getBmiCategory() {
        double bmi = calculateBMI();
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    // --- Method Overloading (Compile-Time Polymorphism) ---

    /**
     * Overloaded Method 1: Adds an existing WorkoutLog instance.
     */
    public void logWorkout(WorkoutLog log) {
        if (log != null) {
            workoutLogs.add(log);
        }
    }

    /**
     * Overloaded Method 2: Creates and logs a Strength workout directly.
     */
    public WorkoutLog logWorkout(String logId, String date, String exerciseName,
                                 int sets, int reps, double weightUsedKg) {
        WorkoutLog log = new WorkoutLog(logId, getId(), date, exerciseName, sets, reps, weightUsedKg);
        workoutLogs.add(log);
        return log;
    }

    /**
     * Overloaded Method 3: Creates and logs a Cardio workout directly.
     */
    public WorkoutLog logWorkout(String logId, String date, String exerciseName,
                                 int durationMinutes, int caloriesBurned) {
        WorkoutLog log = new WorkoutLog(logId, getId(), date, exerciseName, durationMinutes, caloriesBurned);
        workoutLogs.add(log);
        return log;
    }

    public int getTotalCaloriesBurned() {
        int total = 0;
        for (WorkoutLog log : workoutLogs) {
            total += log.getCaloriesBurned();
        }
        return total;
    }

    public double getTotalVolumeKg() {
        double total = 0.0;
        for (WorkoutLog log : workoutLogs) {
            total += log.calculateVolumeKg();
        }
        return total;
    }

    public String generateReceiptText() {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("      GYM TRACKER — WORKOUT & CLUB MANAGEMENT       \n");
        sb.append("             OFFICIAL MONTHLY BILL RECEIPT          \n");
        sb.append("====================================================\n");
        sb.append(String.format("Issue Date       : %s%n", LocalDate.now()));
        sb.append(String.format("Member ID        : %s%n", getId()));
        sb.append(String.format("Member Name      : %s%n", getName()));
        sb.append(String.format("Phone / Email    : %s | %s%n", getPhone(), getEmail()));
        sb.append(String.format("Health Profile   : %.1f kg | %.0f cm | BMI %.1f (%s)%n",
                weightKg, heightCm, calculateBMI(), getBmiCategory()));
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("Membership Plan  : %-18s BDT %8.2f%n",
                plan.getDisplayName(), plan.getMonthlyFee()));
        if (hasPersonalTrainer()) {
            sb.append(String.format("Personal Trainer : %-18s BDT %8.2f%n",
                    assignedTrainer.getName(), PERSONAL_TRAINER_FEE));
        } else {
            sb.append(String.format("Personal Trainer : %-18s BDT %8.2f%n", "None", 0.0));
        }
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("TOTAL MONTHLY DUE:                    BDT %8.2f%n", calculateMonthlyAmount()));
        sb.append("====================================================\n");
        sb.append(String.format("Workouts Logged: %d | Total Volume: %.0f kg | Burned: %d kcal%n",
                workoutLogs.size(), getTotalVolumeKg(), getTotalCaloriesBurned()));
        sb.append("Thank you for training with Gym Tracker!\n");
        return sb.toString();
    }

    // --- Method Overriding (Runtime Polymorphism) ---

    @Override
    public String getRole() {
        return "Member (" + plan.getDisplayName() + ")";
    }

    @Override
    public double calculateMonthlyAmount() {
        double total = plan.getMonthlyFee();
        if (hasPersonalTrainer()) {
            total += PERSONAL_TRAINER_FEE;
        }
        return total;
    }

    @Override
    public String getSummary() {
        return String.format("%s | BMI: %.1f (%s) | Trainer: %s | Bill: BDT %.0f",
                super.getSummary(), calculateBMI(), getBmiCategory(),
                getAssignedTrainerName(), calculateMonthlyAmount());
    }

    @Override
    public String toFileRecord() {
        return String.join("|",
                getId(),
                getName().replace("|", "/"),
                getPhone().replace("|", "/"),
                getEmail().replace("|", "/"),
                plan.name(),
                String.valueOf(weightKg),
                String.valueOf(heightCm),
                getAssignedTrainerId());
    }
}
