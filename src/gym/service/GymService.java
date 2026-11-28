package gym.service;

import gym.exception.DataStorageException;
import gym.exception.DuplicateEntityException;
import gym.exception.EntityNotFoundException;
import gym.exception.InvalidInputException;
import gym.model.Member;
import gym.model.MembershipPlan;
import gym.model.Person;
import gym.model.Trainer;
import gym.model.WorkoutLog;
import gym.repository.DataRepository;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central service orchestrating Gym operations, validations, object associations,
 * dashboard analytics, and file persistence.
 */
public class GymService {
    private final DataRepository<Member> memberRepo = new DataRepository<>();
    private final DataRepository<Trainer> trainerRepo = new DataRepository<>();
    private final DataRepository<WorkoutLog> workoutRepo = new DataRepository<>();
    private final FileStorageService storageService;

    public GymService(FileStorageService storageService) {
        this.storageService = storageService;
    }

    // --- Initialization & Persistence ---

    public void initialize() throws DataStorageException {
        if (storageService.hasExistingData()) {
            reloadFromDisk();
        } else {
            seedSampleData();
            saveToDisk();
        }
    }

    public void reloadFromDisk() throws DataStorageException {
        memberRepo.clear();
        trainerRepo.clear();
        workoutRepo.clear();

        Map<String, Trainer> trainerLookup = new HashMap<>();
        for (Trainer trainer : storageService.loadTrainers()) {
            try {
                trainerRepo.add(trainer);
                trainerLookup.put(trainer.getId().toUpperCase(), trainer);
            } catch (DuplicateEntityException ex) {
                throw new DataStorageException("Duplicate trainer found in trainers.txt: " + ex.getMessage(), ex);
            }
        }

        for (Member member : storageService.loadMembers(trainerLookup)) {
            try {
                memberRepo.add(member);
            } catch (DuplicateEntityException ex) {
                throw new DataStorageException("Duplicate member found in members.txt: " + ex.getMessage(), ex);
            }
        }

        for (WorkoutLog log : storageService.loadWorkouts()) {
            try {
                workoutRepo.add(log);
                if (memberRepo.exists(log.getMemberId())) {
                    memberRepo.findById(log.getMemberId()).logWorkout(log);
                }
            } catch (DuplicateEntityException | EntityNotFoundException ex) {
                throw new DataStorageException("Invalid workout link in workouts.txt: " + ex.getMessage(), ex);
            }
        }
        syncAssociations();
    }

    public void saveToDisk() throws DataStorageException {
        storageService.saveAll(memberRepo.getAll(), trainerRepo.getAll(), workoutRepo.getAll());
    }

    public File exportMemberReceipt(String memberId) throws EntityNotFoundException, DataStorageException {
        Member member = memberRepo.findById(memberId);
        return storageService.exportMemberReceipt(member);
    }

    /**
     * Synchronizes bidirectional Member <-> Trainer object associations.
     */
    private void syncAssociations() {
        for (Trainer trainer : trainerRepo.getAll()) {
            trainer.clearAssignedMembers();
        }
        for (Member member : memberRepo.getAll()) {
            Trainer assigned = member.getAssignedTrainer();
            if (assigned != null && trainerRepo.exists(assigned.getId())) {
                assigned.addAssignedMember(member);
            } else {
                member.setAssignedTrainer(null);
            }
        }
    }

    // --- Validation Helpers ---

