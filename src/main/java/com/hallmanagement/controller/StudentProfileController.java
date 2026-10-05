package com.hallmanagement.controller;

import com.hallmanagement.dao.StudentDAO;
import com.hallmanagement.model.HallHistory;
import com.hallmanagement.model.Student;
import com.hallmanagement.util.BadgeHelper;
import com.hallmanagement.util.SceneManager;
import com.hallmanagement.util.SessionManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class StudentProfileController implements Initializable {

    private final StudentDAO studentDAO = new StudentDAO();
    private Student currentStudent;

    @FXML private Label  lblUserId;
    @FXML private Label  lblRole;

    @FXML private TextField txtName;
    @FXML private Label     lblNameDisplay;
    @FXML private TextField txtPhone;
    @FXML private Label     lblPhoneDisplay;
    @FXML private TextField txtEmail;
    @FXML private Label     lblEmailDisplay;
    @FXML private Label     lblStudentId;
    @FXML private Label     lblDepartment;
    @FXML private Label     lblYear;

    @FXML private Label     lblCurrentHall;
    @FXML private Label     lblCurrentRoom;
    @FXML private Label     lblCurrentSeat;

    @FXML private Button    btnEditName;
    @FXML private Button    btnSaveName;
    @FXML private Button    btnCancelName;

    @FXML private Button    btnEditPhone;
    @FXML private Button    btnSavePhone;
    @FXML private Button    btnCancelPhone;

    @FXML private Button    btnEditEmail;
    @FXML private Button    btnSaveEmail;
    @FXML private Button    btnCancelEmail;
    @FXML private Button    btnLogout;
    @FXML private Button    btnNavNoticeBoard;
    @FXML private Button    btnNavBilling;
    @FXML private Button    btnNavHallChange;
    @FXML private Button    btnNavRoomChange;
    @FXML private Button    btnNavComplaints;
    @FXML private Button    btnNavHallLeave;
    @FXML private Label     lblFeedback;

    @FXML private TableView<HallHistory> tableHistory;
    @FXML private TableColumn<HallHistory, String> colHall;
    @FXML private TableColumn<HallHistory, Integer> colRoom;
    @FXML private TableColumn<HallHistory, Integer> colSeat;
    @FXML private TableColumn<HallHistory, String> colFrom;
    @FXML private TableColumn<HallHistory, String> colTo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupSidebar();
        setupTableColumns();
        loadStudentData();
    }

    private void setupSidebar() {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            lblUserId.setText("User ID: " + session.getCurrentUserId());
            lblRole.setText("Role: Student");
            BadgeHelper.updateAllStudentBadges(btnNavNoticeBoard, btnNavBilling, btnNavHallChange, btnNavRoomChange, btnNavComplaints, btnNavHallLeave, session.getCurrentUserId());
        }
    }

    private void setupTableColumns() {
        colHall.setCellValueFactory(new PropertyValueFactory<>("hallName"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colSeat.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));
        colFrom.setCellValueFactory(new PropertyValueFactory<>("startDateFormatted"));
        colTo.setCellValueFactory(new PropertyValueFactory<>("endDateFormatted"));
    }

    private void loadStudentData() {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn()) return;

        try {
            currentStudent = studentDAO.getStudentByUserId(session.getCurrentUserId());
            if (currentStudent != null) {
                populateFields();
                loadHallHistory();
            } else {
                showFeedback("Student record not found.", true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showFeedback("Database error loading profile.", true);
        }
    }

    private void populateFields() {
        lblNameDisplay.setText(currentStudent.getName());
        lblStudentId.setText(currentStudent.getUserId());
        lblDepartment.setText(currentStudent.getDepartment() != null ? currentStudent.getDepartment() : "N/A");
        lblPhoneDisplay.setText(currentStudent.getPhone() != null ? currentStudent.getPhone() : "Not provided");
        lblEmailDisplay.setText(currentStudent.getEmail() != null && !currentStudent.getEmail().isEmpty() ? currentStudent.getEmail() : "Not provided");
        lblYear.setText(currentStudent.getYear() != null ? currentStudent.getYear() : "N/A");

        lblCurrentHall.setText(currentStudent.getCurrentHall() != null ? currentStudent.getCurrentHall() : "NONE");
        lblCurrentRoom.setText(currentStudent.getCurrentRoom() != null ? String.valueOf(currentStudent.getCurrentRoom()) : "NONE");
        lblCurrentSeat.setText(currentStudent.getCurrentSeat() != null ? String.valueOf(currentStudent.getCurrentSeat()) : "NONE");

        setEditNameMode(false);
        setEditPhoneMode(false);
        setEditEmailMode(false);
    }

    private void loadHallHistory() {
        try {
            List<HallHistory> history = studentDAO.getHallHistory(currentStudent.getDbId());
            ObservableList<HallHistory> observableHistory = FXCollections.observableArrayList(history);
            tableHistory.setItems(observableHistory);
        } catch (SQLException e) {
            e.printStackTrace();
            showFeedback("Error loading hall history.", true);
        }
    }

    @FXML
    private void onEditNameClicked() {
        setEditNameMode(true);
        lblFeedback.setText("");
    }

    @FXML
    private void onCancelNameClicked() {
        txtName.setText(currentStudent.getName());
        setEditNameMode(false);
        lblFeedback.setText("");
    }

    @FXML
    private void onSaveNameClicked() {
        String newName = txtName.getText().trim();
        if (newName.isEmpty()) {
            showFeedback("Please enter a valid name.", true);
            return;
        }

        try {
            boolean updated = studentDAO.updateStudentName(currentStudent.getUserId(), newName);
            if (updated) {
                currentStudent.setName(newName);
                SessionManager.getInstance().getCurrentUser().setName(newName);
                lblNameDisplay.setText(newName);
                setEditNameMode(false);
                showFeedback("Name updated successfully.", false);
            } else {
                showFeedback("Failed to update name.", true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showFeedback("Database error updating name.", true);
        }
    }

    @FXML
    private void onEditPhoneClicked() {
        setEditPhoneMode(true);
        lblFeedback.setText("");
    }

    @FXML
    private void onCancelPhoneClicked() {
        txtPhone.setText(currentStudent.getPhone());
        setEditPhoneMode(false);
        lblFeedback.setText("");
    }

    @FXML
    private void onSavePhoneClicked() {
        String newPhone = txtPhone.getText().trim();

        try {
            boolean updated = studentDAO.updateStudentPhone(currentStudent.getUserId(), newPhone);
            if (updated) {
                currentStudent.setPhone(newPhone);
                SessionManager.getInstance().getCurrentUser().setPhone(newPhone);
                lblPhoneDisplay.setText(newPhone.isEmpty() ? "Not provided" : newPhone);
                setEditPhoneMode(false);
                showFeedback("Phone updated successfully.", false);
            } else {
                showFeedback("Failed to update phone.", true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showFeedback("Database error updating phone.", true);
        }
    }

    @FXML
    private void onEditEmailClicked() {
        setEditEmailMode(true);
        txtEmail.setText(currentStudent.getEmail() != null ? currentStudent.getEmail() : "");
        lblFeedback.setText("");
    }

    @FXML
    private void onCancelEmailClicked() {
        txtEmail.setText(currentStudent.getEmail() != null ? currentStudent.getEmail() : "");
        setEditEmailMode(false);
        lblFeedback.setText("");
    }

    @FXML
    private void onSaveEmailClicked() {
        String newEmail = txtEmail.getText().trim();
        if (!newEmail.isEmpty() && (!newEmail.contains("@") || !newEmail.contains("."))) {
            showFeedback("Please enter a valid email address.", true);
            return;
        }

        try {
            boolean updated = studentDAO.updateStudentEmail(currentStudent.getUserId(), newEmail);
            if (updated) {
                currentStudent.setEmail(newEmail);
                lblEmailDisplay.setText(newEmail.isEmpty() ? "Not provided" : newEmail);
                setEditEmailMode(false);
                showFeedback("Email updated successfully.", false);
            } else {
                showFeedback("Failed to update email.", true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showFeedback("Database error updating email.", true);
        }
    }

    private void setEditNameMode(boolean editing) {
        txtName.setVisible(editing);
        txtName.setManaged(editing);

        lblNameDisplay.setVisible(!editing);
        lblNameDisplay.setManaged(!editing);

        btnSaveName.setVisible(editing);
        btnSaveName.setManaged(editing);
        btnCancelName.setVisible(editing);
        btnCancelName.setManaged(editing);

        btnEditName.setVisible(!editing);
        btnEditName.setManaged(!editing);
    }

    private void setEditPhoneMode(boolean editing) {
        txtPhone.setVisible(editing);
        txtPhone.setManaged(editing);

        lblPhoneDisplay.setVisible(!editing);
        lblPhoneDisplay.setManaged(!editing);

        btnSavePhone.setVisible(editing);
        btnSavePhone.setManaged(editing);
        btnCancelPhone.setVisible(editing);
        btnCancelPhone.setManaged(editing);

        btnEditPhone.setVisible(!editing);
        btnEditPhone.setManaged(!editing);
    }

    private void setEditEmailMode(boolean editing) {
        txtEmail.setVisible(editing);
        txtEmail.setManaged(editing);

        lblEmailDisplay.setVisible(!editing);
        lblEmailDisplay.setManaged(!editing);

        btnSaveEmail.setVisible(editing);
        btnSaveEmail.setManaged(editing);
        btnCancelEmail.setVisible(editing);
        btnCancelEmail.setManaged(editing);

        btnEditEmail.setVisible(!editing);
        btnEditEmail.setManaged(!editing);
    }

    private void showFeedback(String msg, boolean isError) {
        lblFeedback.setText(msg);
        if (isError) {
            lblFeedback.setStyle("-fx-text-fill: #e74c3c;");
        } else {
            lblFeedback.setStyle("-fx-text-fill: #2ecc71;");
        }
    }

    @FXML
    private void onDashboardClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_DASHBOARD_FXML, SceneManager.STUDENT_TITLE);
        } catch (IOException e) {
            System.err.println("[Profile] Failed to return to Dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void onLogoutClicked() {
        SessionManager.getInstance().logout();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.LOGIN_FXML, SceneManager.APP_TITLE);
        } catch (IOException e) {
            System.err.println("[Profile] Failed to return to login: " + e.getMessage());
            Platform.exit();
        }
    }

    @FXML
    private void onBackClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.goBack(stage);
        } catch (Exception e) {
            System.err.println("[StudentProfile] Failed to go back: " + e.getMessage());
        }
    }

    @FXML
    private void onBackToDashboardClicked() {
        onBackClicked();
    }

    @FXML
    private void onHallInformationClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.HALL_INFORMATION_FXML, SceneManager.HALL_INFORMATION_TITLE);
        } catch (IOException e) {
            System.err.println("[StudentProfile] Failed to open Hall Information: " + e.getMessage());
            showFeedback("Failed to load Hall Information.", true);
        }
    }

    @FXML
    private void onBillingClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_BILLING_FXML, SceneManager.STUDENT_BILLING_TITLE);
        } catch (IOException e) {
            System.err.println("[StudentProfile] Failed to open Bill & Due: " + e.getMessage());
            showFeedback("Failed to load Bill & Due.", true);
        }
    }

    @FXML
    private void onNoticeBoardClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.NOTICE_BOARD_FXML, SceneManager.NOTICE_BOARD_TITLE);
        } catch (IOException e) {
            System.err.println("[StudentProfile] Failed to open Notice Board: " + e.getMessage());
            showFeedback("Failed to load Notice Board.", true);
        }
    }

    @FXML
    private void onHallChangeClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_HALL_CHANGE_FXML, SceneManager.STUDENT_HALL_CHANGE_TITLE);
        } catch (IOException e) {
            System.err.println("[StudentProfile] Failed to open Hall Change: " + e.getMessage());
            showFeedback("Failed to load Hall Change.", true);
        }
    }

    @FXML
    private void onRoomChangeClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_ROOM_CHANGE_FXML, SceneManager.STUDENT_ROOM_CHANGE_TITLE);
        } catch (IOException e) {
            System.err.println("[StudentProfile] Failed to open Room Change: " + e.getMessage());
            showFeedback("Failed to load Room Change.", true);
        }
    }

    @FXML
    private void onComplaintsClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_COMPLAINTS_FXML, SceneManager.STUDENT_COMPLAINTS_TITLE);
        } catch (IOException e) {
            System.err.println("[StudentProfile] Failed to open Complaints: " + e.getMessage());
            showFeedback("Failed to load Complaints.", true);
        }
    }

    @FXML
    private void onHallLeaveClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_HALL_LEAVE_FXML, SceneManager.STUDENT_HALL_LEAVE_TITLE);
        } catch (IOException e) {
            System.err.println("[StudentProfile] Failed to open Hall Leave: " + e.getMessage());
            showFeedback("Failed to load Hall Leave.", true);
        }
    }
}
