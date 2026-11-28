package gym;

import gym.exception.DataStorageException;
import gym.exception.DuplicateEntityException;
import gym.exception.EntityNotFoundException;
import gym.exception.InvalidInputException;
import gym.model.Member;
import gym.model.MembershipPlan;
import gym.model.Person;
import gym.model.Trainer;
import gym.service.FileStorageService;
import gym.service.GymService;
import gym.ui.GymManagementFrame;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Automated verification suite testing OOP principles, true object associations,
 * File I/O persistence, Receipt Export, Exception Handling, and rendering live GUI previews.
 */
public class GymSystemSelfTest {
    public static void main(String[] args) throws Exception {
        System.out.println("=== CSE110 Gym Tracker Verification Suite ===");

        // Start cleanly from seeded sample data to ensure latest schema
        File dataDir = new File("data");
        if (dataDir.exists()) {
            File[] oldFiles = dataDir.listFiles();
            if (oldFiles != null) {
                for (File f : oldFiles) {
                    f.delete();
                }
            }
        }

        FileStorageService storage = new FileStorageService(dataDir.getPath());
        GymService service = new GymService(storage);
        service.initialize();

        assertCondition(service.getAllTrainers().size() == 3, "3 seeded trainers loaded");
        assertCondition(service.getAllMembers().size() == 6, "6 seeded members loaded");
        assertCondition(service.getAllWorkouts().size() == 8, "8 seeded workouts loaded");

        // 1. Verify True Object Association (Member <-> Trainer)
        Member arif = service.findMember("M001");
        Trainer tanvir = service.findTrainer("T001");
        assertCondition(arif.getAssignedTrainer() == tanvir, "Member holds direct Trainer object reference (Aggregation)");
        assertCondition(tanvir.getAssignedMembers().contains(arif), "Trainer holds assigned Member reference");
        assertCondition(arif.calculateBMI() > 20.0 && arif.calculateBMI() < 26.0,
                "BMI calculation accurate: " + String.format("%.1f (%s)", arif.calculateBMI(), arif.getBmiCategory()));

        // 2. Verify Runtime Polymorphism
        List<Person> people = service.getAllPeoplePolymorphic();
        assertCondition(people.size() == 9, "Polymorphic List<Person> combines 6 Members + 3 Trainers");
        for (Person p : people) {
            assertCondition(p.calculateMonthlyAmount() > 0, "Polymorphic calculateMonthlyAmount() > 0 for " + p.getId());
        }

        // 3. Verify 4 Custom Exceptions
        boolean caughtDup = false;
        try {
            service.addMember("M001", "Dup", "01711111111", "dup@ewubd.edu", MembershipPlan.BASIC, 70, 170, "NONE");
        } catch (DuplicateEntityException ex) {
            caughtDup = true;
            System.out.println("[PASS] DuplicateEntityException: " + ex.getMessage());
        }
        assertCondition(caughtDup, "DuplicateEntityException caught");

        boolean caughtInvalid = false;
        try {
            service.addMember("M999", "A", "invalid", "bad", MembershipPlan.BASIC, -5, 50, "NONE");
        } catch (InvalidInputException ex) {
            caughtInvalid = true;
            System.out.println("[PASS] InvalidInputException: " + ex.getMessage());
        }
        assertCondition(caughtInvalid, "InvalidInputException caught");

        boolean caughtNotFound = false;
        try {
            service.findMember("M404_MISSING");
        } catch (EntityNotFoundException ex) {
            caughtNotFound = true;
            System.out.println("[PASS] EntityNotFoundException: " + ex.getMessage());
        }
        assertCondition(caughtNotFound, "EntityNotFoundException caught");

        // 4. Verify Receipt File Export & Persistence Round-Trip
        File receipt = service.exportMemberReceipt("M001");
        assertCondition(receipt.exists() && receipt.length() > 100, "Exported member receipt to " + receipt.getPath());

        GymService reloaded = new GymService(new FileStorageService(dataDir.getPath()));
        reloaded.reloadFromDisk();
        assertCondition(reloaded.getAllMembers().size() == 6, "File round-trip preserved all 6 members");
        assertCondition(reloaded.findMember("M001").getAssignedTrainer() != null,
                "File round-trip restored Member -> Trainer object reference");

        // 5. Render live Swing GUI screenshots into docs/screenshots/ if requested
        if (args.length > 0 && "--screenshots".equalsIgnoreCase(args[0])) {
            renderGuiScreenshots(service);
        }

        System.out.println("=== ALL VERIFICATION CHECKS PASSED ===");
    }

    private static void renderGuiScreenshots(GymService service) throws Exception {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        File outDir = new File("docs/screenshots");
        outDir.mkdirs();

        String[][] pages = {
                {GymManagementFrame.PAGE_DASHBOARD, "01_dashboard.png"},
                {GymManagementFrame.PAGE_MEMBERS, "02_members.png"},
                {GymManagementFrame.PAGE_WORKOUTS, "03_workouts.png"},
                {GymManagementFrame.PAGE_OOP_LAB, "04_oop_lab.png"}
        };

        SwingUtilities.invokeAndWait(() -> {
            GymManagementFrame frame = new GymManagementFrame(service);
            frame.setSize(1280, 790);
            frame.addNotify();
            frame.validate();

            for (String[] pair : pages) {
                frame.navigateTo(pair[0]);
                frame.getContentPane().setSize(1260, 760);
                frame.getContentPane().doLayout();
                frame.validate();

                int w = frame.getContentPane().getWidth();
                int h = frame.getContentPane().getHeight();
                BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = img.createGraphics();
                frame.getContentPane().paint(g2);
                g2.dispose();

                try {
                    File dest = new File(outDir, pair[1]);
                    ImageIO.write(img, "png", dest);
                    System.out.println("[SCREENSHOT] Rendered " + dest.getPath());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
            frame.dispose();
        });
    }

    private static void assertCondition(boolean condition, String message) throws DataStorageException {
        if (!condition) {
            throw new DataStorageException("Assertion failed: " + message);
        }
        System.out.println("[PASS] " + message);
    }
}