    private void validateCommonPersonFields(String id, String name, String phone, String email)
            throws InvalidInputException {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidInputException("ID cannot be empty.");
        }
        if (name == null || name.trim().length() < 2) {
            throw new InvalidInputException("Name must contain at least 2 characters.");
        }
        if (phone == null || !phone.trim().matches("[0-9+\\- ]{7,16}")) {
            throw new InvalidInputException("Phone number must be 7-16 digits (e.g., 01712345678).");
        }
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new InvalidInputException("Please enter a valid email address (e.g., student@ewubd.edu).");
        }
    }

    private Trainer resolveTrainer(String trainerId) throws EntityNotFoundException {
        if (trainerId == null || trainerId.trim().isEmpty() || trainerId.equalsIgnoreCase("NONE")) {
            return null;
        }
        return trainerRepo.findById(trainerId);
    }

    // --- Member Operations ---

    public void addMember(String id, String name, String phone, String email,
                          MembershipPlan plan, double weightKg, double heightCm, String trainerId)
            throws InvalidInputException, DuplicateEntityException, EntityNotFoundException {
        validateCommonPersonFields(id, name, phone, email);
        if (weightKg <= 20 || weightKg > 350) {
            throw new InvalidInputException("Weight must be between 20 kg and 350 kg.");
        }
        if (heightCm < 100 || heightCm > 250) {
            throw new InvalidInputException("Height must be between 100 cm and 250 cm.");
        }
        Trainer trainer = resolveTrainer(trainerId);
        Member member = new Member(id.trim().toUpperCase(), name.trim(), phone.trim(), email.trim(),
                plan, weightKg, heightCm, trainer);
        memberRepo.add(member);
        syncAssociations();
    }

    public void updateMember(String id, String name, String phone, String email,
                             MembershipPlan plan, double weightKg, double heightCm, String trainerId)
            throws InvalidInputException, EntityNotFoundException {
        validateCommonPersonFields(id, name, phone, email);
        if (weightKg <= 20 || weightKg > 350) {
            throw new InvalidInputException("Weight must be between 20 kg and 350 kg.");
        }
        if (heightCm < 100 || heightCm > 250) {
            throw new InvalidInputException("Height must be between 100 cm and 250 cm.");
        }
        Member existing = memberRepo.findById(id);
        Trainer trainer = resolveTrainer(trainerId);

        existing.setName(name.trim());
        existing.setPhone(phone.trim());
        existing.setEmail(email.trim());
        existing.setPlan(plan);
        existing.setWeightKg(weightKg);
        existing.setHeightCm(heightCm);
        existing.setAssignedTrainer(trainer);
        memberRepo.update(existing);
        syncAssociations();
    }

    public void deleteMember(String id) throws EntityNotFoundException {
        Member member = memberRepo.findById(id);
        for (WorkoutLog log : workoutRepo.getAll()) {
            if (log.getMemberId().equalsIgnoreCase(member.getId())) {
                workoutRepo.remove(log.getId());
            }
        }
        memberRepo.remove(id);
        syncAssociations();
    }

    public Member findMember(String id) throws EntityNotFoundException {
        return memberRepo.findById(id);
    }

    public List<Member> getAllMembers() {
        return memberRepo.getAll();
    }

    public List<Member> searchMembers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllMembers();
        }
        String q = query.trim().toLowerCase();
        List<Member> matches = new ArrayList<>();
        for (Member m : memberRepo.getAll()) {
            if (m.getId().toLowerCase().contains(q)
                    || m.getName().toLowerCase().contains(q)
                    || m.getPhone().toLowerCase().contains(q)
                    || m.getPlan().getDisplayName().toLowerCase().contains(q)
                    || m.getAssignedTrainerName().toLowerCase().contains(q)) {
                matches.add(m);
            }
        }
        return matches;
    }

    // --- Trainer Operations ---

    public void addTrainer(String id, String name, String phone, String email,
                           String specialty, double baseSalary, int experienceYears)
            throws InvalidInputException, DuplicateEntityException {
        validateCommonPersonFields(id, name, phone, email);
        if (specialty == null || specialty.trim().isEmpty()) {
            throw new InvalidInputException("Trainer specialty cannot be empty.");
        }
        if (baseSalary < 5000) {
            throw new InvalidInputException("Base salary must be at least BDT 5,000.");
        }
        if (experienceYears < 0 || experienceYears > 50) {
            throw new InvalidInputException("Experience years must be between 0 and 50.");
        }
        Trainer trainer = new Trainer(id.trim().toUpperCase(), name.trim(), phone.trim(),
                email.trim(), specialty.trim(), baseSalary, experienceYears);
        trainerRepo.add(trainer);
        syncAssociations();
    }

    public void updateTrainer(String id, String name, String phone, String email,
                              String specialty, double baseSalary, int experienceYears)
            throws InvalidInputException, EntityNotFoundException {
        validateCommonPersonFields(id, name, phone, email);
        if (specialty == null || specialty.trim().isEmpty()) {
            throw new InvalidInputException("Trainer specialty cannot be empty.");
        }
        if (baseSalary < 5000) {
            throw new InvalidInputException("Base salary must be at least BDT 5,000.");
        }
        if (experienceYears < 0 || experienceYears > 50) {
            throw new InvalidInputException("Experience years must be between 0 and 50.");
        }
        Trainer existing = trainerRepo.findById(id);
        existing.setName(name.trim());
        existing.setPhone(phone.trim());
        existing.setEmail(email.trim());
        existing.setSpecialty(specialty.trim());
        existing.setBaseSalary(baseSalary);
        existing.setExperienceYears(experienceYears);
        trainerRepo.update(existing);
        syncAssociations();
    }

    public void deleteTrainer(String id) throws EntityNotFoundException {
        Trainer toRemove = trainerRepo.findById(id);
        for (Member m : memberRepo.getAll()) {
            if (m.getAssignedTrainer() != null && m.getAssignedTrainer().getId().equalsIgnoreCase(toRemove.getId())) {
                m.setAssignedTrainer(null);
            }
        }
        trainerRepo.remove(id);
        syncAssociations();
    }

    public Trainer findTrainer(String id) throws EntityNotFoundException {
        return trainerRepo.findById(id);
    }

    public List<Trainer> getAllTrainers() {
        return trainerRepo.getAll();
    }

    // --- Workout Operations (Demonstrating Method Overloading) ---

    private String generateWorkoutId() {
        int next = workoutRepo.count() + 1;
        String candidate = String.format("W%03d", next);
        while (workoutRepo.exists(candidate)) {
            next++;
            candidate = String.format("W%03d", next);
        }
        return candidate;
    }

    /**
     * Overloaded Service Method 1: Logs a Strength workout.
     */
    public WorkoutLog logWorkout(String memberId, String date, String exerciseName,
                                 int sets, int reps, double weightUsedKg)
            throws EntityNotFoundException, InvalidInputException, DuplicateEntityException {
        Member member = memberRepo.findById(memberId);
        if (exerciseName == null || exerciseName.trim().isEmpty()) {
            throw new InvalidInputException("Exercise name cannot be empty.");
        }
        String safeDate = (date == null || date.trim().isEmpty()) ? LocalDate.now().toString() : date.trim();
        if (sets <= 0 || reps <= 0) {
            throw new InvalidInputException("Sets and reps must be positive numbers.");
        }
        if (weightUsedKg < 0 || weightUsedKg > 600) {
            throw new InvalidInputException("Weight used must be between 0 and 600 kg.");
        }
        String logId = generateWorkoutId();
        WorkoutLog log = member.logWorkout(logId, safeDate, exerciseName.trim(), sets, reps, weightUsedKg);
        workoutRepo.add(log);
        return log;
    }

    /**
     * Overloaded Service Method 2: Logs a Cardio workout.
     */
    public WorkoutLog logWorkout(String memberId, String date, String exerciseName,
                                 int durationMinutes, int caloriesBurned)
            throws EntityNotFoundException, InvalidInputException, DuplicateEntityException {
        Member member = memberRepo.findById(memberId);
        if (exerciseName == null || exerciseName.trim().isEmpty()) {
            throw new InvalidInputException("Exercise name cannot be empty.");
        }
        String safeDate = (date == null || date.trim().isEmpty()) ? LocalDate.now().toString() : date.trim();
        if (durationMinutes <= 0 || durationMinutes > 600) {
            throw new InvalidInputException("Duration must be between 1 and 600 minutes.");
        }
        if (caloriesBurned <= 0 || caloriesBurned > 5000) {
            throw new InvalidInputException("Calories burned must be between 1 and 5000 kcal.");
        }
        String logId = generateWorkoutId();
        WorkoutLog log = member.logWorkout(logId, safeDate, exerciseName.trim(), durationMinutes, caloriesBurned);
        workoutRepo.add(log);
        return log;
    }

    public void deleteWorkout(String logId) throws EntityNotFoundException {
        WorkoutLog log = workoutRepo.findById(logId);
        workoutRepo.remove(logId);
        if (memberRepo.exists(log.getMemberId())) {
            Member member = memberRepo.findById(log.getMemberId());
            member.clearWorkoutLogs();
            for (WorkoutLog remaining : workoutRepo.getAll()) {
                if (remaining.getMemberId().equalsIgnoreCase(member.getId())) {
                    member.logWorkout(remaining);
                }
            }
        }
    }

    public List<WorkoutLog> getAllWorkouts() {
        return workoutRepo.getAll();
    }

    public List<WorkoutLog> getWorkoutsForMember(String memberId) {
        if (memberId == null || memberId.trim().isEmpty() || memberId.equalsIgnoreCase("ALL")) {
            return getAllWorkouts();
        }
        List<WorkoutLog> filtered = new ArrayList<>();
        for (WorkoutLog log : workoutRepo.getAll()) {
            if (log.getMemberId().equalsIgnoreCase(memberId.trim())) {
                filtered.add(log);
            }
        }
        return filtered;
    }

    // --- Polymorphism & Dashboard Analytics ---

    public List<Person> getAllPeoplePolymorphic() {
        List<Person> people = new ArrayList<>();
        people.addAll(trainerRepo.getAll());
        people.addAll(memberRepo.getAll());
        return people;
    }

    public double calculateTotalMonthlyRevenue() {
        double revenue = 0.0;
        for (Member m : memberRepo.getAll()) {
            revenue += m.calculateMonthlyAmount();
        }
        return revenue;
    }

    public double calculateTotalTrainerPayroll() {
        double payroll = 0.0;
        for (Trainer t : trainerRepo.getAll()) {
            payroll += t.calculateMonthlyAmount();
        }
        return payroll;
    }

    public int getPlanCount(MembershipPlan plan) {
        int count = 0;
        for (Member m : memberRepo.getAll()) {
            if (m.getPlan() == plan) {
                count++;
            }
        }
        return count;
    }

    public double getTotalVolumeAcrossGym() {
        double total = 0.0;
        for (WorkoutLog log : workoutRepo.getAll()) {
            total += log.calculateVolumeKg();
        }
        return total;
    }

    public int getTotalCaloriesAcrossGym() {
        int total = 0;
        for (WorkoutLog log : workoutRepo.getAll()) {
            total += log.getCaloriesBurned();
        }
        return total;
    }

    public double getAverageBmi() {
        List<Member> members = memberRepo.getAll();
        if (members.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Member m : members) {
            sum += m.calculateBMI();
        }
        return sum / members.size();
    }

    public String getDataDirectoryPath() {
        return storageService.getDataDirectoryPath();
    }

    // --- Rich Sample Data for Immediate Lab Demo ---

    private void seedSampleData() {
        try {
            addTrainer("T001", "Tanvir Ahmed", "01711223344", "tanvir.coach@gymtracker.bd",
                    "Strength & Hypertrophy", 7000, 5);
            addTrainer("T002", "Nusrat Jahan", "01822334455", "nusrat.fit@gymtracker.bd",
                    "HIIT & Conditioning", 6500, 4);
            addTrainer("T003", "Rafiul Islam", "01933445566", "rafiul.power@gymtracker.bd",
                    "Powerlifting", 7500, 6);

            addMember("M001", "Arif Rahman", "01755667788", "arif@std.ewubd.edu",
                    MembershipPlan.PREMIUM, 74.5, 176.0, "T001");
            addMember("M002", "Sadia Afrin", "01644556677", "sadia@std.ewubd.edu",
                    MembershipPlan.STANDARD, 56.0, 163.0, "T002");
            addMember("M003", "Mehedi Hasan", "01566778899", "mehedi@std.ewubd.edu",
                    MembershipPlan.BASIC, 79.0, 172.0, "NONE");
            addMember("M004", "Farhan Kabir", "01799887766", "farhan@std.ewubd.edu",
                    MembershipPlan.PREMIUM, 84.0, 180.0, "T001");
            addMember("M005", "Nabila Tasnim", "01877665544", "nabila@std.ewubd.edu",
                    MembershipPlan.STANDARD, 52.5, 160.0, "T002");
            addMember("M006", "Zarif Hossain", "01911224466", "zarif@std.ewubd.edu",
                    MembershipPlan.PREMIUM, 91.0, 177.0, "T003");

            logWorkout("M001", "2026-11-24", "Barbell Bench Press", 4, 8, 72.5);
            logWorkout("M001", "2026-11-25", "Back Squat", 4, 6, 100.0);
            logWorkout("M002", "2026-11-25", "Treadmill HIIT Sprints", 30, 340);
            logWorkout("M003", "2026-11-25", "Lat Pulldown", 3, 12, 47.5);
            logWorkout("M004", "2026-11-26", "Conventional Deadlift", 3, 5, 130.0);
            logWorkout("M005", "2026-11-26", "Rowing Machine", 25, 275);
            logWorkout("M006", "2026-11-26", "Overhead Barbell Press", 4, 8, 55.0);
            logWorkout("M004", "2026-11-26", "Cycling Endurance", 40, 410);
        } catch (InvalidInputException | DuplicateEntityException | EntityNotFoundException ex) {
            throw new IllegalStateException("Failed to seed initial demo records: " + ex.getMessage(), ex);
        }
    }
}
