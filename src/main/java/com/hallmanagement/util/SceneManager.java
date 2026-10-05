package com.hallmanagement.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;

public final class SceneManager {

    public static final double MIN_WIDTH  = 960;
    public static final double MIN_HEIGHT = 640;

    public static class NavRecord {
        public final String fxmlPath;
        public final String title;

        public NavRecord(String fxmlPath, String title) {
            this.fxmlPath = fxmlPath;
            this.title = title;
        }
    }

    private static final Deque<NavRecord> historyStack = new ArrayDeque<>();
    private static NavRecord currentRecord = null;
    private static boolean isNavigatingBack = false;

    private SceneManager() {}

    public static void switchTo(Stage stage, String fxmlPath, String title) throws IOException {
        URL fxmlUrl = SceneManager.class.getResource(fxmlPath);
        if (fxmlUrl == null) {
            throw new IOException("FXML resource not found on classpath: " + fxmlPath);
        }

        if (!isNavigatingBack) {
            if (LOGIN_FXML.equals(fxmlPath)) {
                historyStack.clear();
            } else if (currentRecord != null && !currentRecord.fxmlPath.equals(fxmlPath)) {

                if (historyStack.isEmpty() || !historyStack.peek().fxmlPath.equals(currentRecord.fxmlPath)) {
                    historyStack.push(currentRecord);
                }
            }
        }
        currentRecord = new NavRecord(fxmlPath, title);

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        if (stage.getScene() != null) {
            stage.getScene().setRoot(root);
        } else {
            Scene scene = new Scene(root, MIN_WIDTH, MIN_HEIGHT);
            stage.setScene(scene);
            stage.setMinWidth(MIN_WIDTH);
            stage.setMinHeight(MIN_HEIGHT);
            stage.centerOnScreen();
        }

        stage.setTitle(title);
        stage.show();
    }

    public static <T> T switchToAndGetController(Stage stage, String fxmlPath, String title)
            throws IOException {

        URL fxmlUrl = SceneManager.class.getResource(fxmlPath);
        if (fxmlUrl == null) {
            throw new IOException("FXML resource not found on classpath: " + fxmlPath);
        }

        if (!isNavigatingBack) {
            if (LOGIN_FXML.equals(fxmlPath)) {
                historyStack.clear();
            } else if (currentRecord != null && !currentRecord.fxmlPath.equals(fxmlPath)) {
                if (historyStack.isEmpty() || !historyStack.peek().fxmlPath.equals(currentRecord.fxmlPath)) {
                    historyStack.push(currentRecord);
                }
            }
        }
        currentRecord = new NavRecord(fxmlPath, title);

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();
        T controller = loader.getController();

        if (stage.getScene() != null) {
            stage.getScene().setRoot(root);
        } else {
            Scene scene = new Scene(root, MIN_WIDTH, MIN_HEIGHT);
            stage.setScene(scene);
            stage.setMinWidth(MIN_WIDTH);
            stage.setMinHeight(MIN_HEIGHT);
            stage.centerOnScreen();
        }

        stage.setTitle(title);
        stage.show();

        return controller;
    }

