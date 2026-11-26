package gym.service;

import gym.exception.DataStorageException;
import gym.model.Member;
import gym.model.MembershipPlan;
import gym.model.Persistable;
import gym.model.Trainer;
import gym.model.WorkoutLog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles text-file persistence and formatted receipt export.
 * Demonstrates:
 * - File I/O using BufferedReader / BufferedWriter / File (Rubric Criterion 3)
 * - Wildcard Generic helper method writeRecords(File, List<? extends Persistable>)
 * - Robust Exception Handling wrapping IOException & parsing errors in DataStorageException
 */
public class FileStorageService {
    private final File dataDir;
    private final File receiptsDir;
    private final File membersFile;
    private final File trainersFile;
    private final File workoutsFile;

    public FileStorageService(String dataDirPath) {
        this.dataDir = new File(dataDirPath);
        this.receiptsDir = new File(dataDir.getParentFile() != null ? dataDir.getParentFile() : new File("."), "receipts");
        this.membersFile = new File(dataDir, "members.txt");
        this.trainersFile = new File(dataDir, "trainers.txt");
        this.workoutsFile = new File(dataDir, "workouts.txt");
    }

    public boolean hasExistingData() {
        return membersFile.exists() || trainersFile.exists() || workoutsFile.exists();
    }

    public void saveAll(List<Member> members, List<Trainer> trainers, List<WorkoutLog> workouts)
            throws DataStorageException {
        if (!dataDir.exists() && !dataDir.mkdirs()) {
            throw new DataStorageException("Unable to create data directory: " + dataDir.getAbsolutePath());
        }
        try {
            writeRecords(trainersFile, trainers);
            writeRecords(membersFile, members);
            writeRecords(workoutsFile, workouts);
        } catch (IOException ex) {
            throw new DataStorageException("Failed to save gym records to disk: " + ex.getMessage(), ex);
        }
    }

    private void writeRecords(File file, List<? extends Persistable> items) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Persistable item : items) {
                writer.write(item.toFileRecord());
                writer.newLine();
            }
        }
    }

    public List<Trainer> loadTrainers() throws DataStorageException {
        List<Trainer> result = new ArrayList<>();
        if (!trainersFile.exists()) {
            return result;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(trainersFile))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    try {
                        result.add(Trainer.fromFileRecord(trimmed));
                    } catch (IllegalArgumentException ex) {
                        throw new DataStorageException(
                                "Corrupted trainer record at line " + lineNumber + ": " + ex.getMessage(), ex);
                    }
                }
            }
        } catch (IOException ex) {
            throw new DataStorageException("Unable to read trainers.txt: " + ex.getMessage(), ex);
        }
        return result;
    }

    public List<Member> loadMembers(Map<String, Trainer> trainerLookup) throws DataStorageException {
        List<Member> result = new ArrayList<>();
        if (!membersFile.exists()) {
            return result;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(membersFile))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    try {
                        result.add(parseMemberRecord(trimmed, trainerLookup));
                    } catch (IllegalArgumentException ex) {
                        throw new DataStorageException(
                                "Corrupted member record at line " + lineNumber + ": " + ex.getMessage(), ex);
                    }
                }
            }
        } catch (IOException ex) {
            throw new DataStorageException("Unable to read members.txt: " + ex.getMessage(), ex);
        }
        return result;
    }

    private Member parseMemberRecord(String line, Map<String, Trainer> trainerLookup) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 7) {
            throw new IllegalArgumentException("Expected at least 7 columns, found " + parts.length);
        }
        String id = parts[0].trim();
        String name = parts[1].trim();
        String phone = parts[2].trim();
        String email = parts[3].trim();
        MembershipPlan plan = MembershipPlan.valueOf(parts[4].trim().toUpperCase());
        double weightKg = Double.parseDouble(parts[5].trim());
        double heightCm = (parts.length >= 8) ? Double.parseDouble(parts[6].trim()) : 170.0;
        String trainerId = (parts.length >= 8) ? parts[7].trim() : parts[6].trim();

        Trainer trainer = null;
        if (!trainerId.isEmpty() && !trainerId.equalsIgnoreCase("NONE") && trainerLookup != null) {
            trainer = trainerLookup.get(trainerId.toUpperCase());
        }
        return new Member(id, name, phone, email, plan, weightKg, heightCm, trainer);
    }

    public List<WorkoutLog> loadWorkouts() throws DataStorageException {
        List<WorkoutLog> result = new ArrayList<>();
        if (!workoutsFile.exists()) {
            return result;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(workoutsFile))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    try {
                        result.add(WorkoutLog.fromFileRecord(trimmed));
                    } catch (IllegalArgumentException ex) {
                        throw new DataStorageException(
                                "Corrupted workout record at line " + lineNumber + ": " + ex.getMessage(), ex);
                    }
                }
            }
        } catch (IOException ex) {
            throw new DataStorageException("Unable to read workouts.txt: " + ex.getMessage(), ex);
        }
        return result;
    }

    /**
     * Exports a formatted billing invoice for a member into receipts/<ID>_receipt.txt.
     */
    public File exportMemberReceipt(Member member) throws DataStorageException {
        if (!receiptsDir.exists() && !receiptsDir.mkdirs()) {
            throw new DataStorageException("Unable to create receipts directory: " + receiptsDir.getAbsolutePath());
        }
        File outFile = new File(receiptsDir, member.getId() + "_receipt.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            writer.write(member.generateReceiptText());
            return outFile;
        } catch (IOException ex) {
            throw new DataStorageException("Could not export receipt for " + member.getId() + ": " + ex.getMessage(), ex);
        }
    }

    public String getDataDirectoryPath() {
        return dataDir.getAbsolutePath();
    }
}
