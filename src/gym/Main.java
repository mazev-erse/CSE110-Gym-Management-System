package gym;

import gym.exception.DataStorageException;
import gym.service.FileStorageService;
import gym.service.GymService;
import gym.ui.GymManagementFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.File;

/**
 * Application entry point for Gym Tracker — Workout & Club Management System.
 * Course: CSE110 (Object Oriented Programming) — East West University.
 */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            String dataDir = new File("data").getPath();
            FileStorageService storageService = new FileStorageService(dataDir);
            GymService gymService = new GymService(storageService);

            try {
                gymService.initialize();
            } catch (DataStorageException ex) {
                JOptionPane.showMessageDialog(null,
                        "Storage Warning: " + ex.getMessage(),
                        "Startup File Warning",
                        JOptionPane.WARNING_MESSAGE);
            }

            GymManagementFrame frame = new GymManagementFrame(gymService);
            frame.setVisible(true);
        });
    }
}
