package com.hallmanagement.controller;

import com.hallmanagement.util.BadgeHelper;
import com.hallmanagement.util.SceneManager;
import com.hallmanagement.util.SessionManager;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class StudentDashboardController implements Initializable {

    @FXML private Label  lblLiveClock;
    @FXML private Label  lblWelcome;
    @FXML private Label  lblUserId;
    @FXML private Label  lblRole;
    @FXML private Button btnLogout;
    @FXML private Button btnNavNoticeBoard;
    @FXML private Button btnNavBilling;
    @FXML private Button btnNavHallChange;
    @FXML private Button btnNavRoomChange;
    @FXML private Button btnNavComplaints;
    @FXML private Button btnNavHallLeave;

    @FXML private Button btnCardNotice;
    @FXML private Label  lblNoticeCardDesc;
    @FXML private Button btnCardBilling;
    @FXML private Label  lblBillingCardDesc;
    @FXML private Button btnCardHallChange;
    @FXML private Label  lblHallChangeCardDesc;
    @FXML private Button btnCardRoomChange;
    @FXML private Label  lblRoomChangeCardDesc;
    @FXML private Button btnCardComplaints;
    @FXML private Label  lblComplaintsCardDesc;
    @FXML private Button btnCardHallLeave;
    @FXML private Label  lblHallLeaveCardDesc;

    private Timeline clockTimeline;
    private static final DateTimeFormatter CLOCK_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy  ·  hh:mm:ss a");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SessionManager session = SessionManager.getInstance();

        initLiveClock();

        if (session.isLoggedIn()) {
            lblWelcome.setText("Welcome, " + session.getCurrentUserName() + "!");
            lblUserId.setText("Student ID: " + session.getCurrentUserId());
            lblRole.setText("Account Role: Student");

            BadgeHelper.updateAllStudentBadges(btnNavNoticeBoard, btnNavBilling, btnNavHallChange, btnNavRoomChange, btnNavComplaints, btnNavHallLeave, session.getCurrentUserId());

            int unread = BadgeHelper.updateStudentNoticeBadge(btnNavNoticeBoard, session.getCurrentUserId());
            if (unread > 0) {
                if (btnCardNotice != null) btnCardNotice.setText("View Notices 🔴 (" + unread + ")");
                if (lblNoticeCardDesc != null) lblNoticeCardDesc.setText("🔴 " + unread + " unread circular(s) posted by Hall Authority.");
            }

            int billingCount = BadgeHelper.updateStudentBillingBadge(btnNavBilling, session.getCurrentUserId());
            if (billingCount > 0) {
                if (btnCardBilling != null) btnCardBilling.setText("Manage Bills 🔴 (" + billingCount + ")");
                if (lblBillingCardDesc != null) lblBillingCardDesc.setText("🔴 " + billingCount + " pending request(s) or unpaid dues.");
            }

            int hallChangeCount = BadgeHelper.updateStudentHallChangeBadge(btnNavHallChange, session.getCurrentUserId());
            if (hallChangeCount > 0) {
                if (btnCardHallChange != null) btnCardHallChange.setText("Application Status 🔴 (" + hallChangeCount + ")");
                if (lblHallChangeCardDesc != null) lblHallChangeCardDesc.setText("🔴 Pending hall transfer application awaiting review.");
            }

            int roomChangeCount = BadgeHelper.updateStudentRoomChangeBadge(btnNavRoomChange, session.getCurrentUserId());
            if (roomChangeCount > 0) {
                if (btnCardRoomChange != null) btnCardRoomChange.setText("Change Status 🔴 (" + roomChangeCount + ")");
                if (lblRoomChangeCardDesc != null) lblRoomChangeCardDesc.setText("🔴 " + roomChangeCount + " processed room change update(s) ready.");
            }

            int complaintsCount = BadgeHelper.updateStudentComplaintBadge(btnNavComplaints, session.getCurrentUserId());
            if (complaintsCount > 0) {
                if (btnCardComplaints != null) btnCardComplaints.setText("My Complaints 🔴 (" + complaintsCount + ")");
                if (lblComplaintsCardDesc != null) lblComplaintsCardDesc.setText("🔴 " + complaintsCount + " active complaint(s) on file.");
            }

            int leaveCount = BadgeHelper.updateStudentHallLeaveBadge(btnNavHallLeave, session.getCurrentUserId());
            if (leaveCount > 0) {
                if (btnCardHallLeave != null) btnCardHallLeave.setText("Leave Status 🔴 (" + leaveCount + ")");
                if (lblHallLeaveCardDesc != null) lblHallLeaveCardDesc.setText("🔴 " + leaveCount + " processed leave update(s) ready.");
            }
        }
    }

    private void initLiveClock() {
        if (lblLiveClock != null) {
            lblLiveClock.setText(LocalDateTime.now().format(CLOCK_FORMATTER));
            clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                lblLiveClock.setText(LocalDateTime.now().format(CLOCK_FORMATTER));
            }));
            clockTimeline.setCycleCount(Animation.INDEFINITE);
            clockTimeline.play();
        }
    }

    @FXML
    private void onBackClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.goBack(stage);
        } catch (Exception e) {
            System.err.println("[StudentDashboard] Back navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void onLogoutClicked() {
        if (clockTimeline != null) clockTimeline.stop();
        SessionManager.getInstance().logout();

        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.LOGIN_FXML, SceneManager.APP_TITLE);
        } catch (IOException e) {
            System.err.println("[StudentDashboard] Failed to return to login: " + e.getMessage());
            Platform.exit();
        }
    }

    @FXML
    private void onMyProfileClicked() {
        if (clockTimeline != null) clockTimeline.stop();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_PROFILE_FXML, SceneManager.STUDENT_PROFILE_TITLE);
        } catch (IOException e) {
            System.err.println("[StudentDashboard] Failed to open Profile: " + e.getMessage());
        }
    }

    @FXML
    private void onHallInformationClicked() {
        if (clockTimeline != null) clockTimeline.stop();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.HALL_INFORMATION_FXML, SceneManager.HALL_INFORMATION_TITLE);
        } catch (IOException e) {
            System.err.println("[StudentDashboard] Failed to open Hall Information: " + e.getMessage());
        }
    }

    @FXML
    private void onBillingClicked() {
        if (clockTimeline != null) clockTimeline.stop();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_BILLING_FXML, SceneManager.STUDENT_BILLING_TITLE);
        } catch (IOException e) {
            System.err.println("[StudentDashboard] Failed to open Bill & Due: " + e.getMessage());
        }
    }

    @FXML
    private void onNoticeBoardClicked() {
        if (clockTimeline != null) clockTimeline.stop();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.NOTICE_BOARD_FXML, SceneManager.NOTICE_BOARD_TITLE);
        } catch (IOException e) {
            System.err.println("[StudentDashboard] Failed to open Notice Board: " + e.getMessage());
        }
    }

    @FXML
    private void onHallChangeClicked() {
        if (clockTimeline != null) clockTimeline.stop();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_HALL_CHANGE_FXML, SceneManager.STUDENT_HALL_CHANGE_TITLE);
        } catch (IOException e) {
            System.err.println("[StudentDashboard] Failed to open Hall Change: " + e.getMessage());
        }
    }

    @FXML
    private void onRoomChangeClicked() {
        if (clockTimeline != null) clockTimeline.stop();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_ROOM_CHANGE_FXML, SceneManager.STUDENT_ROOM_CHANGE_TITLE);
        } catch (IOException e) {
            System.err.println("[StudentDashboard] Failed to open Room Change: " + e.getMessage());
        }
    }

    @FXML
    private void onComplaintsClicked() {
        if (clockTimeline != null) clockTimeline.stop();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_COMPLAINTS_FXML, SceneManager.STUDENT_COMPLAINTS_TITLE);
        } catch (IOException e) {
            System.err.println("[StudentDashboard] Failed to open Complaints: " + e.getMessage());
        }
    }

    @FXML
    private void onHallLeaveClicked() {
        if (clockTimeline != null) clockTimeline.stop();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_HALL_LEAVE_FXML, SceneManager.STUDENT_HALL_LEAVE_TITLE);
        } catch (IOException e) {
            System.err.println("[StudentDashboard] Failed to open Hall Leave: " + e.getMessage());
        }
    }
}
