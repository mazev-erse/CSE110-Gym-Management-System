package gym.ui;

import gym.exception.DataStorageException;
import gym.exception.DuplicateEntityException;
import gym.exception.EntityNotFoundException;
import gym.exception.InvalidInputException;
import gym.model.Member;
import gym.model.MembershipPlan;
import gym.model.Person;
import gym.model.Trainer;
import gym.model.WorkoutLog;
import gym.service.GymService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Executive Java Swing Dashboard for FitPulse Gym & Workout Management System.
 * Features modern obsidian sidebar navigation, live Java2D analytics charts,
 * instant search, pill badges, receipt exporting, and an interactive OOP/Exception Lab.
 */
public class GymManagementFrame extends JFrame {
    public static final String PAGE_DASHBOARD = "DASHBOARD";
    public static final String PAGE_MEMBERS = "MEMBERS";
    public static final String PAGE_TRAINERS = "TRAINERS";
    public static final String PAGE_WORKOUTS = "WORKOUTS";
    public static final String PAGE_OOP_LAB = "OOP_LAB";

    private final GymService gymService;
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageContainer = new JPanel(pageLayout);
    private final Map<String, ModernComponents.SidebarNavButton> navButtons = new LinkedHashMap<>();

    // Top Bar & Status
    private final JLabel pageTitleLabel = new JLabel("Executive Dashboard");
    private final JLabel pageSubtitleLabel = new JLabel("Real-time overview of memberships, trainers, workouts, and gym finances");
    private final JLabel statusLabel = new JLabel("Synced with local text storage.");

    // Dashboard KPIs & Charts
    private final JLabel kpiMembersVal = new JLabel("0");
    private final JLabel kpiMembersSub = new JLabel("Avg BMI: 0.0");
    private final JLabel kpiTrainersVal = new JLabel("0");
    private final JLabel kpiTrainersSub = new JLabel("Payroll: BDT 0");
    private final JLabel kpiWorkoutsVal = new JLabel("0");
    private final JLabel kpiWorkoutsSub = new JLabel("0 kcal burned");
    private final JLabel kpiRevenueVal = new JLabel("BDT 0");
    private final JLabel kpiRevenueSub = new JLabel("Monthly subscriptions");
    private AnalyticsChartPanel.FinancialBarChart barChart;
    private AnalyticsChartPanel.PlanDonutChart donutChart;
    private DefaultTableModel recentWorkoutsModel;

    // Members Page
    private DefaultTableModel memberTableModel;
    private JTable memberTable;
    private ModernComponents.ModernTextField memberSearchField;
    private ModernComponents.ModernTextField mIdField, mNameField, mPhoneField, mEmailField, mWeightField, mHeightField;
    private JComboBox<MembershipPlan> mPlanCombo;
    private JComboBox<String> mTrainerCombo;

    // Trainers Page
    private DefaultTableModel trainerTableModel;
    private JTable trainerTable;
    private ModernComponents.ModernTextField tIdField, tNameField, tPhoneField, tEmailField;
    private ModernComponents.ModernTextField tSpecialtyField, tSalaryField, tExpField;

    // Workouts Page
    private DefaultTableModel workoutTableModel;
    private JTable workoutTable;
    private JComboBox<String> wMemberSelectCombo;
    private JComboBox<String> wFilterMemberCombo;
    private JComboBox<String> wTypeCombo;
    private ModernComponents.ModernTextField wDateField, wExerciseField;
    private ModernComponents.ModernTextField wSetsField, wRepsField, wWeightUsedField;
    private ModernComponents.ModernTextField wDurationField, wCaloriesField;
    private JPanel wDynamicInputsPanel;
    private JLabel workoutSummaryLabel;

    // OOP & Exception Lab Page
    private DefaultTableModel polyTableModel;
    private JLabel labRevenueVal, labPayrollVal, labNetVal;
    private JTextArea exceptionConsoleArea;

    public GymManagementFrame(GymService gymService) {
        this.gymService = gymService;
        setTitle("FitPulse Pro — Gym & Workout Management System (CSE110 OOP)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 790);
        setMinimumSize(new Dimension(1120, 700));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Theme.CANVAS_BG);

        root.add(buildSidebar(), BorderLayout.WEST);

        JPanel rightWorkspace = new JPanel(new BorderLayout(0, 0));
        rightWorkspace.setBackground(Theme.CANVAS_BG);
        rightWorkspace.add(buildTopHeader(), BorderLayout.NORTH);

        pageContainer.setBackground(Theme.CANVAS_BG);
        pageContainer.setBorder(new EmptyBorder(16, 22, 14, 22));
        pageContainer.add(buildDashboardPage(), PAGE_DASHBOARD);
        pageContainer.add(buildMembersPage(), PAGE_MEMBERS);
        pageContainer.add(buildTrainersPage(), PAGE_TRAINERS);
        pageContainer.add(buildWorkoutsPage(), PAGE_WORKOUTS);
        pageContainer.add(buildOopLabPage(), PAGE_OOP_LAB);

        rightWorkspace.add(pageContainer, BorderLayout.CENTER);
        rightWorkspace.add(buildStatusBar(), BorderLayout.SOUTH);

        root.add(rightWorkspace, BorderLayout.CENTER);
        setContentPane(root);

        refreshAllViews();
        navigateTo(PAGE_DASHBOARD);
        setStatus("Loaded persisted records from: " + gymService.getDataDirectoryPath(), false);
    }

