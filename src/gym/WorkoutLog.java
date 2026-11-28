package gym;

// Represents a single Strength or Cardio workout logged by a member.
public class WorkoutLog implements Identifiable {
    private String logId;
    private String memberId;
    private String date;
    private String exerciseName;
    private String type; // "Strength" or "Cardio"
    private int sets;
    private int reps;
    private double weightKg;
    private int durationMinutes;
    private int caloriesBurned;

    // Full constructor (used when loading from file)
    public WorkoutLog(String logId, String memberId, String date, String exerciseName,
                      String type, int sets, int reps, double weightKg,
                      int durationMinutes, int caloriesBurned) {
        this.logId = logId;
        this.memberId = memberId;
        this.date = date;
        this.exerciseName = exerciseName;
        this.type = type;
        this.sets = sets;
        this.reps = reps;
        this.weightKg = weightKg;
        this.durationMinutes = durationMinutes;
        this.caloriesBurned = caloriesBurned;
    }

    // Overloaded Constructor 1: Strength workout
    public WorkoutLog(String logId, String memberId, String date, String exerciseName,
                      int sets, int reps, double weightKg) {
        this(logId, memberId, date, exerciseName, "Strength", sets, reps, weightKg,
                sets * 3, (int) Math.round(sets * reps * (weightKg * 0.1 + 1.5)));
    }

    // Overloaded Constructor 2: Cardio workout
    public WorkoutLog(String logId, String memberId, String date, String exerciseName,
                      int durationMinutes, int caloriesBurned) {
        this(logId, memberId, date, exerciseName, "Cardio", 0, 0, 0.0,
                durationMinutes, caloriesBurned);
    }

    @Override
    public String getId() {
        return logId;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getDate() {
        return date;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public String getType() {
        return type;
    }

    public int getSets() {
        return sets;
    }

    public int getReps() {
        return reps;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getCaloriesBurned() {
        return caloriesBurned;
    }

    public double getTotalVolume() {
        return sets * reps * weightKg;
    }

    @Override
    public String getSummary() {
        if ("Strength".equalsIgnoreCase(type)) {
            return date + " | " + exerciseName + " (" + sets + "x" + reps + " @ " + weightKg + " kg)";
        }
        return date + " | " + exerciseName + " (" + durationMinutes + " min, " + caloriesBurned + " kcal)";
    }

    public String toFileLine() {
        return logId + "|" + memberId + "|" + date + "|" + exerciseName + "|" + type + "|"
                + sets + "|" + reps + "|" + weightKg + "|" + durationMinutes + "|" + caloriesBurned;
    }

    public static WorkoutLog fromFileLine(String line) {
        String[] p = line.split("\\|");
        return new WorkoutLog(
                p[0], p[1], p[2], p[3], p[4],
                Integer.parseInt(p[5]),
                Integer.parseInt(p[6]),
                Double.parseDouble(p[7]),
                Integer.parseInt(p[8]),
                Integer.parseInt(p[9])
        );
    }
}