    public static void goBack(Stage stage) {
        try {
            isNavigatingBack = true;
            if (!historyStack.isEmpty()) {
                NavRecord prev = historyStack.pop();

                while (prev != null && currentRecord != null && prev.fxmlPath.equals(currentRecord.fxmlPath) && !historyStack.isEmpty()) {
                    prev = historyStack.pop();
                }
                if (prev != null && (currentRecord == null || !prev.fxmlPath.equals(currentRecord.fxmlPath))) {
                    switchTo(stage, prev.fxmlPath, prev.title);
                    return;
                }
            }

            if (SessionManager.getInstance().isLoggedIn()) {
                if (SessionManager.getInstance().isStudent()) {
                    switchTo(stage, STUDENT_DASHBOARD_FXML, STUDENT_TITLE);
                } else {
                    switchTo(stage, PROVOST_DASHBOARD_FXML, PROVOST_TITLE);
                }
            } else {
                switchTo(stage, LOGIN_FXML, APP_TITLE);
            }
        } catch (IOException e) {
            System.err.println("[SceneManager] Error during goBack: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isNavigatingBack = false;
        }
    }

    public static final String LOGIN_FXML                     = "/com/hallmanagement/view/Login.fxml";
    public static final String REGISTER_FXML                  = "/com/hallmanagement/view/Register.fxml";
    public static final String FORGOT_PASSWORD_FXML           = "/com/hallmanagement/view/ForgotPassword.fxml";
    public static final String STUDENT_DASHBOARD_FXML         = "/com/hallmanagement/view/StudentDashboard.fxml";
    public static final String STUDENT_PROFILE_FXML           = "/com/hallmanagement/view/StudentProfile.fxml";
    public static final String STUDENT_BILLING_FXML           = "/com/hallmanagement/view/StudentBilling.fxml";
    public static final String STUDENT_HALL_CHANGE_FXML       = "/com/hallmanagement/view/StudentHallChange.fxml";
    public static final String STUDENT_ROOM_CHANGE_FXML       = "/com/hallmanagement/view/StudentRoomChange.fxml";
    public static final String STUDENT_COMPLAINTS_FXML       = "/com/hallmanagement/view/StudentComplaints.fxml";
    public static final String STUDENT_HALL_LEAVE_FXML        = "/com/hallmanagement/view/StudentHallLeave.fxml";
    public static final String PROVOST_DASHBOARD_FXML         = "/com/hallmanagement/view/ProvostDashboard.fxml";
    public static final String PROVOST_PROFILE_FXML           = "/com/hallmanagement/view/ProvostProfile.fxml";
    public static final String PROVOST_PAYMENT_REQUESTS_FXML  = "/com/hallmanagement/view/ProvostPaymentRequests.fxml";
    public static final String PROVOST_HALL_CHANGE_REQUESTS_FXML = "/com/hallmanagement/view/ProvostHallChangeRequests.fxml";
    public static final String PROVOST_ROOM_CHANGE_REQUESTS_FXML = "/com/hallmanagement/view/ProvostRoomChangeRequests.fxml";
    public static final String PROVOST_COMPLAINTS_FXML        = "/com/hallmanagement/view/ProvostComplaints.fxml";
    public static final String PROVOST_HALL_LEAVE_REQUESTS_FXML  = "/com/hallmanagement/view/ProvostHallLeaveRequests.fxml";
    public static final String HALL_INFORMATION_FXML          = "/com/hallmanagement/view/HallInformation.fxml";
    public static final String NOTICE_BOARD_FXML               = "/com/hallmanagement/view/NoticeBoard.fxml";

    public static final String APP_TITLE                      = "University Hall Management System";
    public static final String FORGOT_PASSWORD_TITLE          = "Password Recovery — University Hall Management";
    public static final String STUDENT_TITLE                  = "Student Portal — Hall Management";
    public static final String STUDENT_PROFILE_TITLE          = "My Profile — Hall Management";
    public static final String STUDENT_BILLING_TITLE          = "Bill & Due — Hall Management";
    public static final String STUDENT_HALL_CHANGE_TITLE      = "Apply for New Hall — Hall Management";
    public static final String STUDENT_ROOM_CHANGE_TITLE      = "Room/Seat Change — Hall Management";
    public static final String STUDENT_COMPLAINTS_TITLE       = "Complaints & Grievances — Hall Management";
    public static final String STUDENT_HALL_LEAVE_TITLE       = "Hall Leave & Clearance — Hall Management";
    public static final String PROVOST_TITLE                  = "Provost Portal — Hall Management";
    public static final String PROVOST_PROFILE_TITLE          = "Provost Profile — Hall Management";
    public static final String PROVOST_PAYMENT_REQUESTS_TITLE = "Payment Requests & History — Hall Management";
    public static final String PROVOST_HALL_CHANGE_TITLE      = "Hall Change Requests — Hall Management";
    public static final String PROVOST_ROOM_CHANGE_REQUESTS_TITLE = "Room/Seat Change Requests — Hall Management";
    public static final String PROVOST_COMPLAINTS_TITLE       = "Student Complaints & Grievances — Hall Management";
    public static final String PROVOST_HALL_LEAVE_REQUESTS_TITLE = "Student Hall Leave Requests — Hall Management";
    public static final String HALL_INFORMATION_TITLE         = "Hall Information & Live Seat Availability — Hall Management";
    public static final String NOTICE_BOARD_TITLE             = "Notice Board — Hall Management";
}
