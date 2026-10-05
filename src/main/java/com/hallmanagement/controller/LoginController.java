package com.hallmanagement.controller;

import com.hallmanagement.dao.UserDAO;
import com.hallmanagement.model.User;
import com.hallmanagement.util.SceneManager;
import com.hallmanagement.util.SessionManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField        txtUserId;
    @FXML private PasswordField    txtPassword;
    @FXML private TextField        txtPasswordPlain;
    @FXML private Button           btnTogglePassword;
    @FXML private ComboBox<String> cmbRole;
    @FXML private Button           btnLogin;
    @FXML private Button           btnClear;
    @FXML private Button           btnExit;
    @FXML private Label            lblStatus;

    private boolean isPasswordVisible = false;

    private static final String ROLE_STUDENT = "Student";
    private static final String ROLE_PROVOST = "Provost / Admin";

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        cmbRole.setItems(FXCollections.observableArrayList(ROLE_STUDENT, ROLE_PROVOST));
        cmbRole.setPromptText("Select Access Type");

        txtPasswordPlain.textProperty().bindBidirectional(txtPassword.textProperty());

        txtUserId.textProperty().addListener((obs, o, n) -> clearStatus());
        txtPassword.textProperty().addListener((obs, o, n) -> clearStatus());
        txtPasswordPlain.textProperty().addListener((obs, o, n) -> clearStatus());
        cmbRole.valueProperty().addListener((obs, o, n) -> clearStatus());

        clearStatus();

        Platform.runLater(() -> txtUserId.requestFocus());
    }

    @FXML
    private void onTogglePasswordClicked() {
        if (isPasswordVisible) {

            txtPasswordPlain.setVisible(false);
            txtPasswordPlain.setManaged(false);
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            btnTogglePassword.setText("👁");
            isPasswordVisible = false;
            txtPassword.requestFocus();
            txtPassword.positionCaret(txtPassword.getText() != null ? txtPassword.getText().length() : 0);
        } else {

            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
            txtPasswordPlain.setVisible(true);
            txtPasswordPlain.setManaged(true);
            btnTogglePassword.setText("👁‍🗨");
            isPasswordVisible = true;
            txtPasswordPlain.requestFocus();
            txtPasswordPlain.positionCaret(txtPasswordPlain.getText() != null ? txtPasswordPlain.getText().length() : 0);
        }
    }

    @FXML
    private void onLoginClicked() {
        performLogin();
    }

    @FXML
    private void onClearClicked() {
        txtUserId.clear();
        txtPassword.clear();
        txtPasswordPlain.clear();
        cmbRole.getSelectionModel().clearSelection();
        cmbRole.setPromptText("Select Access Type");
        clearStatus();
        txtUserId.requestFocus();
    }

    @FXML
    private void onExitClicked() {
        exitApplication();
    }

    @FXML
    private void onCreateAccountClicked() {
        try {
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.REGISTER_FXML, "Create Account — Hall Management");
        } catch (IOException e) {
            System.err.println("Failed to load Registration page: " + e.getMessage());
        }
    }

    @FXML
    private void onForgotPasswordClicked() {
        try {
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.FORGOT_PASSWORD_FXML, SceneManager.FORGOT_PASSWORD_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to load Forgot Password page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            performLogin();
        } else if (event.getCode() == KeyCode.ESCAPE) {
            exitApplication();
        }
    }

    private void performLogin() {

        String userId   = txtUserId.getText().trim();
        String password = txtPassword.getText();
        String roleDisplay = cmbRole.getValue();

        if (userId.isEmpty()) {
            showError("Please enter your User ID.");
            txtUserId.requestFocus();
            return;
        }
        if (password == null || password.isEmpty()) {
            showError("Please enter your password.");
            if (isPasswordVisible) {
                txtPasswordPlain.requestFocus();
            } else {
                txtPassword.requestFocus();
            }
            return;
        }
        if (roleDisplay == null || roleDisplay.isEmpty()) {
            showError("Please select an access type.");
            cmbRole.requestFocus();
            return;
        }

        String dbRole = ROLE_STUDENT.equals(roleDisplay) ? "STUDENT" : "PROVOST";

        btnLogin.setDisable(true);
        showInfo("Authenticating…");

        try {
            User user = userDAO.authenticate(userId, password, dbRole);

            SessionManager.getInstance().login(user);

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            navigateToDashboard(stage, user);

        } catch (UserDAO.InvalidCredentialsException e) {
            showError(e.getMessage());
        } catch (UserDAO.RoleMismatchException e) {
            showError(e.getMessage());
        } catch (UserDAO.InactiveAccountException e) {
            showError(e.getMessage());
        } catch (UserDAO.DatabaseException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            showError("Failed to load the dashboard. Please restart the application.");
            System.err.println("[LoginController] Scene load error: " + e.getMessage());
        } finally {
            btnLogin.setDisable(false);
        }
    }

    private void navigateToDashboard(Stage stage, User user) throws IOException {
        if (user.isStudent()) {
            SceneManager.switchTo(stage, SceneManager.STUDENT_DASHBOARD_FXML,
                    SceneManager.STUDENT_TITLE);
        } else {
            SceneManager.switchTo(stage, SceneManager.PROVOST_DASHBOARD_FXML,
                    SceneManager.PROVOST_TITLE);
        }
    }

    private void showError(String message) {
        lblStatus.setText(message);
        lblStatus.getStyleClass().removeAll("status-info");
        if (!lblStatus.getStyleClass().contains("status-error")) {
            lblStatus.getStyleClass().add("status-error");
        }
        lblStatus.setVisible(true);
        lblStatus.setManaged(true);
    }

    private void showInfo(String message) {
        lblStatus.setText(message);
        lblStatus.getStyleClass().removeAll("status-error");
        if (!lblStatus.getStyleClass().contains("status-info")) {
            lblStatus.getStyleClass().add("status-info");
        }
        lblStatus.setVisible(true);
        lblStatus.setManaged(true);
    }

    private void clearStatus() {
        lblStatus.setText("");
        lblStatus.getStyleClass().removeAll("status-error", "status-info");
        lblStatus.setVisible(false);
        lblStatus.setManaged(false);
    }

    private void exitApplication() {
        Stage stage = (Stage) btnExit.getScene().getWindow();
        stage.close();
        Platform.exit();
    }
}
