package com.hallmanagement;

import com.hallmanagement.dao.StudentDAO;
import com.hallmanagement.dao.UserDAO;
import com.hallmanagement.model.Student;
import com.hallmanagement.model.User;
import com.hallmanagement.util.SessionManager;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;

import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

public class ForgotPasswordSmokeTest {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("       STARTING FORGOT PASSWORD & STUDENT EMAIL SMOKE TEST SUITE               ");
        System.out.println("================================================================================");

        int passed = 0;
        int failed = 0;

        UserDAO userDAO = new UserDAO();
        StudentDAO studentDAO = new StudentDAO();

        try {
            userDAO.ensureEmailColumnsAndSingleStudentSeed();
            userDAO.updatePasswordByUserIdAndRole("2403058", "STUDENT", "1111");
            System.out.println("[TEST 1] Schema Migration & Seed Validation ... PASSED ✓");
            passed++;
        } catch (Exception e) {
            System.err.println("[TEST 1] FAILED: " + e.getMessage());
            failed++;
        }

        try {
            User user = userDAO.authenticate("2403058", "1111", "STUDENT");
            if (user != null && "Shamiul Islam".equals(user.getName()) && "01921602024".equals(user.getPhone())) {
                System.out.println("[TEST 2] Shamiul Islam (2403058, Pass: 1111) Authentication ... PASSED ✓");
                passed++;
            } else {
                throw new Exception("Unexpected student details: " + user);
            }
        } catch (Exception e) {
            System.err.println("[TEST 2] FAILED: " + e.getMessage());
            failed++;
        }

        try {
            String email = userDAO.findEmailByUserIdAndRole("2403058", "STUDENT");
            if ("shamiulislam39999@gmail.com".equalsIgnoreCase(email)) {
                System.out.println("[TEST 3] Student Email Lookup (shamiulislam39999@gmail.com) ... PASSED ✓");
                passed++;
            } else {
                throw new Exception("Expected shamiulislam39999@gmail.com but got: " + email);
            }
        } catch (Exception e) {
            System.err.println("[TEST 3] FAILED: " + e.getMessage());
            failed++;
        }

        try {
            String email = userDAO.findEmailByUserIdAndRole("provost001", "PROVOST");
            if ("saniulsami@gmail.com".equalsIgnoreCase(email)) {
                System.out.println("[TEST 4] Provost Email Lookup (saniulsami@gmail.com) ... PASSED ✓");
                passed++;
            } else {
                throw new Exception("Expected saniulsami@gmail.com but got: " + email);
            }
        } catch (Exception e) {
            System.err.println("[TEST 4] FAILED: " + e.getMessage());
            failed++;
        }

        try {

            boolean updated = userDAO.updatePasswordByUserIdAndRole("2403058", "STUDENT", "2222");
            if (!updated) throw new Exception("Failed to update password to 2222");

            User u2 = userDAO.authenticate("2403058", "2222", "STUDENT");
            if (u2 == null) throw new Exception("Login with new password 2222 failed!");

            userDAO.updatePasswordByUserIdAndRole("2403058", "STUDENT", "1111");
            User uOriginal = userDAO.authenticate("2403058", "1111", "STUDENT");
            if (uOriginal == null) throw new Exception("Login with restored password 1111 failed!");

            System.out.println("[TEST 5] Student Password Reset Lifecycle (OTP DB Update) ... PASSED ✓");
            passed++;
        } catch (Exception e) {
            System.err.println("[TEST 5] FAILED: " + e.getMessage());
            failed++;
        }

