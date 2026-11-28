package gym;

import javax.swing.SwingUtilities;

// Main entry point for Gym Tracker (CSE110 Term Project).
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GymGUI app = new GymGUI();
            app.setVisible(true);
        });
    }
}
