package gym;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

// Standard Java Swing GUI for Gym Tracker (4 tabs: Members, Trainers, Workouts, Summary).
public class GymGUI extends JFrame {
    private DataManager<Member> memberManager = new DataManager<>();
    private DataManager<Trainer> trainerManager = new DataManager<>();
    private DataManager<WorkoutLog> workoutManager = new DataManager<>();
    private FileHandler fileHandler = new FileHandler("data");

    private JTabbedPane tabbedPane;

    // Members Tab
    private DefaultTableModel memberTableModel;
    private JTable memberTable;
    private JTextField searchField, mIdField, mNameField, mPhoneField, mEmailField, mWeightField, mHeightField;
    private JComboBox<String> mPlanBox, mTrainerBox;

    // Trainers Tab
    private DefaultTableModel trainerTableModel;
    private JTable trainerTable;
    private JTextField tIdField, tNameField, tPhoneField, tEmailField, tSpecialtyField, tSalaryField, tExpField;

    // Workouts Tab
    private DefaultTableModel workoutTableModel;
    private JTable workoutTable;
    private JComboBox<String> wMemberBox, wFilterBox, wTypeBox;
    private JTextField wDateField, wExerciseField, wSetsField, wRepsField, wWeightField, wDurationField, wCaloriesField;

    // Summary Tab (Polymorphism Demo)
    private DefaultTableModel summaryTableModel;
    private JLabel summaryStatsLabel;