        try {
            Student s = studentDAO.getStudentByUserId("2403058");
            if (s == null || !"shamiulislam39999@gmail.com".equals(s.getEmail())) {
                throw new Exception("Expected student email shamiulislam39999@gmail.com, got: " + (s != null ? s.getEmail() : "null"));
            }

            studentDAO.updateStudentEmail("2403058", "shamiul.updated@gmail.com");
            Student sUpdated = studentDAO.getStudentByUserId("2403058");
            if (!"shamiul.updated@gmail.com".equals(sUpdated.getEmail())) {
                throw new Exception("Updated email mismatch: " + sUpdated.getEmail());
            }

            studentDAO.updateStudentEmail("2403058", "shamiulislam39999@gmail.com");
            System.out.println("[TEST 6] Student Profile Email Loading & Live Mutation ... PASSED ✓");
            passed++;
        } catch (Exception e) {
            System.err.println("[TEST 6] FAILED: " + e.getMessage());
            failed++;
        }

        try {
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] fxmlSuccess = {false};
            final String[] errorHolder = {null};

            try {
                Platform.startup(() -> {
                    try {
                        SessionManager.getInstance().login(new User(1, "2403058", "Shamiul Islam", "01921602024", "STUDENT", "ACTIVE"));

                        new FXMLLoader(ForgotPasswordSmokeTest.class.getResource("/com/hallmanagement/view/ForgotPassword.fxml")).load();
                        new FXMLLoader(ForgotPasswordSmokeTest.class.getResource("/com/hallmanagement/view/Login.fxml")).load();
                        new FXMLLoader(ForgotPasswordSmokeTest.class.getResource("/com/hallmanagement/view/Register.fxml")).load();
                        new FXMLLoader(ForgotPasswordSmokeTest.class.getResource("/com/hallmanagement/view/StudentProfile.fxml")).load();

                        fxmlSuccess[0] = true;
                    } catch (Throwable t) {
                        errorHolder[0] = t.getMessage();
                        t.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                });
            } catch (IllegalStateException e) {
                Platform.runLater(() -> {
                    try {
                        SessionManager.getInstance().login(new User(1, "2403058", "Shamiul Islam", "01921602024", "STUDENT", "ACTIVE"));

                        new FXMLLoader(ForgotPasswordSmokeTest.class.getResource("/com/hallmanagement/view/ForgotPassword.fxml")).load();
                        new FXMLLoader(ForgotPasswordSmokeTest.class.getResource("/com/hallmanagement/view/Login.fxml")).load();
                        new FXMLLoader(ForgotPasswordSmokeTest.class.getResource("/com/hallmanagement/view/Register.fxml")).load();
                        new FXMLLoader(ForgotPasswordSmokeTest.class.getResource("/com/hallmanagement/view/StudentProfile.fxml")).load();

                        fxmlSuccess[0] = true;
                    } catch (Throwable t) {
                        errorHolder[0] = t.getMessage();
                        t.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            if (fxmlSuccess[0]) {
                System.out.println("[TEST 7] FXML Load Test (ForgotPassword, Login, Register, StudentProfile) ... PASSED ✓");
                passed++;
            } else {
                throw new Exception("FXML Load Failed: " + errorHolder[0]);
            }
        } catch (Exception e) {
            System.err.println("[TEST 7] FAILED: " + e.getMessage());
            failed++;
        }

        try {
            java.util.concurrent.CompletableFuture<Boolean> future = com.hallmanagement.util.EmailService.sendOtpEmailAsync("shamiulislam39999@gmail.com", "Shamiul Islam", "584920");
            boolean sent = future.get(25, java.util.concurrent.TimeUnit.SECONDS);
            if (sent) {
                System.out.println("[TEST 8] EmailService Real Async OTP Dispatch ... PASSED ✓");
                passed++;
            } else {
                throw new Exception("EmailService returned false");
            }
        } catch (Exception e) {
            System.err.println("[TEST 8] FAILED: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        System.out.println("================================================================================");
        System.out.println("  SUMMARY: " + passed + " / " + (passed + failed) + " TESTS PASSED");
        System.out.println("================================================================================");

        if (failed > 0) {
            System.exit(1);
        } else {
            System.exit(0);
        }
    }
}
