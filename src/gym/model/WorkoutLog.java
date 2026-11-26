package gym.model;

/**
 * Represents an individual workout entry logged by a gym member.
 * Demonstrates:
 * - Core Class with Encapsulation (Requirement 1)
 * - Method / Constructor Overloading for Strength vs Cardio workouts (Requirement 2)
 */
public class WorkoutLog implements Identifiable, Persistable {
    private String logId;
    private String memberId;
    private String date;
    private String exerciseName;
    private String category; // "Strength" or "Cardio"
    private int sets;
    private int reps;
    private double weightUsedKg;
    private int durationMinutes;
    private int caloriesBurned;

    // Full constructor (used when loading from file)
    public WorkoutLog(String logId, String memberId, String date, String exerciseName,
                      String category, int sets, int reps, double weightUsedKg,
                      int durationMinutes, int caloriesBurned) {
        this.logId = logId;
        this.memberId = memberId;
        this.date = date;
        this.exerciseName = exerciseName;
        this.category = category;
        this.sets = sets;
        this.reps = reps;
        this.weightUsedKg = weightUsedKg;
        this.durationMinutes = durationMinutes;
        this.caloriesBurned = caloriesBurned;
    }

    // Overloaded Constructor 1: Strength Workout
    public WorkoutLog(String logId, String memberId, String date, String exerciseName,
                      int sets, int reps, double weightUsedKg) {
        this(logId, memberId, date, exerciseName, "Strength", sets, reps, weightUsedKg,
                sets * 3, (int) Math.round(sets * reps * (weightUsedKg * 0.12 + 1.5)));
    }

    // Overloaded Constructor 2: Cardio Workout
    public WorkoutLog(String logId, String memberId, String date, String exerciseName,
                      int durationMinutes, int caloriesBurned) {
        this(logId, memberId, date, exerciseName, "Cardio", 0, 0, 0.0,
                durationMinutes, caloriesBurned);
    }

    @Override
    public String getId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public double getWeightUsedKg() {
        return weightUsedKg;
    }

    public void setWeightUsedKg(double weightUsedKg) {
        this.weightUsedKg = weightUsedKg;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(int caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    /**
     * Calculates total lifted volume (sets * reps * weight) for strength exercises.
     */
    public double calculateVolumeKg() {
        return sets * reps * weightUsedKg;
    }

    @Override
    public String getSummary() {
        if ("Strength".equalsIgnoreCase(category)) {
            return String.format("%s | %s (%d x %d @ %.1f kg)", date, exerciseName, sets, reps, weightUsedKg);
        }
        return String.format("%s | %s (%d min, %d kcal)", date, exerciseName, durationMinutes, caloriesBurned);
    }

    @Override
    public String toFileRecord() {
        return String.join("|",
                logId,
                memberId,
                date,
                exerciseName.replace("|", "/"),
                category,
                String.valueOf(sets),
                String.valueOf(reps),
                String.valueOf(weightUsedKg),
                String.valueOf(durationMinutes),
                String.valueOf(caloriesBurned));
    }

    public static WorkoutLog fromFileRecord(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 10) {
            throw new IllegalArgumentException("Malformed workout record: " + line);
        }
        return new WorkoutLog(
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim(),
                parts[3].trim(),
                parts[4].trim(),
                Integer.parseInt(parts[5].trim()),
                Integer.parseInt(parts[6].trim()),
                Double.parseDouble(parts[7].trim()),
                Integer.parseInt(parts[8].trim()),
                Integer.parseInt(parts[9].trim())
        );
    }
}
