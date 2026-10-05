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

public class ProvostDashboardController implements Initializable {

    @FXML private Label  lblLiveClock;
    @FXML private Label  lblWelcome;
    @FXML private Label  lblUserId;
    @FXML private Label  lblRole;
    @FXML private Button btnLogout;
    @FXML private Button btnNavPaymentRequests;
    @FXML private Button btnNavHallChangeRequests;
    @FXML private Button btnNavRoomChangeRequests;
    @FXML private Button btnNavComplaints;
    @FXML private Button btnNavHallLeaveRequests;
    @FXML private Button btnNavNoticeBoard;
    @FXML private Button btnNavProfile;
    @FXML private Button btnNavHallInfo;

    @FXML private Button btnCardPaymentRequests;
    @FXML private Label  lblPaymentRequestsCardDesc;
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
            lblUserId.setText("Provost ID: " + session.getCurrentUserId());
            lblRole.setText("Account Role: Provost / Administration");

            BadgeHelper.updateAllProvostBadges(btnNavPaymentRequests, btnNavHallChangeRequests, btnNavRoomChangeRequests, btnNavComplaints, btnNavHallLeaveRequests, session.getCurrentUserId());

            int pendingPayments = BadgeHelper.updateProvostPaymentBadge(btnNavPaymentRequests, session.getCurrentUserId());
            if (pendingPayments > 0) {
                if (btnCardPaymentRequests != null) {
                    btnCardPaymentRequests.setText("Manage Requests 🔴 (" + pendingPayments + ")");
                }
                if (lblPaymentRequestsCardDesc != null) {
                    lblPaymentRequestsCardDesc.setText("🔴 " + pendingPayments + " pending payment request(s) awaiting approval.");
                }
            }

            int pendingHallChanges = BadgeHelper.updateProvostHallChangeBadge(btnNavHallChangeRequests, session.getCurrentUserId());
            if (pendingHallChanges > 0) {
                if (btnCardHallChange != null) {
                    btnCardHallChange.setText("Review Transfers 🔴 (" + pendingHallChanges + ")");
                }
                if (lblHallChangeCardDesc != null) {
                    lblHallChangeCardDesc.setText("🔴 " + pendingHallChanges + " pending transfer application(s) awaiting review.");
                }
            }

            int pendingRoomChanges = BadgeHelper.updateProvostRoomChangeBadge(btnNavRoomChangeRequests, session.getCurrentUserId());
            if (pendingRoomChanges > 0) {
                if (btnCardRoomChange != null) {
                    btnCardRoomChange.setText("Review Rooms 🔴 (" + pendingRoomChanges + ")");
                }
                if (lblRoomChangeCardDesc != null) {
                    lblRoomChangeCardDesc.setText("🔴 " + pendingRoomChanges + " pending room change request(s) awaiting review.");
                }
            }

            int pendingComplaints = BadgeHelper.updateProvostComplaintBadge(btnNavComplaints, session.getCurrentUserId());
            if (pendingComplaints > 0) {
                if (btnCardComplaints != null) {
                    btnCardComplaints.setText("Review Complaints 🔴 (" + pendingComplaints + ")");
                }
                if (lblComplaintsCardDesc != null) {
                    lblComplaintsCardDesc.setText("🔴 " + pendingComplaints + " pending complaint(s) awaiting response.");
                }
            }

            int pendingLeave = BadgeHelper.updateProvostHallLeaveBadge(btnNavHallLeaveRequests, session.getCurrentUserId());
            if (pendingLeave > 0) {
                if (btnCardHallLeave != null) {
                    btnCardHallLeave.setText("Leave Requests 🔴 (" + pendingLeave + ")");
                }
                if (lblHallLeaveCardDesc != null) {
                    lblHallLeaveCardDesc.setText("🔴 " + pendingLeave + " pending leave request(s) awaiting clearance.");
                }
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
            System.err.println("[ProvostDashboard] Back navigation error: " + e.getMessage());
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
            System.err.println("[ProvostDashboard] Failed to return to login: " + e.getMessage());
            Platform.exit();
        }
    }

    @FXML
    private void onProfileClicked() {
        if (clockTimeline != null) clockTimeline.stop();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_PROFILE_FXML, SceneManager.PROVOST_PROFILE_TITLE);
        } catch (IOException e) {
            System.err.println("[ProvostDashboard] Failed to open Provost Profile: " + e.getMessage());
        }
    }

    @FXML
    private void onHallInformationClicked() {
        if (clockTimeline != null) clockTimeline.stop();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.HALL_INFORMATION_FXML, SceneManager.HALL_INFORMATION_TITLE);
        } catch (IOException e) {
            System.err.println("[ProvostDashboard] Failed to open Hall Information: " + e.getMessage());
        }
    }

    @FXML
    private void onPaymentRequestsClicked() {
        if (clockTimeline != null) clockTimeline.stop();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_PAYMENT_REQUESTS_FXML, SceneManager.PROVOST_PAYMENT_REQUESTS_TITLE);
        } catch (IOException e) {
            System.err.println("[ProvostDashboard] Failed to open Payment Requests: " + e.getMessage());
        }
    }

    @FXML
    private void onHallChangeRequestsClicked() {
        if (clockTimeline != null) clockTimeline.stop();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_HALL_CHANGE_REQUESTS_FXML, SceneManager.PROVOST_HALL_CHANGE_TITLE);
        } catch (IOException e) {
            System.err.println("[ProvostDashboard] Failed to open Hall Change Requests: " + e.getMessage());
        }
    }

    @FXML
    private void onRoomChangeRequestsClicked() {
        if (clockTimeline != null) clockTimeline.stop();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_ROOM_CHANGE_REQUESTS_FXML, SceneManager.PROVOST_ROOM_CHANGE_REQUESTS_TITLE);
        } catch (IOException e) {
            System.err.println("[ProvostDashboard] Failed to open Room Change Requests: " + e.getMessage());
        }
    }

    @FXML
    private void onNoticeBoardClicked() {
        if (clockTimeline != null) clockTimeline.stop();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.NOTICE_BOARD_FXML, SceneManager.NOTICE_BOARD_TITLE);
        } catch (IOException e) {
            System.err.println("[ProvostDashboard] Failed to open Notice Board: " + e.getMessage());
        }
    }

    @FXML
    private void onComplaintsClicked() {
        if (clockTimeline != null) clockTimeline.stop();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_COMPLAINTS_FXML, SceneManager.PROVOST_COMPLAINTS_TITLE);
        } catch (IOException e) {
            System.err.println("[ProvostDashboard] Failed to open Complaints: " + e.getMessage());
        }
    }

    @FXML
    private void onHallLeaveRequestsClicked() {
        if (clockTimeline != null) clockTimeline.stop();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_HALL_LEAVE_REQUESTS_FXML, SceneManager.PROVOST_HALL_LEAVE_REQUESTS_TITLE);
        } catch (IOException e) {
            System.err.println("[ProvostDashboard] Failed to open Hall Leave Requests: " + e.getMessage());
        }
    }
}