    // ==================== SIDEBAR & HEADER ====================

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(235, 0));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        top.add(new ModernComponents.BrandLogoPanel());
        top.add(Box.createVerticalStrut(10));

        JLabel navHeading = new JLabel("   NAVIGATION");
        navHeading.setFont(Theme.FONT_KPI_LABEL);
        navHeading.setForeground(new Color(100, 116, 139));
        navHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(navHeading);
        top.add(Box.createVerticalStrut(8));

        addNavButton(top, PAGE_DASHBOARD, "DASHBOARD", "Dashboard");
        addNavButton(top, PAGE_MEMBERS, "MEMBERS", "Members");
        addNavButton(top, PAGE_TRAINERS, "TRAINERS", "Trainers");
        addNavButton(top, PAGE_WORKOUTS, "WORKOUTS", "Workout Logbook");
        addNavButton(top, PAGE_OOP_LAB, "OOP_LAB", "OOP & Viva Lab");

        // Bottom Course Info Badge inside Sidebar
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(new EmptyBorder(14, 14, 18, 14));

        ModernComponents.RoundedPanel badge = new ModernComponents.RoundedPanel(
                10, new Color(30, 41, 59), new Color(51, 65, 85));
        badge.setLayout(new BoxLayout(badge, BoxLayout.Y_AXIS));
        badge.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel uniLbl = new JLabel("East West University");
        uniLbl.setFont(Theme.FONT_LABEL);
        uniLbl.setForeground(Color.WHITE);
        JLabel courseLbl = new JLabel("CSE110 • OOP Term Project");
        courseLbl.setFont(Theme.FONT_SMALL);
        courseLbl.setForeground(Theme.TEXT_MUTED);
        JLabel techLbl = new JLabel("Java Swing • File I/O");
        techLbl.setFont(Theme.FONT_SMALL);
        techLbl.setForeground(new Color(96, 165, 250));

        badge.add(uniLbl);
        badge.add(Box.createVerticalStrut(2));
        badge.add(courseLbl);
        badge.add(Box.createVerticalStrut(4));
        badge.add(techLbl);

        footer.add(badge);

        sidebar.add(top, BorderLayout.NORTH);
        sidebar.add(footer, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addNavButton(JPanel container, String pageKey, String icon, String label) {
        ModernComponents.SidebarNavButton btn = new ModernComponents.SidebarNavButton(icon, label);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> navigateTo(pageKey));
        navButtons.put(pageKey, btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(4));
    }

    public void navigateTo(String pageKey) {
        pageLayout.show(pageContainer, pageKey);
        for (Map.Entry<String, ModernComponents.SidebarNavButton> entry : navButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(pageKey));
        }
        switch (pageKey) {
            case PAGE_DASHBOARD:
                pageTitleLabel.setText("Executive Dashboard");
                pageSubtitleLabel.setText("Live gym metrics, financial comparison, and recent workout sessions");
                break;
            case PAGE_MEMBERS:
                pageTitleLabel.setText("Members Directory");
                pageSubtitleLabel.setText("Manage subscriptions, BMI health profiles, trainer links, and billing receipts");
                break;
            case PAGE_TRAINERS:
                pageTitleLabel.setText("Trainers & Payroll");
                pageSubtitleLabel.setText("Manage certified trainers, specialties, assigned trainees, and monthly payouts");
                break;
            case PAGE_WORKOUTS:
                pageTitleLabel.setText("Workout Logbook");
                pageSubtitleLabel.setText("Log Strength & Cardio sessions via overloaded methods and monitor training volume");
                break;
            case PAGE_OOP_LAB:
                pageTitleLabel.setText("OOP Polymorphism & Exception Lab");
                pageSubtitleLabel.setText("Live runtime polymorphism (List<Person>) and interactive exception testing for lab demo");
                break;
            default:
                break;
        }
        repaint();
    }

    private JPanel buildTopHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_LIGHT),
                new EmptyBorder(14, 24, 14, 24)
        ));

        JPanel titleStack = new JPanel(new GridLayout(2, 1, 0, 2));
        titleStack.setOpaque(false);

        pageTitleLabel.setFont(Theme.FONT_PAGE_TITLE);
        pageTitleLabel.setForeground(Theme.TEXT_PRIMARY);
        pageSubtitleLabel.setFont(Theme.FONT_SMALL);
        pageSubtitleLabel.setForeground(Theme.TEXT_SECONDARY);

        titleStack.add(pageTitleLabel);
        titleStack.add(pageSubtitleLabel);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        rightActions.setOpaque(false);

        ModernComponents.ModernButton reloadBtn = new ModernComponents.ModernButton("Reload Files", Theme.SLATE_BTN);
        ModernComponents.ModernButton saveBtn = new ModernComponents.ModernButton("Save All Data", Theme.EMERALD);

        reloadBtn.addActionListener(e -> {
            try {
                gymService.reloadFromDisk();
                refreshAllViews();
                setStatus("Reloaded all records from text files.", false);
                JOptionPane.showMessageDialog(this, "Reloaded members, trainers, and workouts from disk.",
                        "Reload Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (DataStorageException ex) {
                showError("Storage Read Error", ex.getMessage());
            }
        });
        saveBtn.addActionListener(e -> saveWithFeedback());

        rightActions.add(reloadBtn);
        rightActions.add(saveBtn);

        header.add(titleStack, BorderLayout.CENTER);
        header.add(rightActions, BorderLayout.EAST);
        return header;
    }

    // ==================== PAGE 1: DASHBOARD ====================

    private JPanel buildDashboardPage() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setOpaque(false);

        // Top KPI Row
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiRow.setOpaque(false);
        kpiRow.add(ModernComponents.createKpiCard("Active Members", kpiMembersVal, kpiMembersSub, Theme.PRIMARY));
        kpiRow.add(ModernComponents.createKpiCard("Certified Trainers", kpiTrainersVal, kpiTrainersSub, Theme.VIOLET));
        kpiRow.add(ModernComponents.createKpiCard("Workouts Logged", kpiWorkoutsVal, kpiWorkoutsSub, Theme.AMBER));
        kpiRow.add(ModernComponents.createKpiCard("Monthly Revenue", kpiRevenueVal, kpiRevenueSub, Theme.EMERALD));

        // Middle Charts Row
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsRow.setOpaque(false);

        ModernComponents.RoundedPanel barCard = new ModernComponents.RoundedPanel(14);
        barCard.setLayout(new BorderLayout(0, 8));
        barCard.setBorder(new EmptyBorder(14, 16, 12, 16));
        JLabel barTitle = new JLabel("Financial & Training Volume Overview");
        barTitle.setFont(Theme.FONT_SECTION);
        barTitle.setForeground(Theme.TEXT_PRIMARY);
        barChart = new AnalyticsChartPanel.FinancialBarChart(gymService);
        barCard.add(barTitle, BorderLayout.NORTH);
        barCard.add(barChart, BorderLayout.CENTER);

        ModernComponents.RoundedPanel donutCard = new ModernComponents.RoundedPanel(14);
        donutCard.setLayout(new BorderLayout(0, 8));
        donutCard.setBorder(new EmptyBorder(14, 16, 12, 16));
        JLabel donutTitle = new JLabel("Membership Plan Distribution");
        donutTitle.setFont(Theme.FONT_SECTION);
        donutTitle.setForeground(Theme.TEXT_PRIMARY);
        donutChart = new AnalyticsChartPanel.PlanDonutChart(gymService);
        donutCard.add(donutTitle, BorderLayout.NORTH);
        donutCard.add(donutChart, BorderLayout.CENTER);

        chartsRow.add(barCard);
        chartsRow.add(donutCard);

        // Bottom Recent Workouts Card
        ModernComponents.RoundedPanel recentCard = new ModernComponents.RoundedPanel(14);
        recentCard.setLayout(new BorderLayout(0, 10));
        recentCard.setBorder(new EmptyBorder(14, 16, 14, 16));
        recentCard.setPreferredSize(new Dimension(0, 210));

        JPanel recentHeader = new JPanel(new BorderLayout());
        recentHeader.setOpaque(false);
        JLabel recentTitle = new JLabel("Recent Workout Activity");
        recentTitle.setFont(Theme.FONT_SECTION);
        recentTitle.setForeground(Theme.TEXT_PRIMARY);
        ModernComponents.ModernButton goLogBtn = new ModernComponents.ModernButton("+ Log Workout", Theme.PRIMARY);
        goLogBtn.addActionListener(e -> navigateTo(PAGE_WORKOUTS));
        recentHeader.add(recentTitle, BorderLayout.WEST);
        recentHeader.add(goLogBtn, BorderLayout.EAST);

        String[] cols = {"Log ID", "Member", "Date", "Exercise", "Category", "Metrics", "Calories"};
        recentWorkoutsModel = nonEditableModel(cols);
        JTable recentTable = ModernComponents.createModernTable(recentWorkoutsModel);
        recentTable.getColumnModel().getColumn(4).setCellRenderer(new ModernComponents.BadgeCellRenderer());

        recentCard.add(recentHeader, BorderLayout.NORTH);
        recentCard.add(styledScrollPane(recentTable), BorderLayout.CENTER);

        JPanel centerStack = new JPanel(new BorderLayout(0, 16));
        centerStack.setOpaque(false);
        centerStack.add(chartsRow, BorderLayout.CENTER);
        centerStack.add(recentCard, BorderLayout.SOUTH);

        page.add(kpiRow, BorderLayout.NORTH);
        page.add(centerStack, BorderLayout.CENTER);
        return page;
    }

    // ==================== PAGE 2: MEMBERS ====================

    private JPanel buildMembersPage() {
        JPanel page = new JPanel(new BorderLayout(16, 0));
        page.setOpaque(false);

        // Left: Live Search + Table Card
        ModernComponents.RoundedPanel tableCard = new ModernComponents.RoundedPanel(14);
        tableCard.setLayout(new BorderLayout(0, 12));
        tableCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setOpaque(false);
        JLabel searchLbl = new JLabel("Instant Filter:");
        searchLbl.setFont(Theme.FONT_LABEL);
        searchLbl.setForeground(Theme.TEXT_SECONDARY);
        memberSearchField = new ModernComponents.ModernTextField();
        memberSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshMemberTable(gymService.searchMembers(memberSearchField.getText()));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshMemberTable(gymService.searchMembers(memberSearchField.getText()));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshMemberTable(gymService.searchMembers(memberSearchField.getText()));
            }
        });

        ModernComponents.ModernButton receiptBtn = new ModernComponents.ModernButton("Export Bill Receipt", Theme.VIOLET);
        receiptBtn.addActionListener(e -> handleExportReceipt());

        searchBar.add(searchLbl, BorderLayout.WEST);
        searchBar.add(memberSearchField, BorderLayout.CENTER);
        searchBar.add(receiptBtn, BorderLayout.EAST);

        String[] cols = {"ID", "Name", "Plan", "BMI Health", "Assigned Trainer", "Monthly Bill"};
        memberTableModel = nonEditableModel(cols);
        memberTable = ModernComponents.createModernTable(memberTableModel);
        memberTable.getColumnModel().getColumn(2).setCellRenderer(new ModernComponents.BadgeCellRenderer());
        memberTable.getColumnModel().getColumn(3).setCellRenderer(new ModernComponents.BadgeCellRenderer());
        setColumnWidths(memberTable, 66, 120, 92, 152, 118, 92);
        memberTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && memberTable.getSelectedRow() >= 0) {
                populateMemberFormFromSelectedRow();
            }
        });

        tableCard.add(searchBar, BorderLayout.NORTH);
        tableCard.add(styledScrollPane(memberTable), BorderLayout.CENTER);

        // Right: Member Editor Card
        ModernComponents.RoundedPanel formCard = new ModernComponents.RoundedPanel(14);
        formCard.setLayout(new BorderLayout(0, 10));
        formCard.setPreferredSize(new Dimension(345, 0));
        formCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel formTitle = new JLabel("Member Profile Editor");
        formTitle.setFont(Theme.FONT_SECTION);
        formTitle.setForeground(Theme.TEXT_PRIMARY);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = createFormGbc();

        mIdField = new ModernComponents.ModernTextField("M007");
        mNameField = new ModernComponents.ModernTextField();
        mPhoneField = new ModernComponents.ModernTextField();
        mEmailField = new ModernComponents.ModernTextField();
        mPlanCombo = new JComboBox<>(MembershipPlan.values());
        ModernComponents.styleComboBox(mPlanCombo);
        mWeightField = new ModernComponents.ModernTextField("70.0");
        mHeightField = new ModernComponents.ModernTextField("172.0");
        mTrainerCombo = new JComboBox<>();
        ModernComponents.styleComboBox(mTrainerCombo);

        addFormRow(fields, gbc, 0, "Member ID (e.g., M007)", mIdField);
        addFormRow(fields, gbc, 1, "Full Name", mNameField);
        addFormRow(fields, gbc, 2, "Phone Number", mPhoneField);
        addFormRow(fields, gbc, 3, "Email Address", mEmailField);
        addFormRow(fields, gbc, 4, "Membership Tier", mPlanCombo);

        // Side-by-side Weight & Height
        JPanel metricsRow = new JPanel(new GridLayout(1, 2, 10, 0));
        metricsRow.setOpaque(false);
        metricsRow.add(wrapLabeled("Weight (kg)", mWeightField));
        metricsRow.add(wrapLabeled("Height (cm)", mHeightField));
        gbc.gridx = 0;
        gbc.gridy = 10;
        fields.add(metricsRow, gbc);

        addFormRow(fields, gbc, 6, "Personal Trainer (+BDT 1,500)", mTrainerCombo);

        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGrid.setOpaque(false);
        ModernComponents.ModernButton addBtn = new ModernComponents.ModernButton("Add Member", Theme.EMERALD);
        ModernComponents.ModernButton updateBtn = new ModernComponents.ModernButton("Update", Theme.PRIMARY);
        ModernComponents.ModernButton deleteBtn = new ModernComponents.ModernButton("Delete", Theme.ROSE);
        ModernComponents.ModernButton clearBtn = new ModernComponents.ModernButton("Clear", Theme.SLATE_BTN);

        addBtn.addActionListener(e -> handleAddMember());
        updateBtn.addActionListener(e -> handleUpdateMember());
        deleteBtn.addActionListener(e -> handleDeleteMember());
        clearBtn.addActionListener(e -> clearMemberForm());

        btnGrid.add(addBtn);
        btnGrid.add(updateBtn);
        btnGrid.add(deleteBtn);
        btnGrid.add(clearBtn);

        formCard.add(formTitle, BorderLayout.NORTH);
        formCard.add(fields, BorderLayout.CENTER);
        formCard.add(btnGrid, BorderLayout.SOUTH);

        page.add(tableCard, BorderLayout.CENTER);
        page.add(formCard, BorderLayout.EAST);
        return page;
    }

    private void handleAddMember() {
        try {
            double weight = parseDoubleField(mWeightField.getText(), "Weight (kg)");
            double height = parseDoubleField(mHeightField.getText(), "Height (cm)");
            String trainerId = extractSelectedId((String) mTrainerCombo.getSelectedItem());
            gymService.addMember(
                    mIdField.getText(), mNameField.getText(), mPhoneField.getText(),
                    mEmailField.getText(), (MembershipPlan) mPlanCombo.getSelectedItem(),
                    weight, height, trainerId
            );
            autoSaveAndRefresh("Added member " + mIdField.getText().trim().toUpperCase());
            clearMemberForm();
        } catch (InvalidInputException | DuplicateEntityException | EntityNotFoundException ex) {
            showError("Member Validation Error", ex.getMessage());
        }
    }

    private void handleUpdateMember() {
        try {
            double weight = parseDoubleField(mWeightField.getText(), "Weight (kg)");
            double height = parseDoubleField(mHeightField.getText(), "Height (cm)");
            String trainerId = extractSelectedId((String) mTrainerCombo.getSelectedItem());
            gymService.updateMember(
                    mIdField.getText(), mNameField.getText(), mPhoneField.getText(),
                    mEmailField.getText(), (MembershipPlan) mPlanCombo.getSelectedItem(),
                    weight, height, trainerId
            );
            autoSaveAndRefresh("Updated member " + mIdField.getText().trim().toUpperCase());
        } catch (InvalidInputException | EntityNotFoundException ex) {
            showError("Member Update Error", ex.getMessage());
        }
    }

    private void handleDeleteMember() {
        String id = mIdField.getText().trim();
        if (id.isEmpty()) {
            showError("Selection Required", "Select a member row or enter an ID to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete member '" + id + "' and all associated workout logs?",
                "Confirm Member Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                gymService.deleteMember(id);
                autoSaveAndRefresh("Deleted member " + id.toUpperCase());
                clearMemberForm();
            } catch (EntityNotFoundException ex) {
                showError("Not Found", ex.getMessage());
            }
        }
    }

    private void handleExportReceipt() {
        String id = mIdField.getText().trim();
        if (memberTable.getSelectedRow() >= 0) {
            id = String.valueOf(memberTableModel.getValueAt(memberTable.getSelectedRow(), 0));
        }
        try {
            Member member = gymService.findMember(id);
            File saved = gymService.exportMemberReceipt(member.getId());
            setStatus("Exported billing receipt to: " + saved.getAbsolutePath(), false);

            JTextArea preview = new JTextArea(member.generateReceiptText()
                    + "\nSaved file: " + saved.getAbsolutePath());
            preview.setFont(Theme.FONT_MONO);
            preview.setEditable(false);
            preview.setBorder(new EmptyBorder(10, 12, 10, 12));
            JOptionPane.showMessageDialog(this, new JScrollPane(preview),
                    "Receipt Exported — " + member.getId(), JOptionPane.INFORMATION_MESSAGE);
        } catch (EntityNotFoundException | DataStorageException ex) {
            showError("Receipt Export Error", ex.getMessage());
        }
    }

    private void populateMemberFormFromSelectedRow() {
        int row = memberTable.getSelectedRow();
        if (row < 0) return;
        String id = String.valueOf(memberTableModel.getValueAt(row, 0));
        try {
            Member m = gymService.findMember(id);
            mIdField.setText(m.getId());
            mNameField.setText(m.getName());
            mPhoneField.setText(m.getPhone());
            mEmailField.setText(m.getEmail());
            mPlanCombo.setSelectedItem(m.getPlan());
            mWeightField.setText(String.valueOf(m.getWeightKg()));
            mHeightField.setText(String.valueOf(m.getHeightCm()));
            selectComboByPrefix(mTrainerCombo, m.getAssignedTrainerId());
        } catch (EntityNotFoundException ignored) {
        }
    }

    private void clearMemberForm() {
        memberTable.clearSelection();
        mIdField.setText(String.format("M%03d", gymService.getAllMembers().size() + 1));
        mNameField.setText("");
        mPhoneField.setText("");
        mEmailField.setText("");
        mPlanCombo.setSelectedIndex(0);
        mWeightField.setText("70.0");
        mHeightField.setText("172.0");
        if (mTrainerCombo.getItemCount() > 0) mTrainerCombo.setSelectedIndex(0);
    }

    // ==================== PAGE 3: TRAINERS ====================

    private JPanel buildTrainersPage() {
        JPanel page = new JPanel(new BorderLayout(16, 0));
        page.setOpaque(false);

        ModernComponents.RoundedPanel tableCard = new ModernComponents.RoundedPanel(14);
        tableCard.setLayout(new BorderLayout(0, 12));
        tableCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Certified Trainers Roster (+BDT 750 Commission per Assigned Member)");
        title.setFont(Theme.FONT_SECTION);
        title.setForeground(Theme.TEXT_PRIMARY);

        String[] cols = {"ID", "Name", "Specialty", "Exp", "Base Salary", "Trainees", "Monthly Payout"};
        trainerTableModel = nonEditableModel(cols);
        trainerTable = ModernComponents.createModernTable(trainerTableModel);
        trainerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && trainerTable.getSelectedRow() >= 0) {
                populateTrainerFormFromSelectedRow();
            }
        });

        tableCard.add(title, BorderLayout.NORTH);
        tableCard.add(styledScrollPane(trainerTable), BorderLayout.CENTER);

        ModernComponents.RoundedPanel formCard = new ModernComponents.RoundedPanel(14);
        formCard.setLayout(new BorderLayout(0, 10));
        formCard.setPreferredSize(new Dimension(345, 0));
        formCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel formTitle = new JLabel("Trainer Profile Editor");
        formTitle.setFont(Theme.FONT_SECTION);
        formTitle.setForeground(Theme.TEXT_PRIMARY);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = createFormGbc();

        tIdField = new ModernComponents.ModernTextField("T004");
        tNameField = new ModernComponents.ModernTextField();
        tPhoneField = new ModernComponents.ModernTextField();
        tEmailField = new ModernComponents.ModernTextField();
        tSpecialtyField = new ModernComponents.ModernTextField();
        tSalaryField = new ModernComponents.ModernTextField("18000");
        tExpField = new ModernComponents.ModernTextField("3");

        addFormRow(fields, gbc, 0, "Trainer ID (e.g., T004)", tIdField);
        addFormRow(fields, gbc, 1, "Full Name", tNameField);
        addFormRow(fields, gbc, 2, "Phone Number", tPhoneField);
        addFormRow(fields, gbc, 3, "Email Address", tEmailField);
        addFormRow(fields, gbc, 4, "Training Specialty", tSpecialtyField);

        JPanel compRow = new JPanel(new GridLayout(1, 2, 10, 0));
        compRow.setOpaque(false);
        compRow.add(wrapLabeled("Base Salary (BDT)", tSalaryField));
        compRow.add(wrapLabeled("Exp (Years)", tExpField));
        gbc.gridx = 0;
        gbc.gridy = 10;
        fields.add(compRow, gbc);

        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGrid.setOpaque(false);
        ModernComponents.ModernButton addBtn = new ModernComponents.ModernButton("Add Trainer", Theme.EMERALD);
        ModernComponents.ModernButton updateBtn = new ModernComponents.ModernButton("Update", Theme.PRIMARY);
        ModernComponents.ModernButton deleteBtn = new ModernComponents.ModernButton("Delete", Theme.ROSE);
        ModernComponents.ModernButton clearBtn = new ModernComponents.ModernButton("Clear", Theme.SLATE_BTN);

        addBtn.addActionListener(e -> handleAddTrainer());
        updateBtn.addActionListener(e -> handleUpdateTrainer());
        deleteBtn.addActionListener(e -> handleDeleteTrainer());
        clearBtn.addActionListener(e -> clearTrainerForm());

        btnGrid.add(addBtn);
        btnGrid.add(updateBtn);
        btnGrid.add(deleteBtn);
        btnGrid.add(clearBtn);

        formCard.add(formTitle, BorderLayout.NORTH);
        formCard.add(fields, BorderLayout.CENTER);
        formCard.add(btnGrid, BorderLayout.SOUTH);

        page.add(tableCard, BorderLayout.CENTER);
        page.add(formCard, BorderLayout.EAST);
        return page;
    }

    private void handleAddTrainer() {
        try {
            double salary = parseDoubleField(tSalaryField.getText(), "Base Salary");
            int exp = parseIntField(tExpField.getText(), "Experience (Years)");
            gymService.addTrainer(tIdField.getText(), tNameField.getText(), tPhoneField.getText(),
                    tEmailField.getText(), tSpecialtyField.getText(), salary, exp);
            autoSaveAndRefresh("Added trainer " + tIdField.getText().trim().toUpperCase());
            clearTrainerForm();
        } catch (InvalidInputException | DuplicateEntityException ex) {
            showError("Trainer Validation Error", ex.getMessage());
        }
    }

    private void handleUpdateTrainer() {
        try {
            double salary = parseDoubleField(tSalaryField.getText(), "Base Salary");
            int exp = parseIntField(tExpField.getText(), "Experience (Years)");
            gymService.updateTrainer(tIdField.getText(), tNameField.getText(), tPhoneField.getText(),
                    tEmailField.getText(), tSpecialtyField.getText(), salary, exp);
            autoSaveAndRefresh("Updated trainer " + tIdField.getText().trim().toUpperCase());
        } catch (InvalidInputException | EntityNotFoundException ex) {
            showError("Trainer Update Error", ex.getMessage());
        }
    }

    private void handleDeleteTrainer() {
        String id = tIdField.getText().trim();
        if (id.isEmpty()) {
            showError("Selection Required", "Select or enter a Trainer ID to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete trainer '" + id + "'? Assigned members will be unassigned.",
                "Confirm Trainer Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                gymService.deleteTrainer(id);
                autoSaveAndRefresh("Deleted trainer " + id.toUpperCase());
                clearTrainerForm();
            } catch (EntityNotFoundException ex) {
                showError("Not Found", ex.getMessage());
            }
        }
    }

    private void populateTrainerFormFromSelectedRow() {
        int row = trainerTable.getSelectedRow();
        if (row < 0) return;
        String id = String.valueOf(trainerTableModel.getValueAt(row, 0));
        try {
            Trainer t = gymService.findTrainer(id);
            tIdField.setText(t.getId());
            tNameField.setText(t.getName());
            tPhoneField.setText(t.getPhone());
            tEmailField.setText(t.getEmail());
            tSpecialtyField.setText(t.getSpecialty());
            tSalaryField.setText(String.valueOf(t.getBaseSalary()));
            tExpField.setText(String.valueOf(t.getExperienceYears()));
        } catch (EntityNotFoundException ignored) {
        }
    }

    private void clearTrainerForm() {
        trainerTable.clearSelection();
        tIdField.setText(String.format("T%03d", gymService.getAllTrainers().size() + 1));
        tNameField.setText("");
        tPhoneField.setText("");
        tEmailField.setText("");
        tSpecialtyField.setText("");
        tSalaryField.setText("18000");
        tExpField.setText("3");
    }

    // ==================== PAGE 4: WORKOUT LOGBOOK ====================

    private JPanel buildWorkoutsPage() {
        JPanel page = new JPanel(new BorderLayout(16, 0));
        page.setOpaque(false);

        ModernComponents.RoundedPanel tableCard = new ModernComponents.RoundedPanel(14);
        tableCard.setLayout(new BorderLayout(0, 12));
        tableCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel filterRow = new JPanel(new BorderLayout(10, 0));
        filterRow.setOpaque(false);

        JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftFilter.setOpaque(false);
        JLabel flbl = new JLabel("Filter Member:");
        flbl.setFont(Theme.FONT_LABEL);
        flbl.setForeground(Theme.TEXT_SECONDARY);
        wFilterMemberCombo = new JComboBox<>();
        ModernComponents.styleComboBox(wFilterMemberCombo);
        wFilterMemberCombo.setPreferredSize(new Dimension(220, 32));
        wFilterMemberCombo.addActionListener(e -> refreshWorkoutTable());
        leftFilter.add(flbl);
        leftFilter.add(wFilterMemberCombo);

        workoutSummaryLabel = new JLabel("Volume: 0 kg  •  Burned: 0 kcal");
        workoutSummaryLabel.setFont(Theme.FONT_LABEL);
        workoutSummaryLabel.setForeground(Theme.PRIMARY);

        filterRow.add(leftFilter, BorderLayout.WEST);
        filterRow.add(workoutSummaryLabel, BorderLayout.EAST);

        String[] cols = {"Log ID", "Member", "Date", "Exercise", "Category", "Sets x Reps @ Kg", "Duration", "Calories"};
        workoutTableModel = nonEditableModel(cols);
        workoutTable = ModernComponents.createModernTable(workoutTableModel);
        workoutTable.getColumnModel().getColumn(4).setCellRenderer(new ModernComponents.BadgeCellRenderer());
        setColumnWidths(workoutTable, 64, 66, 94, 134, 84, 114, 68, 78);

        ModernComponents.ModernButton delLogBtn = new ModernComponents.ModernButton("Delete Selected Log", Theme.ROSE);
        delLogBtn.addActionListener(e -> handleDeleteWorkout());
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomBar.setOpaque(false);
        bottomBar.add(delLogBtn);

        tableCard.add(filterRow, BorderLayout.NORTH);
        tableCard.add(styledScrollPane(workoutTable), BorderLayout.CENTER);
        tableCard.add(bottomBar, BorderLayout.SOUTH);

        // Right: Overloaded Workout Logger
        ModernComponents.RoundedPanel formCard = new ModernComponents.RoundedPanel(14);
        formCard.setLayout(new BorderLayout(0, 10));
        formCard.setPreferredSize(new Dimension(345, 0));
        formCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel formTitle = new JLabel("Log Session (Method Overloading)");
        formTitle.setFont(Theme.FONT_SECTION);
        formTitle.setForeground(Theme.TEXT_PRIMARY);

        JPanel topFields = new JPanel(new GridBagLayout());
        topFields.setOpaque(false);
        GridBagConstraints gbc = createFormGbc();

        wMemberSelectCombo = new JComboBox<>();
        ModernComponents.styleComboBox(wMemberSelectCombo);
        wDateField = new ModernComponents.ModernTextField(LocalDate.now().toString());
        wExerciseField = new ModernComponents.ModernTextField();
        wTypeCombo = new JComboBox<>(new String[]{"Strength (Sets / Reps / Weight)", "Cardio (Duration / Calories)"});
        ModernComponents.styleComboBox(wTypeCombo);

        addFormRow(topFields, gbc, 0, "Member", wMemberSelectCombo);
        addFormRow(topFields, gbc, 1, "Date (YYYY-MM-DD)", wDateField);
        addFormRow(topFields, gbc, 2, "Exercise Name", wExerciseField);
        addFormRow(topFields, gbc, 3, "Workout Mode", wTypeCombo);

        CardLayout dynamicCards = new CardLayout();
        wDynamicInputsPanel = new JPanel(dynamicCards);
        wDynamicInputsPanel.setOpaque(false);

        JPanel strengthPanel = new JPanel(new GridBagLayout());
        strengthPanel.setOpaque(false);
        GridBagConstraints sgbc = createFormGbc();
        wSetsField = new ModernComponents.ModernTextField("4");
        wRepsField = new ModernComponents.ModernTextField("8");
        wWeightUsedField = new ModernComponents.ModernTextField("75.0");
        addFormRow(strengthPanel, sgbc, 0, "Sets", wSetsField);
        addFormRow(strengthPanel, sgbc, 1, "Reps per Set", wRepsField);
        addFormRow(strengthPanel, sgbc, 2, "Weight Used (kg)", wWeightUsedField);

        JPanel cardioPanel = new JPanel(new GridBagLayout());
        cardioPanel.setOpaque(false);
        GridBagConstraints cgbc = createFormGbc();
        wDurationField = new ModernComponents.ModernTextField("30");
        wCaloriesField = new ModernComponents.ModernTextField("320");
        addFormRow(cardioPanel, cgbc, 0, "Duration (Minutes)", wDurationField);
        addFormRow(cardioPanel, cgbc, 1, "Calories Burned (kcal)", wCaloriesField);

        wDynamicInputsPanel.add(strengthPanel, "STRENGTH");
        wDynamicInputsPanel.add(cardioPanel, "CARDIO");

        wTypeCombo.addActionListener(e -> dynamicCards.show(
                wDynamicInputsPanel, wTypeCombo.getSelectedIndex() == 0 ? "STRENGTH" : "CARDIO"));

        JPanel combined = new JPanel(new BorderLayout(0, 4));
        combined.setOpaque(false);
        combined.add(topFields, BorderLayout.NORTH);
        combined.add(wDynamicInputsPanel, BorderLayout.CENTER);

        ModernComponents.ModernButton saveLogBtn = new ModernComponents.ModernButton("Save Workout Entry", Theme.EMERALD);
        saveLogBtn.setPreferredSize(new Dimension(0, 40));
        saveLogBtn.addActionListener(e -> handleLogWorkout());

        formCard.add(formTitle, BorderLayout.NORTH);
        formCard.add(combined, BorderLayout.CENTER);
        formCard.add(saveLogBtn, BorderLayout.SOUTH);

        page.add(tableCard, BorderLayout.CENTER);
        page.add(formCard, BorderLayout.EAST);
        return page;
    }

    private void handleLogWorkout() {
        String memberId = extractSelectedId((String) wMemberSelectCombo.getSelectedItem());
        if (memberId.equals("NONE") || memberId.isEmpty()) {
            showError("No Member", "Add and select a member before logging workouts.");
            return;
        }
        try {
            if (wTypeCombo.getSelectedIndex() == 0) {
                int sets = parseIntField(wSetsField.getText(), "Sets");
                int reps = parseIntField(wRepsField.getText(), "Reps");
                double weight = parseDoubleField(wWeightUsedField.getText(), "Weight Used (kg)");
                gymService.logWorkout(memberId, wDateField.getText(), wExerciseField.getText(), sets, reps, weight);
            } else {
                int duration = parseIntField(wDurationField.getText(), "Duration (Minutes)");
                int calories = parseIntField(wCaloriesField.getText(), "Calories Burned");
                gymService.logWorkout(memberId, wDateField.getText(), wExerciseField.getText(), duration, calories);
            }
            wExerciseField.setText("");
            autoSaveAndRefresh("Logged workout for " + memberId);
        } catch (InvalidInputException | EntityNotFoundException | DuplicateEntityException ex) {
            showError("Workout Logging Error", ex.getMessage());
        }
    }

    private void handleDeleteWorkout() {
        int row = workoutTable.getSelectedRow();
        if (row < 0) {
            showError("Selection Required", "Select a workout row to delete.");
            return;
        }
        String logId = String.valueOf(workoutTableModel.getValueAt(row, 0));
        try {
            gymService.deleteWorkout(logId);
            autoSaveAndRefresh("Deleted workout log " + logId);
        } catch (EntityNotFoundException ex) {
            showError("Delete Error", ex.getMessage());
        }
    }

    // ==================== PAGE 5: OOP & EXCEPTION LAB ====================

    private JPanel buildOopLabPage() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setOpaque(false);

        // Top Financial Strip
        JPanel topRow = new JPanel(new GridLayout(1, 3, 14, 0));
        topRow.setOpaque(false);
        labRevenueVal = new JLabel("BDT 0");
        labPayrollVal = new JLabel("BDT 0");
        labNetVal = new JLabel("BDT 0");
        topRow.add(ModernComponents.createKpiCard("Monthly Inflow (Members)", labRevenueVal,
                new JLabel("Sum of Member.calculateMonthlyAmount()"), Theme.EMERALD));
        topRow.add(ModernComponents.createKpiCard("Monthly Outflow (Trainers)", labPayrollVal,
                new JLabel("Sum of Trainer.calculateMonthlyAmount()"), Theme.ROSE));
        topRow.add(ModernComponents.createKpiCard("Net Monthly Balance", labNetVal,
                new JLabel("Revenue minus Trainer Payroll"), Theme.PRIMARY));

        // Middle: Polymorphic List<Person> Table
        ModernComponents.RoundedPanel polyCard = new ModernComponents.RoundedPanel(14);
        polyCard.setLayout(new BorderLayout(0, 10));
        polyCard.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel polyTitle = new JLabel("Runtime Polymorphism Dispatch — Iterating Over List<Person>");
        polyTitle.setFont(Theme.FONT_SECTION);
        polyTitle.setForeground(Theme.TEXT_PRIMARY);

        String[] cols = {"ID", "Name", "Concrete Subclass", "Overridden getRole()", "calculateMonthlyAmount()", "Polymorphic getSummary()"};
        polyTableModel = nonEditableModel(cols);
        JTable polyTable = ModernComponents.createModernTable(polyTableModel);
        polyTable.setRowHeight(29);
        polyTable.getColumnModel().getColumn(2).setCellRenderer(new ModernComponents.BadgeCellRenderer());
        setColumnWidths(polyTable, 62, 118, 108, 175, 175, 320);

        polyCard.add(polyTitle, BorderLayout.NORTH);
        polyCard.add(styledScrollPane(polyTable), BorderLayout.CENTER);

        // Bottom: Interactive Exception Handling Sandbox (Viva Helper!)
        ModernComponents.RoundedPanel exCard = new ModernComponents.RoundedPanel(14);
        exCard.setLayout(new BorderLayout(0, 8));
        exCard.setBorder(new EmptyBorder(12, 16, 14, 16));
        exCard.setPreferredSize(new Dimension(0, 182));

        JPanel exHeader = new JPanel(new BorderLayout(0, 6));
        exHeader.setOpaque(false);
        JLabel exTitle = new JLabel("Interactive Exception Handling Sandbox (Click to Demonstrate Checked Exceptions)");
        exTitle.setFont(Theme.FONT_SECTION);
        exTitle.setForeground(Theme.TEXT_PRIMARY);

        JPanel exButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        exButtons.setOpaque(false);
        ModernComponents.ModernButton testDupBtn = new ModernComponents.ModernButton("Trigger DuplicateEntityException", Theme.ROSE);
        ModernComponents.ModernButton testNotFoundBtn = new ModernComponents.ModernButton("Trigger EntityNotFoundException", Theme.AMBER);
        ModernComponents.ModernButton testInvalidBtn = new ModernComponents.ModernButton("Trigger InvalidInputException", Theme.VIOLET);

        testDupBtn.addActionListener(e -> runExceptionDemo("DUPLICATE"));
        testNotFoundBtn.addActionListener(e -> runExceptionDemo("NOT_FOUND"));
        testInvalidBtn.addActionListener(e -> runExceptionDemo("INVALID"));

        exButtons.add(testDupBtn);
        exButtons.add(testNotFoundBtn);
        exButtons.add(testInvalidBtn);

        exHeader.add(exTitle, BorderLayout.NORTH);
        exHeader.add(exButtons, BorderLayout.CENTER);

        exceptionConsoleArea = new JTextArea(
                "[READY] Click any button above to trigger and gracefully catch our custom checked exceptions.\n"
                        + "Active Exception Hierarchy: DuplicateEntityException, EntityNotFoundException, InvalidInputException, DataStorageException.");
        exceptionConsoleArea.setFont(Theme.FONT_MONO);
        exceptionConsoleArea.setLineWrap(true);
        exceptionConsoleArea.setWrapStyleWord(true);
        exceptionConsoleArea.setEditable(false);
        exceptionConsoleArea.setBackground(new Color(15, 23, 42));
        exceptionConsoleArea.setForeground(new Color(134, 239, 172));
        exceptionConsoleArea.setBorder(new EmptyBorder(10, 12, 10, 12));

        exCard.add(exHeader, BorderLayout.NORTH);
        exCard.add(new JScrollPane(exceptionConsoleArea), BorderLayout.CENTER);

        page.add(topRow, BorderLayout.NORTH);
        page.add(polyCard, BorderLayout.CENTER);
        page.add(exCard, BorderLayout.SOUTH);
        return page;
    }

    private void runExceptionDemo(String mode) {
        if ("DUPLICATE".equals(mode)) {
            try {
                gymService.addMember("M001", "Duplicate Test", "01711111111", "dup@ewubd.edu",
                        MembershipPlan.BASIC, 70.0, 170.0, "NONE");
            } catch (DuplicateEntityException ex) {
                exceptionConsoleArea.setText("[CAUGHT DuplicateEntityException]\n"
                        + "Trigger : Attempting to register duplicate ID 'M001' into DataRepository<Member>\n"
                        + "Message : " + ex.getMessage());
            } catch (Exception ex) {
                exceptionConsoleArea.setText("[CAUGHT " + ex.getClass().getSimpleName() + "] " + ex.getMessage());
            }
        } else if ("NOT_FOUND".equals(mode)) {
            try {
                gymService.findMember("M999_UNKNOWN");
            } catch (EntityNotFoundException ex) {
                exceptionConsoleArea.setText("[CAUGHT EntityNotFoundException]\n"
                        + "Trigger : Searching for non-existent ID 'M999_UNKNOWN' in DataRepository<Member>\n"
                        + "Message : " + ex.getMessage());
            }
        } else {
            try {
                gymService.addMember("M888", "X", "bad-phone", "invalid-email",
                        MembershipPlan.BASIC, -15.0, 50.0, "NONE");
            } catch (InvalidInputException ex) {
                exceptionConsoleArea.setText("[CAUGHT InvalidInputException]\n"
                        + "Trigger : Submitting invalid name / negative body weight to GymService validator\n"
                        + "Message : " + ex.getMessage());
            } catch (Exception ex) {
                exceptionConsoleArea.setText("[CAUGHT " + ex.getClass().getSimpleName() + "] " + ex.getMessage());
            }
        }
    }

    // ==================== REFRESH & DATA BINDING ====================

    private void refreshAllViews() {
        List<Member> members = gymService.getAllMembers();
        List<Trainer> trainers = gymService.getAllTrainers();
        List<WorkoutLog> workouts = gymService.getAllWorkouts();
        double revenue = gymService.calculateTotalMonthlyRevenue();
        double payroll = gymService.calculateTotalTrainerPayroll();

        // Dashboard KPIs
        kpiMembersVal.setText(String.valueOf(members.size()));
        kpiMembersSub.setText(String.format("Avg Member BMI: %.1f", gymService.getAverageBmi()));
        kpiTrainersVal.setText(String.valueOf(trainers.size()));
        kpiTrainersSub.setText(String.format("Payroll: BDT %,.0f/mo", payroll));
        kpiWorkoutsVal.setText(String.valueOf(workouts.size()));
        kpiWorkoutsSub.setText(String.format("%,d kcal • %,.0f kg lifted",
                gymService.getTotalCaloriesAcrossGym(), gymService.getTotalVolumeAcrossGym()));
        kpiRevenueVal.setText(String.format("BDT %,.0f", revenue));
        kpiRevenueSub.setText(String.format("%d Premium • %d Std • %d Basic",
                gymService.getPlanCount(MembershipPlan.PREMIUM),
                gymService.getPlanCount(MembershipPlan.STANDARD),
                gymService.getPlanCount(MembershipPlan.BASIC)));

        // Recent Workouts on Dashboard
        recentWorkoutsModel.setRowCount(0);
        int start = Math.max(0, workouts.size() - 5);
        for (int i = workouts.size() - 1; i >= start; i--) {
            WorkoutLog log = workouts.get(i);
            String metric = "Strength".equalsIgnoreCase(log.getCategory())
                    ? String.format("%d x %d @ %.1f kg", log.getSets(), log.getReps(), log.getWeightUsedKg())
                    : log.getDurationMinutes() + " min cardio";
            recentWorkoutsModel.addRow(new Object[]{
                    log.getId(), log.getMemberId(), log.getDate(), log.getExerciseName(),
                    log.getCategory(), metric, log.getCaloriesBurned() + " kcal"
            });
        }

        // Trainer Combo
        String prevTrainer = (String) mTrainerCombo.getSelectedItem();
        mTrainerCombo.removeAllItems();
        mTrainerCombo.addItem("NONE - No Personal Trainer");
        for (Trainer t : trainers) {
            mTrainerCombo.addItem(t.getId() + " - " + t.getName() + " (" + t.getSpecialty() + ")");
        }
        if (prevTrainer != null) selectComboByPrefix(mTrainerCombo, extractSelectedId(prevTrainer));

        // Member Combos in Workouts
        String prevWMember = (String) wMemberSelectCombo.getSelectedItem();
        String prevFilter = (String) wFilterMemberCombo.getSelectedItem();
        wMemberSelectCombo.removeAllItems();
        wFilterMemberCombo.removeAllItems();
        wFilterMemberCombo.addItem("ALL - All Members");
        for (Member m : members) {
            String item = m.getId() + " - " + m.getName();
            wMemberSelectCombo.addItem(item);
            wFilterMemberCombo.addItem(item);
        }
        if (prevWMember != null) selectComboByPrefix(wMemberSelectCombo, extractSelectedId(prevWMember));
        if (prevFilter != null) selectComboByPrefix(wFilterMemberCombo, extractSelectedId(prevFilter));

        refreshMemberTable(gymService.searchMembers(memberSearchField.getText()));
        refreshTrainerTable(trainers);
        refreshWorkoutTable();
        refreshPolymorphismView(revenue, payroll);

        if (barChart != null) barChart.repaint();
        if (donutChart != null) donutChart.repaint();
    }

    private void refreshMemberTable(List<Member> members) {
        memberTableModel.setRowCount(0);
        for (Member m : members) {
            String bmiTag = String.format("%.1f (%s)", m.calculateBMI(), m.getBmiCategory());
            memberTableModel.addRow(new Object[]{
                    m.getId(),
                    m.getName(),
                    m.getPlan().getDisplayName(),
                    bmiTag,
                    m.getAssignedTrainerName(),
                    String.format("BDT %,.0f", m.calculateMonthlyAmount())
            });
        }
    }

    private void refreshTrainerTable(List<Trainer> trainers) {
        trainerTableModel.setRowCount(0);
        for (Trainer t : trainers) {
            trainerTableModel.addRow(new Object[]{
                    t.getId(),
                    t.getName(),
                    t.getSpecialty(),
                    t.getExperienceYears() + " yrs",
                    String.format("BDT %,.0f", t.getBaseSalary()),
                    t.getAssignedMemberCount() + " assigned",
                    String.format("BDT %,.0f", t.calculateMonthlyAmount())
            });
        }
    }

    private void refreshWorkoutTable() {
        if (workoutTableModel == null) return;
        String memberId = extractSelectedId((String) wFilterMemberCombo.getSelectedItem());
        List<WorkoutLog> logs = gymService.getWorkoutsForMember(memberId);

        workoutTableModel.setRowCount(0);
        double volume = 0.0;
        int calories = 0;
        for (WorkoutLog log : logs) {
            String detail = "Strength".equalsIgnoreCase(log.getCategory())
                    ? String.format("%d x %d @ %.1f kg", log.getSets(), log.getReps(), log.getWeightUsedKg())
                    : "—";
            workoutTableModel.addRow(new Object[]{
                    log.getId(), log.getMemberId(), log.getDate(), log.getExerciseName(),
                    log.getCategory(), detail, log.getDurationMinutes() + " min", log.getCaloriesBurned() + " kcal"
            });
            volume += log.calculateVolumeKg();
            calories += log.getCaloriesBurned();
        }
        workoutSummaryLabel.setText(String.format("Volume: %,.0f kg  •  Burned: %,d kcal", volume, calories));
    }

    private void refreshPolymorphismView(double revenue, double payroll) {
        labRevenueVal.setText(String.format("BDT %,.0f", revenue));
        labPayrollVal.setText(String.format("BDT %,.0f", payroll));
        double net = revenue - payroll;
        labNetVal.setText(String.format("%sBDT %,.0f", net >= 0 ? "+" : "-", Math.abs(net)));
        labNetVal.setForeground(net >= 0 ? Theme.EMERALD : Theme.ROSE);

        polyTableModel.setRowCount(0);
        for (Person p : gymService.getAllPeoplePolymorphic()) {
            String flow = (p instanceof Member) ? "Bill Inflow" : "Payroll Outflow";
            polyTableModel.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getClass().getSimpleName(),
                    p.getRole(),
                    String.format("BDT %,.0f (%s)", p.calculateMonthlyAmount(), flow),
                    p.getSummary()
            });
        }
    }

    // ==================== PERSISTENCE & VALIDATION HELPERS ====================

    private void autoSaveAndRefresh(String actionMsg) {
        refreshAllViews();
        try {
            gymService.saveToDisk();
            setStatus(actionMsg + " — Auto-saved to data/*.txt", false);
        } catch (DataStorageException ex) {
            setStatus(actionMsg + " — Warning: Save failed (" + ex.getMessage() + ")", true);
        }
    }

    private void saveWithFeedback() {
        try {
            gymService.saveToDisk();
            setStatus("All records saved to " + gymService.getDataDirectoryPath(), false);
            JOptionPane.showMessageDialog(this,
                    "Saved members.txt, trainers.txt, and workouts.txt successfully!",
                    "Files Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (DataStorageException ex) {
            showError("File Save Error", ex.getMessage());
        }
    }

    private double parseDoubleField(String raw, String fieldName) throws InvalidInputException {
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException ex) {
            throw new InvalidInputException("'" + fieldName + "' must be a valid decimal number (entered: '" + raw + "').");
        }
    }

    private int parseIntField(String raw, String fieldName) throws InvalidInputException {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            throw new InvalidInputException("'" + fieldName + "' must be a whole integer (entered: '" + raw + "').");
        }
    }

    private String extractSelectedId(String comboItem) {
        if (comboItem == null || comboItem.trim().isEmpty()) return "NONE";
        int idx = comboItem.indexOf(" - ");
        return idx > 0 ? comboItem.substring(0, idx).trim() : comboItem.trim();
    }

    private void selectComboByPrefix(JComboBox<String> combo, String prefix) {
        if (prefix == null) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            String item = combo.getItemAt(i);
            if (item != null && item.toUpperCase().startsWith(prefix.toUpperCase())) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_LIGHT),
                new EmptyBorder(6, 22, 6, 22)
        ));
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setForeground(Theme.TEXT_SECONDARY);
        bar.add(statusLabel, BorderLayout.CENTER);
        return bar;
    }

    private static void setColumnWidths(JTable table, int... widths) {
        for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? Theme.ROSE : Theme.TEXT_SECONDARY);
    }

    private void showError(String title, String message) {
        setStatus("Error: " + message, true);
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private static DefaultTableModel nonEditableModel(String[] cols) {
        return new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static JScrollPane styledScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER_LIGHT));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private static GridBagConstraints createFormGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 0, 3, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }

    private static void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = row * 2;
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_LABEL);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        panel.add(lbl, gbc);

        gbc.gridy = row * 2 + 1;
        panel.add(field, gbc);
    }

    private static JPanel wrapLabeled(String label, Component field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_LABEL);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }
}
