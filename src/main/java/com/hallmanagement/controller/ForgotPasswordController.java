package com.hallmanagement.controller;

import com.hallmanagement.dao.UserDAO;
import com.hallmanagement.util.EmailService;
import com.hallmanagement.util.SceneManager;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ForgotPasswordController implements Initializable {

    private final UserDAO userDAO = new UserDAO();
    private final SecureRandom random = new SecureRandom();

    @FXML private HBox stepPill1;
    @FXML private HBox stepPill2;
    @FXML private HBox stepPill3;
    @FXML private Label stepBadge1;
    @FXML private Label stepBadge2;
    @FXML private Label stepBadge3;
    @FXML private Label stepText1;
    @FXML private Label stepText2;
    @FXML private Label stepText3;

    @FXML private VBox boxStep1;
    @FXML private ComboBox<String> cmbRole;
    @FXML private TextField txtUserId;
    @FXML private Label lblStatusStep1;
    @FXML private Button btnSendOtp;

    @FXML private VBox boxStep2;
    @FXML private Label lblEmailNotice;
    @FXML private TextField txtOtp;
    @FXML private Label lblTimer;
    @FXML private Hyperlink linkResendOtp;
    @FXML private Label lblStatusStep2;
    @FXML private Button btnVerifyOtp;

    @FXML private VBox boxStep3;
    @FXML private Label lblVerifiedAccountInfo;
    @FXML private PasswordField txtNewPassword;
    @FXML private TextField txtNewPasswordPlain;
    @FXML private Button btnToggleNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField txtConfirmPasswordPlain;
    @FXML private Button btnToggleConfirmPassword;
    @FXML private Label lblStatusStep3;
    @FXML private Button btnResetPassword;

    @FXML private VBox boxSuccess;
    @FXML private Button btnLoginDirect;

    private String currentUserId;
    private String currentRole;
    private String registeredEmail;
    private String activeOtp;
    private int secondsRemaining = 180;
    private Timeline countdownTimeline;
    private boolean isPasswordsVisible = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbRole.setItems(FXCollections.observableArrayList("Student", "Provost / Admin"));
        cmbRole.getSelectionModel().selectFirst();

        txtNewPasswordPlain.textProperty().bindBidirectional(txtNewPassword.textProperty());
        txtConfirmPasswordPlain.textProperty().bindBidirectional(txtConfirmPassword.textProperty());
    }

    @FXML
    private void onSendOtpClicked() {
        lblStatusStep1.setText("");
        String userId = txtUserId.getText().trim();
        String roleDisplay = cmbRole.getValue();

        if (userId.isEmpty()) {
            showStatus1("Please enter your Student Roll / Provost ID.", true);
            return;
        }

        String role = "Student".equalsIgnoreCase(roleDisplay) ? "STUDENT" : "PROVOST";

        try {
            String email = userDAO.findEmailByUserIdAndRole(userId, role);
            if (email == null || email.trim().isEmpty()) {
                showStatus1("No account found or no registered email for ID: " + userId + " (" + roleDisplay + ")", true);
                return;
            }

            this.currentUserId = userId;
            this.currentRole = role;
            this.registeredEmail = email.trim();

            btnSendOtp.setDisable(true);
            btnSendOtp.setText("SENDING EMAIL...");

            generateAndDispatchOtp();

            updateStepper(2);
            boxStep1.setVisible(false);
            boxStep1.setManaged(false);

            boxStep2.setVisible(true);
            boxStep2.setManaged(true);
            txtOtp.clear();
            txtOtp.requestFocus();

        } catch (SQLException e) {
            e.printStackTrace();
            showStatus1("Database error checking account: " + e.getMessage(), true);
        } finally {
            btnSendOtp.setDisable(false);
            btnSendOtp.setText("SEND VERIFICATION CODE TO EMAIL");
        }
    }

    private void generateAndDispatchOtp() {

        int code = 100000 + random.nextInt(900000);
        this.activeOtp = String.valueOf(code);

        String masked = maskEmail(registeredEmail);
        lblEmailNotice.setText("Verification code sent to registered email: " + masked);

        EmailService.sendOtpEmailAsync(registeredEmail, currentUserId, activeOtp);

        startTimer();
    }

    private void startTimer() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        secondsRemaining = 180;
        linkResendOtp.setDisable(true);
        updateTimerDisplay();

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), (ActionEvent event) -> {
            secondsRemaining--;
            if (secondsRemaining > 0) {
                updateTimerDisplay();
            } else {
                lblTimer.setText("Code has expired. Please click 'Resend Code'.");
                linkResendOtp.setDisable(false);
                countdownTimeline.stop();
            }
        }));
        countdownTimeline.setCycleCount(180);
        countdownTimeline.play();
    }

    private void updateTimerDisplay() {
        int mins = secondsRemaining / 60;
        int secs = secondsRemaining % 60;
        String timeStr = mins > 0 ? String.format("%d:%02d min", mins, secs) : secs + "s";
        lblTimer.setText("Code expires in: " + timeStr);
    }

    @FXML
    private void onResendOtpClicked() {
        lblStatusStep2.setText("");
        generateAndDispatchOtp();
        showStatus2("A new verification code has been sent to your email.", false);
    }

    @FXML
    private void onVerifyOtpClicked() {
        lblStatusStep2.setText("");

        String rawInput = txtOtp.getText() != null ? txtOtp.getText() : "";
        String enteredOtp = rawInput.replaceAll("[^0-9]", "").trim();

        System.out.println("[ForgotPassword] Verifying OTP. Input: [" + rawInput + "] -> Cleaned: [" + enteredOtp + "] vs Expected: [" + activeOtp + "] (Remaining: " + secondsRemaining + "s)");

        if (enteredOtp.isEmpty()) {
            showStatus2("Please enter the 6-digit verification code.", true);
            return;
        }

        if (secondsRemaining <= 0) {
            showStatus2("The OTP code has expired. Please click 'Resend Code'.", true);
            return;
        }

        if (!enteredOtp.equals(activeOtp)) {
            showStatus2("Invalid verification code. Please check your latest email and try again.", true);
            return;
        }

        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        updateStepper(3);
        boxStep2.setVisible(false);
        boxStep2.setManaged(false);

        lblVerifiedAccountInfo.setText("Setting new password for " + currentRole + " ID: " + currentUserId);
        boxStep3.setVisible(true);
        boxStep3.setManaged(true);
        txtNewPassword.clear();
        txtConfirmPassword.clear();
        txtNewPassword.requestFocus();
    }

    private void updateStepper(int activeStep) {
        if (stepPill1 == null || stepPill2 == null || stepPill3 == null) return;

        stepPill1.getStyleClass().remove("step-pill-active");
        stepPill2.getStyleClass().remove("step-pill-active");
        stepPill3.getStyleClass().remove("step-pill-active");

        stepBadge1.getStyleClass().remove("step-badge-num-active");
        stepBadge2.getStyleClass().remove("step-badge-num-active");
        stepBadge3.getStyleClass().remove("step-badge-num-active");

        stepText1.getStyleClass().remove("step-pill-text-active");
        stepText2.getStyleClass().remove("step-pill-text-active");
        stepText3.getStyleClass().remove("step-pill-text-active");

        if (activeStep == 1) {
            stepPill1.getStyleClass().add("step-pill-active");
            stepBadge1.getStyleClass().add("step-badge-num-active");
            stepText1.getStyleClass().add("step-pill-text-active");
        } else if (activeStep == 2) {
            stepPill2.getStyleClass().add("step-pill-active");
            stepBadge2.getStyleClass().add("step-badge-num-active");
            stepText2.getStyleClass().add("step-pill-text-active");
        } else if (activeStep == 3) {
            stepPill3.getStyleClass().add("step-pill-active");
            stepBadge3.getStyleClass().add("step-badge-num-active");
            stepText3.getStyleClass().add("step-pill-text-active");
        }
    }

    @FXML
    private void onResetPasswordClicked() {
        lblStatusStep3.setText("");
        String newPass = txtNewPassword.getText();
        String confirmPass = txtConfirmPassword.getText();

        if (newPass == null || newPass.trim().isEmpty()) {
            showStatus3("Please enter a new password.", true);
            return;
        }

        if (newPass.length() < 4) {
            showStatus3("Password must be at least 4 characters long.", true);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showStatus3("Passwords do not match. Please re-enter.", true);
            return;
        }

        try {
            boolean updated = userDAO.updatePasswordByUserIdAndRole(currentUserId, currentRole, newPass);
            if (updated) {

                boxStep3.setVisible(false);
                boxStep3.setManaged(false);

                boxSuccess.setVisible(true);
                boxSuccess.setManaged(true);
            } else {
                showStatus3("Failed to update password in database.", true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showStatus3("Database error updating password: " + e.getMessage(), true);
        }
    }

    @FXML
    private void onToggleNewPasswordClicked() {
        toggleBothPasswords();
        if (isPasswordsVisible) {
            txtNewPasswordPlain.requestFocus();
            txtNewPasswordPlain.positionCaret(txtNewPasswordPlain.getText() != null ? txtNewPasswordPlain.getText().length() : 0);
        } else {
            txtNewPassword.requestFocus();
            txtNewPassword.positionCaret(txtNewPassword.getText() != null ? txtNewPassword.getText().length() : 0);
        }
    }

    @FXML
    private void onToggleConfirmPasswordClicked() {
        toggleBothPasswords();
        if (isPasswordsVisible) {
            txtConfirmPasswordPlain.requestFocus();
            txtConfirmPasswordPlain.positionCaret(txtConfirmPasswordPlain.getText() != null ? txtConfirmPasswordPlain.getText().length() : 0);
        } else {
            txtConfirmPassword.requestFocus();
            txtConfirmPassword.positionCaret(txtConfirmPassword.getText() != null ? txtConfirmPassword.getText().length() : 0);
        }
    }

    private void toggleBothPasswords() {
        isPasswordsVisible = !isPasswordsVisible;
        if (isPasswordsVisible) {

            txtNewPassword.setVisible(false);
            txtNewPassword.setManaged(false);
            txtNewPasswordPlain.setVisible(true);
            txtNewPasswordPlain.setManaged(true);

            txtConfirmPassword.setVisible(false);
            txtConfirmPassword.setManaged(false);
            txtConfirmPasswordPlain.setVisible(true);
            txtConfirmPasswordPlain.setManaged(true);

            btnToggleNewPassword.setText("👁‍🗨");
            btnToggleConfirmPassword.setText("👁‍🗨");
        } else {

            txtNewPasswordPlain.setVisible(false);
            txtNewPasswordPlain.setManaged(false);
            txtNewPassword.setVisible(true);
            txtNewPassword.setManaged(true);

            txtConfirmPasswordPlain.setVisible(false);
            txtConfirmPasswordPlain.setManaged(false);
            txtConfirmPassword.setVisible(true);
            txtConfirmPassword.setManaged(true);

            btnToggleNewPassword.setText("👁");
            btnToggleConfirmPassword.setText("👁");
        }
    }

    @FXML
    private void onBackToLoginClicked() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        try {
            Stage stage = (Stage) boxStep1.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.LOGIN_FXML, SceneManager.APP_TITLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIdx = email.indexOf('@');
        String namePart = email.substring(0, atIdx);
        String domainPart = email.substring(atIdx);

        if (namePart.length() <= 2) {
            return namePart.charAt(0) + "***" + domainPart;
        }
        return namePart.charAt(0) + "***" + namePart.charAt(namePart.length() - 1) + domainPart;
    }

    private void showStatus1(String message, boolean isError) {
        if (message == null || message.trim().isEmpty()) {
            lblStatusStep1.setText("");
            lblStatusStep1.setVisible(false);
            lblStatusStep1.setManaged(false);
            return;
        }
        lblStatusStep1.setText(message);
        lblStatusStep1.setStyle(isError
                ? "-fx-text-fill: #ef5350; -fx-background-color: rgba(239,83,80,0.12); -fx-background-radius: 6; -fx-padding: 8 12 8 12;"
                : "-fx-text-fill: #4ade80; -fx-background-color: rgba(74,222,128,0.12); -fx-background-radius: 6; -fx-padding: 8 12 8 12;");
        lblStatusStep1.setVisible(true);
        lblStatusStep1.setManaged(true);
    }

    private void showStatus2(String message, boolean isError) {
        if (message == null || message.trim().isEmpty()) {
            lblStatusStep2.setText("");
            lblStatusStep2.setVisible(false);
            lblStatusStep2.setManaged(false);
            return;
        }
        lblStatusStep2.setText(message);
        lblStatusStep2.setStyle(isError
                ? "-fx-text-fill: #ef5350; -fx-background-color: rgba(239,83,80,0.12); -fx-background-radius: 6; -fx-padding: 8 12 8 12;"
                : "-fx-text-fill: #4ade80; -fx-background-color: rgba(74,222,128,0.12); -fx-background-radius: 6; -fx-padding: 8 12 8 12;");
        lblStatusStep2.setVisible(true);
        lblStatusStep2.setManaged(true);
    }

    private void showStatus3(String message, boolean isError) {
        if (message == null || message.trim().isEmpty()) {
            lblStatusStep3.setText("");
            lblStatusStep3.setVisible(false);
            lblStatusStep3.setManaged(false);
            return;
        }
        lblStatusStep3.setText(message);
        lblStatusStep3.setStyle(isError
                ? "-fx-text-fill: #ef5350; -fx-background-color: rgba(239,83,80,0.12); -fx-background-radius: 6; -fx-padding: 8 12 8 12;"
                : "-fx-text-fill: #4ade80; -fx-background-color: rgba(74,222,128,0.12); -fx-background-radius: 6; -fx-padding: 8 12 8 12;");
        lblStatusStep3.setVisible(true);
        lblStatusStep3.setManaged(true);
    }
}