    public GymGUI() {
        setTitle("Gym Tracker - CSE110 OOP Project");
        setSize(1020, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        loadInitialData();

        add(createHeaderPanel(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabbedPane.addTab("Members", createMembersTab());
        tabbedPane.addTab("Trainers", createTrainersTab());
        tabbedPane.addTab("Workouts", createWorkoutsTab());
        tabbedPane.addTab("Summary (Polymorphism)", createSummaryTab());
        add(tabbedPane, BorderLayout.CENTER);

        refreshAllTables();
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    // ==================== DATA LOADING & SAMPLE DATA ====================

    private void loadInitialData() {
        try {
            if (fileHandler.dataFilesExist()) {
                fileHandler.loadAll(trainerManager, memberManager, workoutManager);
            } else {
                loadSampleData();
                fileHandler.saveAll(trainerManager, memberManager, workoutManager);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "File error: " + ex.getMessage(),
                    "File I/O Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSampleData() {
        try {
            Trainer t1 = new Trainer("T001", "Tanvir Ahmed", "01711223344", "tanvir@gym.com", "Strength", 7000, 5);
            Trainer t2 = new Trainer("T002", "Nusrat Jahan", "01822334455", "nusrat@gym.com", "Cardio & HIIT", 6500, 4);
            Trainer t3 = new Trainer("T003", "Rafiul Islam", "01933445566", "rafiul@gym.com", "Powerlifting", 7500, 6);
            trainerManager.add(t1);
            trainerManager.add(t2);
            trainerManager.add(t3);

            Member m1 = new Member("M001", "Arif Rahman", "01755667788", "arif@ewubd.edu", "Premium", 74.5, 176, t1);
            Member m2 = new Member("M002", "Sadia Afrin", "01644556677", "sadia@ewubd.edu", "Standard", 56.0, 163, t2);
            Member m3 = new Member("M003", "Mehedi Hasan", "01566778899", "mehedi@ewubd.edu", "Basic", 79.0, 172, null);
            Member m4 = new Member("M004", "Farhan Kabir", "01799887766", "farhan@ewubd.edu", "Premium", 84.0, 180, t1);
            memberManager.add(m1);
            memberManager.add(m2);
            memberManager.add(m3);
            memberManager.add(m4);
            syncTrainerMembers();

            workoutManager.add(m1.addWorkout("W001", "2026-11-25", "Bench Press", 4, 8, 70.0));
            workoutManager.add(m1.addWorkout("W002", "2026-11-26", "Back Squat", 4, 6, 95.0));
            workoutManager.add(m2.addWorkout("W003", "2026-11-26", "Treadmill Run", 30, 320));
            workoutManager.add(m3.addWorkout("W004", "2026-11-27", "Lat Pulldown", 3, 12, 45.0));
            workoutManager.add(m4.addWorkout("W005", "2026-11-27", "Deadlift", 3, 5, 120.0));
        } catch (DuplicateIdException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void syncTrainerMembers() {
        for (Trainer t : trainerManager.getAll()) {
            t.clearAssignedMembers();
        }
        for (Member m : memberManager.getAll()) {
            if (m.getAssignedTrainer() != null && trainerManager.exists(m.getAssignedTrainer().getId())) {
                m.getAssignedTrainer().addAssignedMember(m);
            } else {
                m.setAssignedTrainer(null);
            }
        }
    }

    private void saveDataQuietly() {
        try {
            fileHandler.saveAll(trainerManager, memberManager, workoutManager);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save files: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== HEADER ====================

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 41, 59));
        header.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("Gym Tracker — Workout & Club Management System");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);

        JButton saveBtn = new JButton("Save Files");
        JButton reloadBtn = new JButton("Reload Files");

        saveBtn.addActionListener(e -> {
            saveDataQuietly();
            JOptionPane.showMessageDialog(this, "All records saved to data/ folder.");
        });

        reloadBtn.addActionListener(e -> {
            try {
                fileHandler.loadAll(trainerManager, memberManager, workoutManager);
                syncTrainerMembers();
                refreshAllTables();
                JOptionPane.showMessageDialog(this, "Data reloaded from files.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error reading files: " + ex.getMessage(),
                        "File Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttons.add(reloadBtn);
        buttons.add(saveBtn);

        header.add(title, BorderLayout.WEST);
        header.add(buttons, BorderLayout.EAST);
        return header;
    }

    // ==================== TAB 1: MEMBERS ====================

    private JPanel createMembersTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        JButton showAllBtn = new JButton("Show All");

        searchBtn.addActionListener(e -> filterMembers(searchField.getText()));
        showAllBtn.addActionListener(e -> {
            searchField.setText("");
            refreshMemberTable(memberManager.getAll());
        });

        searchPanel.add(new JLabel("Search (ID or Name):"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(showAllBtn);

        // Table
        String[] cols = {"ID", "Name", "Phone", "Plan", "BMI", "Trainer", "Monthly Fee"};
        memberTableModel = new DefaultTableModel(cols, 0);
        memberTable = new JTable(memberTableModel);
        memberTable.setRowHeight(24);
        memberTable.getSelectionModel().addListSelectionListener(e -> fillMemberFormFromTable());

        JPanel leftPanel = new JPanel(new BorderLayout(0, 8));
        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(memberTable), BorderLayout.CENTER);

        // Right Form
        JPanel form = new JPanel(new GridLayout(10, 2, 6, 8));
        form.setPreferredSize(new Dimension(310, 0));
        form.setBorder(BorderFactory.createTitledBorder("Member Details"));

        mIdField = new JTextField("M005");
        mNameField = new JTextField();
        mPhoneField = new JTextField();
        mEmailField = new JTextField();
        mPlanBox = new JComboBox<>(new String[]{"Basic", "Standard", "Premium"});
        mWeightField = new JTextField("70");
        mHeightField = new JTextField("170");
        mTrainerBox = new JComboBox<>();

        form.add(new JLabel("Member ID:"));      form.add(mIdField);
        form.add(new JLabel("Full Name:"));      form.add(mNameField);
        form.add(new JLabel("Phone:"));          form.add(mPhoneField);
        form.add(new JLabel("Email:"));          form.add(mEmailField);
        form.add(new JLabel("Plan:"));           form.add(mPlanBox);
        form.add(new JLabel("Weight (kg):"));    form.add(mWeightField);
        form.add(new JLabel("Height (cm):"));    form.add(mHeightField);
        form.add(new JLabel("Trainer:"));        form.add(mTrainerBox);

        JButton addBtn = new JButton("Add Member");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");

        addBtn.addActionListener(e -> addMemberAction());
        updateBtn.addActionListener(e -> updateMemberAction());
        deleteBtn.addActionListener(e -> deleteMemberAction());
        clearBtn.addActionListener(e -> clearMemberForm());

        form.add(addBtn);    form.add(updateBtn);
        form.add(deleteBtn); form.add(clearBtn);

        panel.add(leftPanel, BorderLayout.CENTER);
        panel.add(form, BorderLayout.EAST);
        return panel;
    }

    private void addMemberAction() {
        try {
            validatePersonFields(mIdField.getText(), mNameField.getText(), mPhoneField.getText(), mEmailField.getText());
            double weight = Double.parseDouble(mWeightField.getText().trim());
            double height = Double.parseDouble(mHeightField.getText().trim());
            if (weight <= 20 || height < 100) {
                throw new InvalidDataException("Please enter valid weight (>20 kg) and height (>=100 cm).");
            }
            Trainer trainer = getSelectedTrainerObject();
            Member m = new Member(mIdField.getText().trim().toUpperCase(), mNameField.getText().trim(),
                    mPhoneField.getText().trim(), mEmailField.getText().trim(),
                    (String) mPlanBox.getSelectedItem(), weight, height, trainer);

            memberManager.add(m);
            syncTrainerMembers();
            saveDataQuietly();
            refreshAllTables();
            clearMemberForm();
        } catch (NumberFormatException ex) {
            showError("Weight and Height must be valid numbers!");
        } catch (InvalidDataException | DuplicateIdException | NotFoundException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateMemberAction() {
        try {
            validatePersonFields(mIdField.getText(), mNameField.getText(), mPhoneField.getText(), mEmailField.getText());
            Member m = memberManager.findById(mIdField.getText());
            double weight = Double.parseDouble(mWeightField.getText().trim());
            double height = Double.parseDouble(mHeightField.getText().trim());
            if (weight <= 20 || height < 100) {
                throw new InvalidDataException("Please enter valid weight (>20 kg) and height (>=100 cm).");
            }
            m.setName(mNameField.getText().trim());
            m.setPhone(mPhoneField.getText().trim());
            m.setEmail(mEmailField.getText().trim());
            m.setPlan((String) mPlanBox.getSelectedItem());
            m.setWeightKg(weight);
            m.setHeightCm(height);
            m.setAssignedTrainer(getSelectedTrainerObject());

            syncTrainerMembers();
            saveDataQuietly();
            refreshAllTables();
            JOptionPane.showMessageDialog(this, "Member updated successfully.");
        } catch (NumberFormatException ex) {
            showError("Weight and Height must be valid numbers!");
        } catch (InvalidDataException | NotFoundException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteMemberAction() {
        try {
            String id = mIdField.getText().trim();
            memberManager.remove(id);
            syncTrainerMembers();
            saveDataQuietly();
            refreshAllTables();
            clearMemberForm();
        } catch (NotFoundException ex) {
            showError(ex.getMessage());
        }
    }

    private void fillMemberFormFromTable() {
        int row = memberTable.getSelectedRow();
        if (row < 0) return;
        String id = memberTableModel.getValueAt(row, 0).toString();
        try {
            Member m = memberManager.findById(id);
            mIdField.setText(m.getId());
            mNameField.setText(m.getName());
            mPhoneField.setText(m.getPhone());
            mEmailField.setText(m.getEmail());
            mPlanBox.setSelectedItem(m.getPlan());
            mWeightField.setText(String.valueOf(m.getWeightKg()));
            mHeightField.setText(String.valueOf(m.getHeightCm()));
            selectComboByPrefix(mTrainerBox, m.getAssignedTrainerId());
        } catch (NotFoundException ignored) {
        }
    }

    private void clearMemberForm() {
        memberTable.clearSelection();
        mIdField.setText(String.format("M%03d", memberManager.size() + 1));
        mNameField.setText("");
        mPhoneField.setText("");
        mEmailField.setText("");
        mWeightField.setText("70");
        mHeightField.setText("170");
    }

    private void filterMembers(String query) {
        String q = query.trim().toLowerCase();
        ArrayList<Member> results = new ArrayList<>();
        for (Member m : memberManager.getAll()) {
            if (m.getId().toLowerCase().contains(q) || m.getName().toLowerCase().contains(q)) {
                results.add(m);
            }
        }
        refreshMemberTable(results);
    }

    // ==================== TAB 2: TRAINERS ====================

    private JPanel createTrainersTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Name", "Specialty", "Exp (Yrs)", "Base Salary", "Trainees", "Total Salary"};
        trainerTableModel = new DefaultTableModel(cols, 0);
        trainerTable = new JTable(trainerTableModel);
        trainerTable.setRowHeight(24);
        trainerTable.getSelectionModel().addListSelectionListener(e -> fillTrainerFormFromTable());

        JPanel form = new JPanel(new GridLayout(9, 2, 6, 8));
        form.setPreferredSize(new Dimension(310, 0));
        form.setBorder(BorderFactory.createTitledBorder("Trainer Details"));

        tIdField = new JTextField("T004");
        tNameField = new JTextField();
        tPhoneField = new JTextField();
        tEmailField = new JTextField();
        tSpecialtyField = new JTextField();
        tSalaryField = new JTextField("7000");
        tExpField = new JTextField("2");

        form.add(new JLabel("Trainer ID:"));      form.add(tIdField);
        form.add(new JLabel("Full Name:"));       form.add(tNameField);
        form.add(new JLabel("Phone:"));           form.add(tPhoneField);
        form.add(new JLabel("Email:"));           form.add(tEmailField);
        form.add(new JLabel("Specialty:"));       form.add(tSpecialtyField);
        form.add(new JLabel("Base Salary:"));     form.add(tSalaryField);
        form.add(new JLabel("Exp (Years):"));     form.add(tExpField);

        JButton addBtn = new JButton("Add Trainer");
        JButton deleteBtn = new JButton("Delete Trainer");
        JButton clearBtn = new JButton("Clear");

        addBtn.addActionListener(e -> addTrainerAction());
        deleteBtn.addActionListener(e -> deleteTrainerAction());
        clearBtn.addActionListener(e -> clearTrainerForm());

        form.add(addBtn);    form.add(deleteBtn);
        form.add(clearBtn);  form.add(new JLabel(""));

        panel.add(new JScrollPane(trainerTable), BorderLayout.CENTER);
        panel.add(form, BorderLayout.EAST);
        return panel;
    }

    private void addTrainerAction() {
        try {
            validatePersonFields(tIdField.getText(), tNameField.getText(), tPhoneField.getText(), tEmailField.getText());
            if (tSpecialtyField.getText().trim().isEmpty()) {
                throw new InvalidDataException("Specialty cannot be empty.");
            }
            double salary = Double.parseDouble(tSalaryField.getText().trim());
            int exp = Integer.parseInt(tExpField.getText().trim());
            if (salary < 3000 || exp < 0) {
                throw new InvalidDataException("Enter a valid base salary (>=3000) and experience (>=0).");
            }
            Trainer t = new Trainer(tIdField.getText().trim().toUpperCase(), tNameField.getText().trim(),
                    tPhoneField.getText().trim(), tEmailField.getText().trim(),
                    tSpecialtyField.getText().trim(), salary, exp);

            trainerManager.add(t);
            saveDataQuietly();
            refreshAllTables();
            clearTrainerForm();
        } catch (NumberFormatException ex) {
            showError("Salary and Experience must be valid numbers!");
        } catch (InvalidDataException | DuplicateIdException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteTrainerAction() {
        try {
            trainerManager.remove(tIdField.getText().trim());
            syncTrainerMembers();
            saveDataQuietly();
            refreshAllTables();
            clearTrainerForm();
        } catch (NotFoundException ex) {
            showError(ex.getMessage());
        }
    }

    private void fillTrainerFormFromTable() {
        int row = trainerTable.getSelectedRow();
        if (row < 0) return;
        String id = trainerTableModel.getValueAt(row, 0).toString();
        try {
            Trainer t = trainerManager.findById(id);
            tIdField.setText(t.getId());
            tNameField.setText(t.getName());
            tPhoneField.setText(t.getPhone());
            tEmailField.setText(t.getEmail());
            tSpecialtyField.setText(t.getSpecialty());
            tSalaryField.setText(String.valueOf(t.getBaseSalary()));
            tExpField.setText(String.valueOf(t.getExperienceYears()));
        } catch (NotFoundException ignored) {
        }
    }

    private void clearTrainerForm() {
        trainerTable.clearSelection();
        tIdField.setText(String.format("T%03d", trainerManager.size() + 1));
        tNameField.setText("");
        tPhoneField.setText("");
        tEmailField.setText("");
        tSpecialtyField.setText("");
        tSalaryField.setText("7000");
        tExpField.setText("2");
    }

    // ==================== TAB 3: WORKOUTS ====================

    private JPanel createWorkoutsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        wFilterBox = new JComboBox<>();
        wFilterBox.addActionListener(e -> refreshWorkoutTable());
        filterPanel.add(new JLabel("Filter by Member:"));
        filterPanel.add(wFilterBox);

        String[] cols = {"Log ID", "Member", "Date", "Exercise", "Type", "Details", "Calories"};
        workoutTableModel = new DefaultTableModel(cols, 0);
        workoutTable = new JTable(workoutTableModel);
        workoutTable.setRowHeight(24);

        JPanel leftPanel = new JPanel(new BorderLayout(0, 8));
        leftPanel.add(filterPanel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(workoutTable), BorderLayout.CENTER);

        // Right Form (Demonstrates Method Overloading: Strength vs Cardio)
        JPanel form = new JPanel(new GridLayout(10, 2, 6, 8));
        form.setPreferredSize(new Dimension(310, 0));
        form.setBorder(BorderFactory.createTitledBorder("Log Workout"));

        wMemberBox = new JComboBox<>();
        wDateField = new JTextField(LocalDate.now().toString());
        wExerciseField = new JTextField();
        wTypeBox = new JComboBox<>(new String[]{"Strength", "Cardio"});
        wSetsField = new JTextField("4");
        wRepsField = new JTextField("10");
        wWeightField = new JTextField("60");
        wDurationField = new JTextField("30");
        wCaloriesField = new JTextField("300");

        wTypeBox.addActionListener(e -> toggleWorkoutFields());

        form.add(new JLabel("Member:"));          form.add(wMemberBox);
        form.add(new JLabel("Date:"));            form.add(wDateField);
        form.add(new JLabel("Exercise:"));        form.add(wExerciseField);
        form.add(new JLabel("Workout Type:"));    form.add(wTypeBox);
        form.add(new JLabel("Sets:"));            form.add(wSetsField);
        form.add(new JLabel("Reps:"));            form.add(wRepsField);
        form.add(new JLabel("Weight (kg):"));     form.add(wWeightField);
        form.add(new JLabel("Duration (min):"));  form.add(wDurationField);
        form.add(new JLabel("Calories (kcal):")); form.add(wCaloriesField);

        JButton logBtn = new JButton("Log Workout");
        JButton delLogBtn = new JButton("Delete Log");
        logBtn.addActionListener(e -> logWorkoutAction());
        delLogBtn.addActionListener(e -> deleteWorkoutAction());

        form.add(logBtn); form.add(delLogBtn);
        toggleWorkoutFields();

        panel.add(leftPanel, BorderLayout.CENTER);
        panel.add(form, BorderLayout.EAST);
        return panel;
    }

    private void toggleWorkoutFields() {
        boolean isStrength = "Strength".equals(wTypeBox.getSelectedItem());
        wSetsField.setEnabled(isStrength);
        wRepsField.setEnabled(isStrength);
        wWeightField.setEnabled(isStrength);
        wDurationField.setEnabled(!isStrength);
        wCaloriesField.setEnabled(!isStrength);
    }

    private void logWorkoutAction() {
        try {
            String memberId = extractId((String) wMemberBox.getSelectedItem());
            Member m = memberManager.findById(memberId);
            String exercise = wExerciseField.getText().trim();
            if (exercise.isEmpty()) {
                throw new InvalidDataException("Exercise name cannot be empty.");
            }
            String logId = generateWorkoutId();
            WorkoutLog log;

            if ("Strength".equals(wTypeBox.getSelectedItem())) {
                int sets = Integer.parseInt(wSetsField.getText().trim());
                int reps = Integer.parseInt(wRepsField.getText().trim());
                double weight = Double.parseDouble(wWeightField.getText().trim());
                if (sets <= 0 || reps <= 0 || weight < 0) {
                    throw new InvalidDataException("Sets and reps must be positive numbers.");
                }
                // Calls Overloaded Method 1 (Strength)
                log = m.addWorkout(logId, wDateField.getText().trim(), exercise, sets, reps, weight);
            } else {
                int duration = Integer.parseInt(wDurationField.getText().trim());
                int calories = Integer.parseInt(wCaloriesField.getText().trim());
                if (duration <= 0 || calories <= 0) {
                    throw new InvalidDataException("Duration and calories must be positive numbers.");
                }
                // Calls Overloaded Method 2 (Cardio)
                log = m.addWorkout(logId, wDateField.getText().trim(), exercise, duration, calories);
            }

            workoutManager.add(log);
            wExerciseField.setText("");
            saveDataQuietly();
            refreshAllTables();
        } catch (NumberFormatException ex) {
            showError("Please enter valid numeric values for workout fields!");
        } catch (InvalidDataException | NotFoundException | DuplicateIdException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteWorkoutAction() {
        int row = workoutTable.getSelectedRow();
        if (row < 0) {
            showError("Select a workout row to delete.");
            return;
        }
        String logId = workoutTableModel.getValueAt(row, 0).toString();
        try {
            workoutManager.remove(logId);
            saveDataQuietly();
            refreshAllTables();
        } catch (NotFoundException ex) {
            showError(ex.getMessage());
        }
    }

    private String generateWorkoutId() {
        int n = workoutManager.size() + 1;
        while (workoutManager.exists(String.format("W%03d", n))) {
            n++;
        }
        return String.format("W%03d", n);
    }

    // ==================== TAB 4: SUMMARY (POLYMORPHISM) ====================

    private JPanel createSummaryTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        summaryStatsLabel = new JLabel("Loading stats...");
        summaryStatsLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        summaryStatsLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                new EmptyBorder(10, 12, 10, 12)
        ));

        String[] cols = {"ID", "Name", "Class", "Role (Overridden)", "Monthly Amount", "Summary"};
        summaryTableModel = new DefaultTableModel(cols, 0);
        JTable summaryTable = new JTable(summaryTableModel);
        summaryTable.setRowHeight(24);

        panel.add(summaryStatsLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(summaryTable), BorderLayout.CENTER);
        return panel;
    }

    // ==================== REFRESH & VALIDATION HELPERS ====================

    private void refreshAllTables() {
        // Reload Trainer dropdown in Members tab
        String prevTrainer = (String) mTrainerBox.getSelectedItem();
        mTrainerBox.removeAllItems();
        mTrainerBox.addItem("NONE - No Trainer");
        for (Trainer t : trainerManager.getAll()) {
            mTrainerBox.addItem(t.getId() + " - " + t.getName());
        }
        if (prevTrainer != null) selectComboByPrefix(mTrainerBox, extractId(prevTrainer));

        // Reload Member dropdowns in Workouts tab
        wMemberBox.removeAllItems();
        wFilterBox.removeAllItems();
        wFilterBox.addItem("ALL - All Members");
        for (Member m : memberManager.getAll()) {
            String item = m.getId() + " - " + m.getName();
            wMemberBox.addItem(item);
            wFilterBox.addItem(item);
        }

        refreshMemberTable(memberManager.getAll());
        refreshTrainerTable();
        refreshWorkoutTable();
        refreshSummaryTab();
    }

    private void refreshMemberTable(ArrayList<Member> members) {
        memberTableModel.setRowCount(0);
        for (Member m : members) {
            memberTableModel.addRow(new Object[]{
                    m.getId(), m.getName(), m.getPhone(), m.getPlan(),
                    String.format("%.1f (%s)", m.calculateBMI(), m.getBmiStatus()),
                    m.getAssignedTrainerName(),
                    String.format("BDT %.0f", m.calculateMonthlyAmount())
            });
        }
    }

    private void refreshTrainerTable() {
        trainerTableModel.setRowCount(0);
        for (Trainer t : trainerManager.getAll()) {
            trainerTableModel.addRow(new Object[]{
                    t.getId(), t.getName(), t.getSpecialty(), t.getExperienceYears(),
                    String.format("BDT %.0f", t.getBaseSalary()),
                    t.getAssignedMembers().size(),
                    String.format("BDT %.0f", t.calculateMonthlyAmount())
            });
        }
    }

    private void refreshWorkoutTable() {
        if (workoutTableModel == null) return;
        String filterId = extractId((String) wFilterBox.getSelectedItem());
        workoutTableModel.setRowCount(0);

        for (WorkoutLog w : workoutManager.getAll()) {
            if ("ALL".equalsIgnoreCase(filterId) || w.getMemberId().equalsIgnoreCase(filterId)) {
                String details = "Strength".equalsIgnoreCase(w.getType())
                        ? w.getSets() + "x" + w.getReps() + " @ " + w.getWeightKg() + " kg"
                        : w.getDurationMinutes() + " min";
                workoutTableModel.addRow(new Object[]{
                        w.getId(), w.getMemberId(), w.getDate(), w.getExerciseName(),
                        w.getType(), details, w.getCaloriesBurned() + " kcal"
                });
            }
        }
    }

    private void refreshSummaryTab() {
        // Demonstrates Runtime Polymorphism using ArrayList<Person>
        ArrayList<Person> allPeople = new ArrayList<>();
        allPeople.addAll(trainerManager.getAll());
        allPeople.addAll(memberManager.getAll());

        double revenue = 0;
        double payroll = 0;
        summaryTableModel.setRowCount(0);

        for (Person p : allPeople) {
            double amount = p.calculateMonthlyAmount();
            if (p instanceof Member) {
                revenue += amount;
            } else if (p instanceof Trainer) {
                payroll += amount;
            }
            summaryTableModel.addRow(new Object[]{
                    p.getId(), p.getName(), p.getClass().getSimpleName(),
                    p.getRole(), String.format("BDT %.0f", amount), p.getSummary()
            });
        }

        summaryStatsLabel.setText(String.format(
                "Members: %d   |   Trainers: %d   |   Workouts: %d   |   Monthly Revenue: BDT %.0f   |   Trainer Payroll: BDT %.0f",
                memberManager.size(), trainerManager.size(), workoutManager.size(), revenue, payroll));
    }

    private void validatePersonFields(String id, String name, String phone, String email)
            throws InvalidDataException {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidDataException("ID cannot be empty.");
        }
        if (name == null || name.trim().length() < 2) {
            throw new InvalidDataException("Name must be at least 2 characters.");
        }
        if (phone == null || phone.trim().length() < 7) {
            throw new InvalidDataException("Please enter a valid phone number.");
        }
        if (email == null || !email.contains("@")) {
            throw new InvalidDataException("Please enter a valid email address.");
        }
    }

    private Trainer getSelectedTrainerObject() throws NotFoundException {
        String id = extractId((String) mTrainerBox.getSelectedItem());
        if ("NONE".equalsIgnoreCase(id) || id.isEmpty()) {
            return null;
        }
        return trainerManager.findById(id);
    }

    private String extractId(String comboText) {
        if (comboText == null) return "NONE";
        int idx = comboText.indexOf(" - ");
        return idx > 0 ? comboText.substring(0, idx).trim() : comboText.trim();
    }

    private void selectComboByPrefix(JComboBox<String> box, String prefix) {
        for (int i = 0; i < box.getItemCount(); i++) {
            String item = box.getItemAt(i);
            if (item != null && item.toUpperCase().startsWith(prefix.toUpperCase())) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input / System Error", JOptionPane.ERROR_MESSAGE);
    }
}
