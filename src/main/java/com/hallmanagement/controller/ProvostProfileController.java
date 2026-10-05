package com.hallmanagement.controller;

import com.hallmanagement.dao.HallDAO;
import com.hallmanagement.dao.ProvostDAO;
import com.hallmanagement.model.HallInfo;
import com.hallmanagement.model.ProvostInfo;
import com.hallmanagement.model.User;
import com.hallmanagement.util.BadgeHelper;
import com.hallmanagement.util.SceneManager;
import com.hallmanagement.util.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ProvostProfileController implements Initializable {

    private final ProvostDAO provostDAO = new ProvostDAO();
    private final HallDAO hallDAO = new HallDAO();

    private User currentUser;
    private ProvostInfo currentProvost;

    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavProfile;
    @FXML private Button btnNavHallInfo;
    @FXML private Button btnNavPaymentRequests;
    @FXML private Button btnNavHallChangeRequests;
    @FXML private Button btnNavRoomChangeRequests;
    @FXML private Button btnNavComplaints;
    @FXML private Button btnNavHallLeaveRequests;
    @FXML private Label lblSidebarUser;
    @FXML private Label lblSidebarRole;
    @FXML private Button btnLogout;

    @FXML private Label lblHeroName;
    @FXML private Label lblHeroDesignation;
    @FXML private Label lblHeroId;
    @FXML private Label lblAssignedHallName;

    @FXML private Label lblStatHallName;
    @FXML private Label lblStatRooms;
    @FXML private Label lblStatTotalSeats;
    @FXML private Label lblStatOccupied;
    @FXML private Label lblStatAvailable;

    @FXML private TextField txtName;
    @FXML private TextField txtUserId;
    @FXML private TextField txtDesignation;
    @FXML private TextField txtAge;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtOfficeRoom;
    @FXML private ComboBox<HallInfo> cmbAssignedHall;

    @FXML private Button btnEditToggle;
    @FXML private HBox boxEditActions;
    @FXML private Label lblFeedback;

    private boolean isEditMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblSidebarUser.setText("ID: " + currentUser.getUserId());
            lblSidebarRole.setText(currentUser.getName() + " (Provost)");
            BadgeHelper.updateAllProvostBadges(btnNavPaymentRequests, btnNavHallChangeRequests, btnNavRoomChangeRequests, btnNavComplaints, btnNavHallLeaveRequests, currentUser.getUserId());
        }

        setupHallComboBox();
        loadProfileData();
    }

    private void setupHallComboBox() {
        cmbAssignedHall.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(HallInfo hall) {
                return hall == null ? "" : hall.getHallName();
            }

            @Override
            public HallInfo fromString(String string) {
                return null;
            }
        });

        try {
            List<HallInfo> halls = hallDAO.getAllHallsWithStats();
            cmbAssignedHall.setItems(javafx.collections.FXCollections.observableArrayList(halls));
        } catch (SQLException e) {
            System.err.println("Failed to load halls in provost profile: " + e.getMessage());
        }
    }

    private void loadProfileData() {
        if (currentUser == null) return;

        try {
            currentProvost = provostDAO.getProvostProfile(currentUser.getUserId());
            if (currentProvost != null) {

                lblHeroName.setText(currentProvost.getName());
                lblHeroDesignation.setText(currentProvost.getDesignation());
                lblHeroId.setText("ID: " + currentProvost.getUserId());
                lblAssignedHallName.setText(currentProvost.getHallName());

                txtName.setText(currentProvost.getName());
                txtUserId.setText(currentProvost.getUserId());
                txtDesignation.setText(currentProvost.getDesignation());
                txtAge.setText(String.valueOf(currentProvost.getAge()));
                txtPhone.setText(currentProvost.getPhone() != null ? currentProvost.getPhone() : "");
                txtEmail.setText(currentProvost.getEmail());
                txtOfficeRoom.setText(currentProvost.getOfficeRoom());

                if (cmbAssignedHall.getItems() != null) {
                    for (HallInfo h : cmbAssignedHall.getItems()) {
                        if (h.getId() == currentProvost.getHallId()) {
                            cmbAssignedHall.setValue(h);
                            break;
                        }
                    }
                }

                HallInfo hallInfo = hallDAO.getHallById(currentProvost.getHallId());
                if (hallInfo != null) {
                    if (lblStatHallName != null) lblStatHallName.setText(hallInfo.getHallName());
                    if (lblStatRooms != null) lblStatRooms.setText(String.valueOf(hallInfo.getTotalRooms()));
                    if (lblStatTotalSeats != null) lblStatTotalSeats.setText(String.valueOf(hallInfo.getTotalSeats()));
                    if (lblStatOccupied != null) lblStatOccupied.setText(String.valueOf(hallInfo.getOccupiedSeats()));
                    if (lblStatAvailable != null) lblStatAvailable.setText(String.valueOf(hallInfo.getAvailableSeats()));
                } else {
                    if (lblStatHallName != null) lblStatHallName.setText(currentProvost.getHallName());
                }
            }

            setEditMode(false);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load provost profile: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setEditMode(boolean enable) {
        isEditMode = enable;
        txtName.setEditable(enable);
        txtDesignation.setEditable(enable);
        txtAge.setEditable(enable);
        txtPhone.setEditable(enable);
        txtEmail.setEditable(enable);
        txtOfficeRoom.setEditable(enable);
        cmbAssignedHall.setDisable(!enable);

        btnEditToggle.setVisible(!enable);
        btnEditToggle.setManaged(!enable);
        boxEditActions.setVisible(enable);
        boxEditActions.setManaged(enable);

        if (enable) {
            txtName.requestFocus();
            lblFeedback.setText("You may now edit your details above and click Save Changes.");
            lblFeedback.setStyle("-fx-text-fill: #90caf9;");
        } else {
            lblFeedback.setText("");
        }
    }

    @FXML
    private void onEditToggleClicked() {
        setEditMode(true);
    }

    @FXML
    private void onCancelEditClicked() {
        loadProfileData();
    }

    @FXML
    private void onSaveChangesClicked() {
        String name = txtName.getText().trim();
        String designation = txtDesignation.getText().trim();
        String ageStr = txtAge.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String officeRoom = txtOfficeRoom.getText().trim();

        if (name.isEmpty()) {
            lblFeedback.setText("Name cannot be blank.");
            lblFeedback.setStyle("-fx-text-fill: #ef5350;");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age < 21 || age > 95) {
                lblFeedback.setText("Please enter a valid age (21 - 95).");
                lblFeedback.setStyle("-fx-text-fill: #ef5350;");
                return;
            }
        } catch (NumberFormatException e) {
            lblFeedback.setText("Age must be a numeric integer.");
            lblFeedback.setStyle("-fx-text-fill: #ef5350;");
            return;
        }

        if (phone.isEmpty()) {
            lblFeedback.setText("Phone number cannot be blank.");
            lblFeedback.setStyle("-fx-text-fill: #ef5350;");
            return;
        }

        if (email.isEmpty() || !email.contains("@")) {
            lblFeedback.setText("Please enter a valid email address.");
            lblFeedback.setStyle("-fx-text-fill: #ef5350;");
            return;
        }

        HallInfo selectedHall = cmbAssignedHall.getValue();
        int hallId = selectedHall != null ? selectedHall.getId() : (currentProvost != null ? currentProvost.getHallId() : 1);

        try {
            provostDAO.updateProvostProfile(
                currentUser.getUserId(),
                name,
                phone,
                age,
                email,
                designation,
                officeRoom,
                hallId
            );

            currentUser.setName(name);
            currentUser.setPhone(phone);

            showAlert("Profile Updated", "Provost profile and hall assignment have been saved successfully.", Alert.AlertType.INFORMATION);
            loadProfileData();

        } catch (SQLException e) {
            lblFeedback.setText("Failed to save changes: " + e.getMessage());
            lblFeedback.setStyle("-fx-text-fill: #ef5350;");
        }
    }

    @FXML
    private void onRefreshClicked() {
        loadProfileData();
    }

    @FXML
    private void onDashboardClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_DASHBOARD_FXML, SceneManager.PROVOST_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to navigate to Provost Dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void onHallInformationClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.HALL_INFORMATION_FXML, SceneManager.HALL_INFORMATION_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to navigate to Hall Info: " + e.getMessage());
        }
    }

    @FXML
    private void onPaymentRequestsClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_PAYMENT_REQUESTS_FXML, SceneManager.PROVOST_PAYMENT_REQUESTS_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to navigate to Payment Requests: " + e.getMessage());
        }
    }

    @FXML
    private void onHallChangeRequestsClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_HALL_CHANGE_REQUESTS_FXML, SceneManager.PROVOST_HALL_CHANGE_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to navigate to Hall Change Requests: " + e.getMessage());
        }
    }

    @FXML
    private void onRoomChangeRequestsClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_ROOM_CHANGE_REQUESTS_FXML, SceneManager.PROVOST_ROOM_CHANGE_REQUESTS_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to navigate to Room Change Requests: " + e.getMessage());
        }
    }

    @FXML
    private void onNoticeBoardClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.NOTICE_BOARD_FXML, SceneManager.NOTICE_BOARD_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to navigate to Notice Board: " + e.getMessage());
        }
    }

    @FXML
    private void onComplaintsClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_COMPLAINTS_FXML, SceneManager.PROVOST_COMPLAINTS_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to navigate to Complaints: " + e.getMessage());
        }
    }

    @FXML
    private void onHallLeaveRequestsClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_HALL_LEAVE_REQUESTS_FXML, SceneManager.PROVOST_HALL_LEAVE_REQUESTS_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to navigate to Hall Leave Requests: " + e.getMessage());
        }
    }

    @FXML
    private void onBackClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.goBack(stage);
        } catch (Exception e) {
            System.err.println("[ProvostProfile] Error on back: " + e.getMessage());
        }
    }

    @FXML
    private void onLogoutClicked() {
        SessionManager.getInstance().logout();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.LOGIN_FXML, SceneManager.APP_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to logout: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
