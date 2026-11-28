package gym;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

// Handles saving and loading text files using BufferedReader and BufferedWriter.
public class FileHandler {
    private File dataFolder;
    private File membersFile;
    private File trainersFile;
    private File workoutsFile;

    public FileHandler(String folderName) {
        this.dataFolder = new File(folderName);
        this.trainersFile = new File(dataFolder, "trainers.txt");
        this.membersFile = new File(dataFolder, "members.txt");
        this.workoutsFile = new File(dataFolder, "workouts.txt");
    }

    public boolean dataFilesExist() {
        return trainersFile.exists() || membersFile.exists() || workoutsFile.exists();
    }

    public void saveAll(DataManager<Trainer> trainers,
                        DataManager<Member> members,
                        DataManager<WorkoutLog> workouts) throws IOException {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(trainersFile))) {
            for (Trainer t : trainers.getAll()) {
                bw.write(t.toFileLine());
                bw.newLine();
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(membersFile))) {
            for (Member m : members.getAll()) {
                bw.write(m.toFileLine());
                bw.newLine();
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(workoutsFile))) {
            for (WorkoutLog w : workouts.getAll()) {
                bw.write(w.toFileLine());
                bw.newLine();
            }
        }
    }

    public void loadAll(DataManager<Trainer> trainers,
                        DataManager<Member> members,
                        DataManager<WorkoutLog> workouts) throws IOException {
        trainers.clear();
        members.clear();
        workouts.clear();

        HashMap<String, Trainer> trainerMap = new HashMap<>();

        if (trainersFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(trainersFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        Trainer t = Trainer.fromFileLine(line.trim());
                        try {
                            trainers.add(t);
                            trainerMap.put(t.getId().toUpperCase(), t);
                        } catch (DuplicateIdException e) {
                            System.err.println("Skipped duplicate trainer: " + e.getMessage());
                        }
                    }
                }
            }
        }

        if (membersFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(membersFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        String[] p = line.trim().split("\\|");
                        String trainerId = p[7];
                        Trainer assigned = "NONE".equalsIgnoreCase(trainerId)
                                ? null : trainerMap.get(trainerId.toUpperCase());
                        Member m = new Member(
                                p[0], p[1], p[2], p[3], p[4],
                                Double.parseDouble(p[5]),
                                Double.parseDouble(p[6]),
                                assigned
                        );
                        try {
                            members.add(m);
                            if (assigned != null) {
                                assigned.addAssignedMember(m);
                            }
                        } catch (DuplicateIdException e) {
                            System.err.println("Skipped duplicate member: " + e.getMessage());
                        }
                    }
                }
            }
        }

        if (workoutsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(workoutsFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        WorkoutLog log = WorkoutLog.fromFileLine(line.trim());
                        try {
                            workouts.add(log);
                            if (members.exists(log.getMemberId())) {
                                members.findById(log.getMemberId()).addWorkout(log);
                            }
                        } catch (DuplicateIdException | NotFoundException e) {
                            System.err.println("Skipped workout record: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
