package com.hallmanagement.controller;

import com.hallmanagement.dao.HallChangeDAO;
import com.hallmanagement.dao.HallDAO;
import com.hallmanagement.dao.StudentDAO;
import com.hallmanagement.model.*;
import com.hallmanagement.util.BadgeHelper;
import com.hallmanagement.util.SceneManager;
import com.hallmanagement.util.SessionManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class StudentHallChangeController implements Initializable {

    private final HallChangeDAO hallChangeDAO = new HallChangeDAO();
    private final HallDAO hallDAO = new HallDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    private User currentUser;
    private Student currentStudent;

    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavProfile;
    @FXML private Button btnNavHallInfo;
    @FXML private Button btnNavBilling;
    @FXML private Button btnNavNoticeBoard;
    @FXML private Button btnNavHallChange;
    @FXML private Button btnNavRoomChange;
    @FXML private Button btnNavComplaints;
    @FXML private Button btnNavHallLeave;
    @FXML private Label lblSidebarUser;
    @FXML private Label lblSidebarRole;
    @FXML private Button btnLogout;

    @FXML private VBox boxEligibilityBanner;
    @FXML private Label lblEligibilityTitle;
    @FXML private Label lblEligibilityMessage;
    @FXML private HBox boxPendingActions;
    @FXML private Button btnEditPendingRequest;
    @FXML private Button btnCancelPendingRequest;
    @FXML private Button btnPayDueAction;

    @FXML private VBox boxApplicationForm;
    @FXML private Label lblCurHall;
    @FXML private Label lblCurRoom;
    @FXML private Label lblCurSeat;

    @FXML private ComboBox<HallInfo> cmbNewHall;
    @FXML private ComboBox<RoomInfo> cmbNewRoom;
    @FXML private ComboBox<SeatInfo> cmbNewSeat;
    @FXML private Label lblFormFeedback;
    @FXML private Button btnSubmitRequest;

    @FXML private Label lblHistoryCount;
    @FXML private TableView<HallChangeRequest> tableHistory;
    @FXML private TableColumn<HallChangeRequest, String> colSerial;
    @FXML private TableColumn<HallChangeRequest, String> colCode;
    @FXML private TableColumn<HallChangeRequest, String> colDate;
    @FXML private TableColumn<HallChangeRequest, String> colCurResidence;
    @FXML private TableColumn<HallChangeRequest, String> colReqResidence;
    @FXML private TableColumn<HallChangeRequest, String> colStatus;
    @FXML private TableColumn<HallChangeRequest, String> colRemarks;

    private HallChangeRequest activePendingRequest = null;
    private boolean isEditingExisting = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblSidebarUser.setText("ID: " + currentUser.getUserId());
            lblSidebarRole.setText(currentUser.getName() + " (Student)");
            BadgeHelper.updateAllStudentBadges(btnNavNoticeBoard, btnNavBilling, btnNavHallChange, btnNavRoomChange, btnNavComplaints, btnNavHallLeave, currentUser.getUserId());
        }

        setupTableColumns();
        setupComboBoxConverters();
        loadAllData();
    }

    private void setupTableColumns() {
        colSerial.setCellValueFactory(new PropertyValueFactory<>("serial"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("requestCode"));
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedRequestDate()));
        colCurResidence.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCurrentResidenceDisplay()));
        colReqResidence.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRequestedResidenceDisplay()));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colRemarks.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getRejectionReason() != null ? cell.getValue().getRejectionReason() :
                        (cell.getValue().isApproved() ? "Approved by " + (cell.getValue().getProcessedByName() != null ? cell.getValue().getProcessedByName() : "Provost") : "Awaiting review")
        ));

        colSerial.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #90caf9; -fx-alignment: CENTER;");
                }
            }
        });

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("APPROVED".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else if ("REJECTED".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #ef5350; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    }
                }
            }
        });
    }

    private void setupComboBoxConverters() {
        cmbNewHall.setConverter(new StringConverter<>() {
            @Override
            public String toString(HallInfo hall) {
                if (hall == null) return null;
                return hall.getHallName() + " (" + hall.getAvailableSeats() + " seats available)";
            }

            @Override
            public HallInfo fromString(String string) {
                return null;
            }
        });

        cmbNewRoom.setConverter(new StringConverter<>() {
            @Override
            public String toString(RoomInfo room) {
                if (room == null) return null;
                return "Room " + room.getRoomNumber() + " — " + room.getAvailableSeats() + " Available Seats";
            }

            @Override
            public RoomInfo fromString(String string) {
                return null;
            }
        });

        cmbNewSeat.setConverter(new StringConverter<>() {
            @Override
            public String toString(SeatInfo seat) {
                if (seat == null) return null;
                return String.format("Seat %02d", seat.getSeatNumber());
            }

            @Override
            public SeatInfo fromString(String string) {
                return null;
            }
        });
    }

    private void loadAllData() {
        if (currentUser == null) return;

        try {

            currentStudent = studentDAO.getStudentByUserId(currentUser.getUserId());
            if (currentStudent != null && currentStudent.getCurrentHall() != null) {
                lblCurHall.setText(currentStudent.getCurrentHall());
                lblCurRoom.setText("Room " + currentStudent.getCurrentRoom());
                lblCurSeat.setText(String.format("Seat %02d", currentStudent.getCurrentSeat()));
            } else {
                lblCurHall.setText("Not Assigned");
                lblCurRoom.setText("N/A");
                lblCurSeat.setText("N/A");
            }

            HallChangeDAO.EligibilityStatus status = hallChangeDAO.checkStudentEligibility(currentUser.getUserId());
            renderEligibilityBanner(status);

            List<HallChangeRequest> history = hallChangeDAO.getRequestsForStudent(currentUser.getUserId());
            tableHistory.setItems(FXCollections.observableArrayList(history));
            lblHistoryCount.setText(history.size() + " total requests");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load hall change data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void renderEligibilityBanner(HallChangeDAO.EligibilityStatus status) throws SQLException {
        activePendingRequest = status.getPendingRequest();
        isEditingExisting = false;
        btnSubmitRequest.setText("Submit Transfer Application");

        if (status.getTotalDue() > 0) {

            boxEligibilityBanner.setStyle("-fx-background-color: rgba(239,83,80,0.1); -fx-border-color: rgba(239,83,80,0.4); -fx-border-radius: 8; -fx-padding: 16;");
            lblEligibilityTitle.setText("Ineligible: Outstanding Due Found");
            lblEligibilityTitle.setStyle("-fx-text-fill: #ef5350; -fx-font-weight: bold; -fx-font-size: 15px;");
            lblEligibilityMessage.setText(status.getMessage());
            btnPayDueAction.setVisible(true);
            btnPayDueAction.setManaged(true);
            boxPendingActions.setVisible(false);
            boxPendingActions.setManaged(false);
            boxApplicationForm.setVisible(false);
            boxApplicationForm.setManaged(false);

        } else if (status.isHasPendingRequest()) {

            boxEligibilityBanner.setStyle("-fx-background-color: rgba(245,158,11,0.1); -fx-border-color: rgba(245,158,11,0.4); -fx-border-radius: 8; -fx-padding: 16;");
            lblEligibilityTitle.setText("Application In Progress");
            lblEligibilityTitle.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 15px;");
            lblEligibilityMessage.setText(status.getMessage() + " You can edit your requested room/seat or cancel your request before Provost review.");
            btnPayDueAction.setVisible(false);
            btnPayDueAction.setManaged(false);
            boxPendingActions.setVisible(true);
            boxPendingActions.setManaged(true);
            boxApplicationForm.setVisible(false);
            boxApplicationForm.setManaged(false);

        } else {

            boxEligibilityBanner.setStyle("-fx-background-color: rgba(74,222,128,0.1); -fx-border-color: rgba(74,222,128,0.4); -fx-border-radius: 8; -fx-padding: 16;");
            lblEligibilityTitle.setText("Eligible for Hall Transfer");
            lblEligibilityTitle.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold; -fx-font-size: 15px;");
            lblEligibilityMessage.setText("Your hall account has zero outstanding dues. You may select a desired hall, room, and seat below.");
            btnPayDueAction.setVisible(false);
            btnPayDueAction.setManaged(false);
            boxPendingActions.setVisible(false);
            boxPendingActions.setManaged(false);
            boxApplicationForm.setVisible(true);
            boxApplicationForm.setManaged(true);

            loadAvailableHalls();
        }
    }

    private void loadAvailableHalls() throws SQLException {
        List<HallInfo> allHalls = hallDAO.getAllHallsWithStats();

        List<HallInfo> availableHalls = allHalls.stream()
                .filter(h -> h.getAvailableSeats() > 0)
                .toList();

        cmbNewHall.setItems(FXCollections.observableArrayList(availableHalls));
        cmbNewRoom.getItems().clear();
        cmbNewRoom.setDisable(true);
        cmbNewSeat.getItems().clear();
        cmbNewSeat.setDisable(true);
    }

    @FXML
    private void onNewHallSelected() {
        HallInfo selectedHall = cmbNewHall.getValue();
        cmbNewRoom.getItems().clear();
        cmbNewSeat.getItems().clear();
        cmbNewSeat.setDisable(true);

        if (selectedHall == null) {
            cmbNewRoom.setDisable(true);
            return;
        }

        try {
            List<RoomInfo> rooms = hallDAO.getRoomsByHallId(selectedHall.getId());

            List<RoomInfo> availableRooms = rooms.stream()
                    .filter(r -> r.getAvailableSeats() > 0)
                    .toList();

            cmbNewRoom.setItems(FXCollections.observableArrayList(availableRooms));
            cmbNewRoom.setDisable(false);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load rooms: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onNewRoomSelected() {
        RoomInfo selectedRoom = cmbNewRoom.getValue();
        cmbNewSeat.getItems().clear();

        if (selectedRoom == null) {
            cmbNewSeat.setDisable(true);
            return;
        }

        try {
            List<SeatInfo> seats = hallDAO.getSeatsByRoomId(selectedRoom.getRoomId());

            List<SeatInfo> availableSeats = seats.stream()
                    .filter(s -> !s.isOccupied())
                    .toList();

            cmbNewSeat.setItems(FXCollections.observableArrayList(availableSeats));
            cmbNewSeat.setDisable(false);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load seats: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onSubmitRequestClicked() {
        lblFormFeedback.setText("");

        HallInfo hall = cmbNewHall.getValue();
        RoomInfo room = cmbNewRoom.getValue();
        SeatInfo seat = cmbNewSeat.getValue();

        if (hall == null) {
            lblFormFeedback.setText("Please select a target hall.");
            return;
        }
        if (room == null) {
            lblFormFeedback.setText("Please select an available room.");
            return;
        }
        if (seat == null) {
            lblFormFeedback.setText("Please select an available seat.");
            return;
        }

        if (currentStudent != null && currentStudent.getCurrentHall() != null
                && currentStudent.getCurrentHall().equalsIgnoreCase(hall.getHallName())
                && currentStudent.getCurrentRoom() != null
                && currentStudent.getCurrentRoom() == room.getRoomNumber()
                && currentStudent.getCurrentSeat() != null
                && currentStudent.getCurrentSeat() == seat.getSeatNumber()) {
            lblFormFeedback.setText("Cannot request transfer to the exact same seat you currently occupy.");
            return;
        }

        try {
            if (isEditingExisting && activePendingRequest != null) {

                HallChangeRequest updated = hallChangeDAO.updatePendingHallChangeRequest(
                        activePendingRequest.getId(),
                        currentUser.getUserId(),
                        hall.getId(),
                        room.getRoomNumber(),
                        seat.getSeatNumber()
                );

                showAlert("Application Updated",
                        "Your Hall Change Request (" + updated.getRequestCode() + ") has been updated successfully.\n\n" +
                        "New Requested Residence: " + updated.getRequestedResidenceDisplay() + "\n\n" +
                        "Please wait for Provost verification and administrative approval.",
                        Alert.AlertType.INFORMATION);
            } else {

                HallChangeRequest created = hallChangeDAO.createHallChangeRequest(
                        currentUser.getUserId(),
                        hall.getId(),
                        room.getRoomNumber(),
                        seat.getSeatNumber()
                );

                showAlert("Application Submitted",
                        "Your Hall Change Request (" + created.getRequestCode() + ") has been submitted successfully.\n\n" +
                        "Requested Residence: " + created.getRequestedResidenceDisplay() + "\n\n" +
                        "Please wait for Provost verification and administrative approval.",
                        Alert.AlertType.INFORMATION);
            }

            loadAllData();

        } catch (Exception e) {
            lblFormFeedback.setText(e.getMessage());
        }
    }

    @FXML
    private void onCancelPendingRequestClicked() {
        if (activePendingRequest == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Hall Change Request");
        confirm.setHeaderText("Cancel Transfer Application (" + activePendingRequest.getRequestCode() + ")?");
        confirm.setContentText(
                "Are you sure you want to cancel your pending transfer request to:\n\n" +
                "  " + activePendingRequest.getRequestedResidenceDisplay() + "\n\n" +
                "Once cancelled, your active application will be withdrawn and you can submit a new application whenever you wish."
        );

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                hallChangeDAO.cancelHallChangeRequest(activePendingRequest.getId(), currentUser.getUserId());
                showAlert("Request Withdrawn", "Your hall change request has been cancelled successfully. You are now free to submit a new request.", Alert.AlertType.INFORMATION);
                loadAllData();
            } catch (Exception e) {
                showAlert("Cancellation Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void onEditPendingRequestClicked() {
        if (activePendingRequest == null) return;

        isEditingExisting = true;
        btnSubmitRequest.setText("💾 Save Request Changes");
        boxApplicationForm.setVisible(true);
        boxApplicationForm.setManaged(true);
        lblFormFeedback.setText("Editing Application " + activePendingRequest.getRequestCode() + ": Select desired new hall, room, and seat below.");
        lblFormFeedback.setStyle("-fx-text-fill: #90caf9;");

        try {
            loadAvailableHalls();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load available halls: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onRefreshClicked() {
        loadAllData();
    }

    @FXML
    private void onDashboardClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_DASHBOARD_FXML, SceneManager.STUDENT_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to navigate to Student Dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void onProfileClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_PROFILE_FXML, SceneManager.STUDENT_PROFILE_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to navigate to Profile: " + e.getMessage());
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
    private void onBillingClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_BILLING_FXML, SceneManager.STUDENT_BILLING_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to navigate to Billing: " + e.getMessage());
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
    private void onRoomChangeClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_ROOM_CHANGE_FXML, SceneManager.STUDENT_ROOM_CHANGE_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to navigate to Room Change: " + e.getMessage());
        }
    }

    @FXML
    private void onComplaintsClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_COMPLAINTS_FXML, SceneManager.STUDENT_COMPLAINTS_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to navigate to Complaints: " + e.getMessage());
        }
    }

    @FXML
    private void onHallLeaveClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_HALL_LEAVE_FXML, SceneManager.STUDENT_HALL_LEAVE_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to navigate to Hall Leave: " + e.getMessage());
        }
    }

    @FXML
    private void onBackClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.goBack(stage);
        } catch (Exception e) {
            System.err.println("[StudentHallChange] Error on back: " + e.getMessage());
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
