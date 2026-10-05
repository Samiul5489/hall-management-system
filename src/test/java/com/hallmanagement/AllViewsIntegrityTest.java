package com.hallmanagement;

import com.hallmanagement.dao.UserDAO;
import com.hallmanagement.model.User;
import com.hallmanagement.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class AllViewsIntegrityTest {

    private static final List<String> ALL_FXMLS = Arrays.asList(
        "/com/hallmanagement/view/Login.fxml",
        "/com/hallmanagement/view/Register.fxml",
        "/com/hallmanagement/view/ForgotPassword.fxml",
        "/com/hallmanagement/view/StudentDashboard.fxml",
        "/com/hallmanagement/view/StudentProfile.fxml",
        "/com/hallmanagement/view/HallInformation.fxml",
        "/com/hallmanagement/view/StudentBilling.fxml",
        "/com/hallmanagement/view/NoticeBoard.fxml",
        "/com/hallmanagement/view/StudentHallChange.fxml",
        "/com/hallmanagement/view/StudentRoomChange.fxml",
        "/com/hallmanagement/view/StudentComplaints.fxml",
        "/com/hallmanagement/view/StudentHallLeave.fxml",
        "/com/hallmanagement/view/ProvostDashboard.fxml",
        "/com/hallmanagement/view/ProvostProfile.fxml",
        "/com/hallmanagement/view/ProvostPaymentRequests.fxml",
        "/com/hallmanagement/view/ProvostHallChangeRequests.fxml",
        "/com/hallmanagement/view/ProvostRoomChangeRequests.fxml",
        "/com/hallmanagement/view/ProvostComplaints.fxml",
        "/com/hallmanagement/view/ProvostHallLeaveRequests.fxml"
    );

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("         DEEP INTEGRITY CHECK: 19 FXML VIEWS + ACTIVE SESSION DATA LOADING      ");
        System.out.println("================================================================================");

        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {}

        UserDAO userDAO = new UserDAO();
        userDAO.ensureEmailColumnsAndSingleStudentSeed();
        userDAO.updatePasswordByUserIdAndRole("2403058", "STUDENT", "1111");
        userDAO.updatePasswordByUserIdAndRole("provost001", "PROVOST", "provostpassword");

        User studentUser = userDAO.authenticate("2403058", "1111", "STUDENT");
        User provostUser = userDAO.authenticate("provost001", "provostpassword", "PROVOST");

        if (studentUser == null) throw new RuntimeException("Student test user 2403058 not found!");
        if (provostUser == null) throw new RuntimeException("Provost test user provost001 not found!");

        AtomicInteger passedCount = new AtomicInteger(0);
        AtomicInteger failedCount = new AtomicInteger(0);

        System.out.println("\n[PHASE 1] Testing Student Session (Roll: 2403058)...");
        SessionManager.getInstance().login(studentUser);

        CountDownLatch latch1 = new CountDownLatch(1);
        Platform.runLater(() -> {
            for (String fxmlPath : ALL_FXMLS) {
                try {
                    URL fxmlUrl = AllViewsIntegrityTest.class.getResource(fxmlPath);
                    if (fxmlUrl == null) throw new RuntimeException("FXML not found: " + fxmlPath);
                    FXMLLoader loader = new FXMLLoader(fxmlUrl);
                    loader.load();
                    Object controller = loader.getController();
                    System.out.println("  [STUDENT VIEW OK] " + fxmlPath + " (" + (controller != null ? controller.getClass().getSimpleName() : "None") + ")");
                    passedCount.incrementAndGet();
                } catch (Throwable t) {
                    Throwable root = t;
                    while (root.getCause() != null) root = root.getCause();
                    System.err.println("  [STUDENT VIEW FAIL] " + fxmlPath + " -> " + root.getClass().getSimpleName() + ": " + root.getMessage());
                    root.printStackTrace();
                    failedCount.incrementAndGet();
                }
            }
            latch1.countDown();
        });
        latch1.await();

        System.out.println("\n[PHASE 2] Testing Provost Session (ID: provost001)...");
        SessionManager.getInstance().login(provostUser);

        CountDownLatch latch2 = new CountDownLatch(1);
        Platform.runLater(() -> {
            for (String fxmlPath : ALL_FXMLS) {
                try {
                    URL fxmlUrl = AllViewsIntegrityTest.class.getResource(fxmlPath);
                    if (fxmlUrl == null) throw new RuntimeException("FXML not found: " + fxmlPath);
                    FXMLLoader loader = new FXMLLoader(fxmlUrl);
                    loader.load();
                    Object controller = loader.getController();
                    System.out.println("  [PROVOST VIEW OK] " + fxmlPath + " (" + (controller != null ? controller.getClass().getSimpleName() : "None") + ")");
                    passedCount.incrementAndGet();
                } catch (Throwable t) {
                    Throwable root = t;
                    while (root.getCause() != null) root = root.getCause();
                    System.err.println("  [PROVOST VIEW FAIL] " + fxmlPath + " -> " + root.getClass().getSimpleName() + ": " + root.getMessage());
                    root.printStackTrace();
                    failedCount.incrementAndGet();
                }
            }
            latch2.countDown();
        });
        latch2.await();

        System.out.println("================================================================================");
        System.out.println("SUMMARY: " + passedCount.get() + " successful loads, " + failedCount.get() + " failures.");
        if (failedCount.get() > 0) {
            System.err.println("FAILED: One or more views failed during live session initialization.");
            System.exit(1);
        } else {
            System.out.println("ALL 38 VIEW/ROLE COMBINATIONS VERIFIED 100% WITH ZERO ERRORS!");
            System.exit(0);
        }
    }
}
