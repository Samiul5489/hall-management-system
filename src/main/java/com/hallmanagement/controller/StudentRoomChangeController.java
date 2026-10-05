package com.hallmanagement.controller;

import com.hallmanagement.dao.RoomChangeDAO;
import com.hallmanagement.dao.StudentDAO;
import com.hallmanagement.model.RoomChangeRequest;
import com.hallmanagement.model.Student;
import com.hallmanagement.model.User;
import com.hallmanagement.util.BadgeHelper;
import com.hallmanagement.util.SceneManager;
import com.hallmanagement.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class StudentRoomChangeController implements Initializable {

    private final RoomChangeDAO roomChangeDAO = new RoomChangeDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    private User currentUser;
    private Student currentStudent;
    private RoomChangeDAO.EligibilityStatus currentEligibility;
    private RoomChangeRequest activePendingRequest;
    private Integer editingRequestId = null;

    @FXML private Label lblSidebarUser;
    @FXML private Label lblSidebarRole;
    @FXML private Button btnLogout;
    @FXML private Button btnNavNoticeBoard;
    @FXML private Button btnNavBilling;
    @FXML private Button btnNavHallChange;
    @FXML private Button btnNavRoomChange;
    @FXML private Button btnNavComplaints;
    @FXML private Button btnNavHallLeave;

    @FXML private Label lblCurrentHall;
    @FXML private Label lblCurrentRoom;
    @FXML private Label lblCurrentSeat;

    @FXML private VBox boxEligibilityBanner;
    @FXML private Label lblEligibilityTitle;
    @FXML private Label lblEligibilityMessage;
    @FXML private Button btnPayDueAction;

    @FXML private VBox boxPendingNotice;
    @FXML private Label lblPendingNoticeSummary;

    @FXML private VBox boxApplicationForm;
    @FXML private Label lblFormTitle;
    @FXML private Button btnCancelForm;
    @FXML private Label lblFormStudentName;
    @FXML private Label lblFormStudentId;
    @FXML private Label lblFormStudentDept;
    @FXML private Label lblFormCurrentHall;
    @FXML private ComboBox<Integer> cmbRequestedRoom;
    @FXML private ComboBox<Integer> cmbRequestedSeat;
    @FXML private Label lblRoomInfoSubtext;
    @FXML private Label lblSeatInfoSubtext;
    @FXML private TextArea txtReason;
    @FXML private Button btnSubmitRequest;
    @FXML private Label lblFormFeedback;

    @FXML private VBox boxReviewModal;
    @FXML private Label lblModalStatus;
    @FXML private Label lblModalReqCode;
    @FXML private Label lblModalReqDate;
    @FXML private Label lblModalCurResidence;
    @FXML private Label lblModalReqResidence;
    @FXML private Label lblModalReason;
    @FXML private VBox boxModalProcessedInfo;
    @FXML private Label lblModalProcessedBy;
    @FXML private Label lblModalProcessedDate;
    @FXML private VBox boxModalRejectionReason;
    @FXML private Label lblModalRejectionReason;

    @FXML private Label lblHistoryCount;
    @FXML private TableView<RoomChangeRequest> tableRequests;
    @FXML private TableColumn<RoomChangeRequest, String> colSerial;
    @FXML private TableColumn<RoomChangeRequest, String> colDate;
    @FXML private TableColumn<RoomChangeRequest, String> colCurRoom;
    @FXML private TableColumn<RoomChangeRequest, String> colCurSeat;
    @FXML private TableColumn<RoomChangeRequest, String> colReqRoom;
    @FXML private TableColumn<RoomChangeRequest, String> colReqSeat;
    @FXML private TableColumn<RoomChangeRequest, String> colReason;
    @FXML private TableColumn<RoomChangeRequest, String> colStatus;
    @FXML private TableColumn<RoomChangeRequest, Void> colAction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblSidebarUser.setText("ID: " + currentUser.getUserId());
            lblSidebarRole.setText(currentUser.getName() + " (Student)");
        }

        setupTableColumns();
        setupDropdownListeners();
        loadAllData();
    }

    private void setupTableColumns() {
        colSerial.setCellValueFactory(new PropertyValueFactory<>("serial"));
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedRequestDate()));
        colCurRoom.setCellValueFactory(cell -> new SimpleStringProperty("Room " + cell.getValue().getCurrentRoom()));
        colCurSeat.setCellValueFactory(cell -> new SimpleStringProperty("Seat " + String.format("%02d", cell.getValue().getCurrentSeat())));
        colReqRoom.setCellValueFactory(cell -> new SimpleStringProperty("Room " + cell.getValue().getRequestedRoom()));
        colReqSeat.setCellValueFactory(cell -> new SimpleStringProperty("Seat " + String.format("%02d", cell.getValue().getRequestedSeat())));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        Label lblActionHeader = new Label("Action");
        lblActionHeader.setStyle("-fx-text-fill: #90caf9; -fx-font-weight: bold; -fx-font-size: 12.5px; -fx-alignment: CENTER;");
        lblActionHeader.setMaxWidth(Double.MAX_VALUE);
        lblActionHeader.setAlignment(Pos.CENTER);
        colAction.setGraphic(lblActionHeader);
        colAction.setText(null);

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status.toUpperCase()) {
                        case "PENDING":
                            setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                            break;
                        case "APPROVED":
                            setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                            break;
                        case "REJECTED":
                            setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                            break;
                        case "CANCELLED":
                            setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #e2e8f0;");
                    }
                }
            }
        });

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnReview = new Button("Review");
            private final HBox container = new HBox(6, btnReview);

            {
                setAlignment(Pos.CENTER);
                container.setAlignment(Pos.CENTER);
                btnReview.setStyle("-fx-background-color: rgba(26,115,232,0.22); -fx-text-fill: #90caf9; -fx-border-color: rgba(26,115,232,0.6); -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 14 4 14;");
                btnReview.setOnAction(event -> {
                    RoomChangeRequest req = getTableView().getItems().get(getIndex());
                    openReviewModal(req);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
    }

    private void setupDropdownListeners() {
        cmbRequestedRoom.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && currentStudent != null && currentStudent.getCurrentHallId() != null) {
                loadAvailableSeatsForSelectedRoom(newVal);
            } else {
                cmbRequestedSeat.getItems().clear();
            }
        });
    }

    private void loadAllData() {
        if (currentUser == null) return;

        try {
            currentStudent = studentDAO.getStudentByUserId(currentUser.getUserId());
            currentEligibility = roomChangeDAO.checkStudentEligibility(currentUser.getUserId());

            updateHeaderAndEligibilityUI();
            loadRequestHistory();
            updateSidebarBadges();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load Room Change data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateHeaderAndEligibilityUI() {
        if (currentStudent == null || currentEligibility == null) return;

        if (lblCurrentHall != null) lblCurrentHall.setText(currentStudent.getCurrentHall() != null ? currentStudent.getCurrentHall() : "NONE");
        if (lblCurrentRoom != null) lblCurrentRoom.setText(currentStudent.getCurrentRoom() != null ? String.valueOf(currentStudent.getCurrentRoom()) : "NONE");
        if (lblCurrentSeat != null) lblCurrentSeat.setText(currentStudent.getCurrentSeat() != null ? String.format("%02d", currentStudent.getCurrentSeat()) : "NONE");

        if (lblFormStudentName != null) lblFormStudentName.setText(currentStudent.getName() != null ? currentStudent.getName() : "");
        if (lblFormStudentId != null) lblFormStudentId.setText(currentStudent.getUserId() != null ? currentStudent.getUserId() : "");
        if (lblFormStudentDept != null) lblFormStudentDept.setText(currentStudent.getDepartment() != null ? currentStudent.getDepartment() : "RUET");
        if (lblFormCurrentHall != null) lblFormCurrentHall.setText(currentStudent.getCurrentHall() != null ? currentStudent.getCurrentHall() : "NONE");

        activePendingRequest = currentEligibility.getPendingRequest();

        if (currentEligibility.getTotalDue() > 0) {

            boxEligibilityBanner.setStyle("-fx-background-color: rgba(239,83,80,0.1); -fx-border-color: rgba(239,83,80,0.4); -fx-border-radius: 8; -fx-padding: 16;");
            lblEligibilityTitle.setText("Ineligible: Outstanding Due Found");
            lblEligibilityTitle.setStyle("-fx-text-fill: #ef5350; -fx-font-weight: bold; -fx-font-size: 15px;");
            lblEligibilityMessage.setText("You cannot apply for a room change because you have an outstanding due of Tk. " + String.format("%.2f", currentEligibility.getTotalDue()) + ". Please clear your due first.");
            btnPayDueAction.setVisible(true);
            btnPayDueAction.setManaged(true);

            boxPendingNotice.setVisible(false);
            boxPendingNotice.setManaged(false);
            boxApplicationForm.setVisible(false);
            boxApplicationForm.setManaged(false);

        } else if (currentEligibility.isHasPendingRequest() && activePendingRequest != null) {

            boxEligibilityBanner.setStyle("-fx-background-color: rgba(245,158,11,0.1); -fx-border-color: rgba(245,158,11,0.4); -fx-border-radius: 8; -fx-padding: 16;");
            lblEligibilityTitle.setText("Application In Progress");
            lblEligibilityTitle.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 15px;");
            lblEligibilityMessage.setText("You have an active pending Room/Seat Change application (" + activePendingRequest.getRequestCode() + "). You can review, edit, or cancel it below.");
            btnPayDueAction.setVisible(false);
            btnPayDueAction.setManaged(false);

            boxPendingNotice.setVisible(true);
            boxPendingNotice.setManaged(true);
            lblPendingNoticeSummary.setText("Application " + activePendingRequest.getRequestCode() + ": Requested Room " + activePendingRequest.getRequestedRoom() + " / Seat " + activePendingRequest.getRequestedSeat() + " (Submitted: " + activePendingRequest.getFormattedRequestDate() + ")");

            if (editingRequestId == null) {
                boxApplicationForm.setVisible(false);
                boxApplicationForm.setManaged(false);
            }

        } else if (!currentEligibility.isHasActiveResidence()) {

            boxEligibilityBanner.setStyle("-fx-background-color: rgba(239,83,80,0.1); -fx-border-color: rgba(239,83,80,0.4); -fx-border-radius: 8; -fx-padding: 16;");
            lblEligibilityTitle.setText("Ineligible: No Active Residence");
            lblEligibilityTitle.setStyle("-fx-text-fill: #ef5350; -fx-font-weight: bold; -fx-font-size: 15px;");
            lblEligibilityMessage.setText("You do not currently have an active Hall/Room/Seat, so you cannot submit a Room/Seat Change Request.");
            btnPayDueAction.setVisible(false);
            btnPayDueAction.setManaged(false);

            boxPendingNotice.setVisible(false);
            boxPendingNotice.setManaged(false);
            boxApplicationForm.setVisible(false);
            boxApplicationForm.setManaged(false);

        } else {

            boxEligibilityBanner.setStyle("-fx-background-color: rgba(74,222,128,0.1); -fx-border-color: rgba(74,222,128,0.4); -fx-border-radius: 8; -fx-padding: 16;");
            lblEligibilityTitle.setText("Eligible for Room/Seat Change");
            lblEligibilityTitle.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold; -fx-font-size: 15px;");
            lblEligibilityMessage.setText("Your account has zero outstanding dues. You may select a desired room and seat within " + currentStudent.getCurrentHall() + " below.");
            btnPayDueAction.setVisible(false);
            btnPayDueAction.setManaged(false);

            boxPendingNotice.setVisible(false);
            boxPendingNotice.setManaged(false);

            if (editingRequestId == null) {
                resetFormToNewApplication();
            }
        }
    }

    private void resetFormToNewApplication() {
        editingRequestId = null;
        boxApplicationForm.setVisible(true);
        boxApplicationForm.setManaged(true);
        lblFormTitle.setText("New Room/Seat Change Application");
        btnCancelForm.setVisible(false);
        btnCancelForm.setManaged(false);
        btnSubmitRequest.setText("📨  Submit Request");
        txtReason.clear();
        lblFormFeedback.setText("");

        loadAvailableRooms();
    }

    private void loadAvailableRooms() {
        if (currentStudent == null || currentStudent.getCurrentHallId() == null) return;
        try {
            List<Integer> rooms = roomChangeDAO.getAvailableRoomsForHall(currentStudent.getCurrentHallId());
            cmbRequestedRoom.setItems(FXCollections.observableArrayList(rooms));
            if (rooms.isEmpty()) {
                lblRoomInfoSubtext.setText("⚠️ No rooms with available seats are currently available in your Hall.");
                lblRoomInfoSubtext.setStyle("-fx-text-fill: #f87171;");
            } else {
                lblRoomInfoSubtext.setText("Found " + rooms.size() + " room(s) with available seats in " + currentStudent.getCurrentHall() + ".");
                lblRoomInfoSubtext.setStyle("-fx-text-fill: #94a3b8;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblFormFeedback.setText("Error loading available rooms: " + e.getMessage());
        }
    }

    private void loadAvailableSeatsForSelectedRoom(int roomNumber) {
        if (currentStudent == null || currentStudent.getCurrentHallId() == null) return;
        try {
            List<Integer> seats = roomChangeDAO.getAvailableSeatsForRoom(
                    currentStudent.getCurrentHallId(),
                    roomNumber,
                    currentStudent.getCurrentRoom(),
                    currentStudent.getCurrentSeat()
            );
            cmbRequestedSeat.setItems(FXCollections.observableArrayList(seats));
            if (seats.isEmpty()) {
                lblSeatInfoSubtext.setText("⚠️ No available seats in room " + roomNumber + ".");
                lblSeatInfoSubtext.setStyle("-fx-text-fill: #f87171;");
            } else {
                lblSeatInfoSubtext.setText(seats.size() + " vacant seat(s) available in room " + roomNumber + ".");
                lblSeatInfoSubtext.setStyle("-fx-text-fill: #4ade80;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblFormFeedback.setText("Error loading available seats: " + e.getMessage());
        }
    }

    private void loadRequestHistory() {
        try {
            List<RoomChangeRequest> history = roomChangeDAO.getRequestsForStudent(currentUser.getUserId());
            tableRequests.setItems(FXCollections.observableArrayList(history));
            lblHistoryCount.setText(history.size() + " request(s) total");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateSidebarBadges() {
        BadgeHelper.updateAllStudentBadges(
                btnNavNoticeBoard, btnNavBilling, btnNavHallChange,
                btnNavRoomChange, btnNavComplaints, btnNavHallLeave,
                currentUser.getUserId()
        );
    }

    @FXML
    private void onSubmitRequestClicked() {
        lblFormFeedback.setText("");

        Integer selectedRoom = cmbRequestedRoom.getValue();
        Integer selectedSeat = cmbRequestedSeat.getValue();
        String reason = txtReason.getText();

        if (selectedRoom == null) {
            lblFormFeedback.setText("Please select a requested room.");
            lblFormFeedback.setStyle("-fx-text-fill: #f87171;");
            return;
        }
        if (selectedSeat == null) {
            lblFormFeedback.setText("Please select a requested seat.");
            lblFormFeedback.setStyle("-fx-text-fill: #f87171;");
            return;
        }
        if (reason == null || reason.trim().isEmpty()) {
            lblFormFeedback.setText("Please provide a reason for the room/seat change.");
            lblFormFeedback.setStyle("-fx-text-fill: #f87171;");
            return;
        }

        try {
            if (editingRequestId != null) {

                RoomChangeRequest updated = roomChangeDAO.updatePendingRoomChangeRequest(
                        editingRequestId,
                        currentUser.getUserId(),
                        selectedRoom,
                        selectedSeat,
                        reason.trim()
                );
                showAlert("Application Updated", "Your Room/Seat Change Request (" + updated.getRequestCode() + ") has been updated successfully.", Alert.AlertType.INFORMATION);
                editingRequestId = null;
            } else {

                RoomChangeRequest created = roomChangeDAO.createRoomChangeRequest(
                        currentUser.getUserId(),
                        currentStudent.getCurrentHallId(),
                        currentStudent.getCurrentRoom(),
                        currentStudent.getCurrentSeat(),
                        selectedRoom,
                        selectedSeat,
                        reason.trim()
                );
                showAlert("Application Submitted", "Your Room/Seat Change Request (" + created.getRequestCode() + ") has been submitted successfully.\n\nIt is now pending review by the Provost.", Alert.AlertType.INFORMATION);
            }

            loadAllData();

        } catch (Exception ex) {
            lblFormFeedback.setText(ex.getMessage());
            lblFormFeedback.setStyle("-fx-text-fill: #f87171;");
        }
    }

    @FXML
    private void onEditPendingClicked() {
        if (activePendingRequest == null) return;

        editingRequestId = activePendingRequest.getId();
        boxApplicationForm.setVisible(true);
        boxApplicationForm.setManaged(true);
        lblFormTitle.setText("Edit Application: " + activePendingRequest.getRequestCode());
        btnCancelForm.setVisible(true);
        btnCancelForm.setManaged(true);
        btnSubmitRequest.setText("💾  Save Changes");
        txtReason.setText(activePendingRequest.getReason());
        lblFormFeedback.setText("Editing Application " + activePendingRequest.getRequestCode() + ". Select new room and seat below.");
        lblFormFeedback.setStyle("-fx-text-fill: #90caf9;");

        loadAvailableRooms();
        cmbRequestedRoom.setValue(activePendingRequest.getRequestedRoom());
        loadAvailableSeatsForSelectedRoom(activePendingRequest.getRequestedRoom());
        cmbRequestedSeat.setValue(activePendingRequest.getRequestedSeat());
    }

    @FXML
    private void onCancelFormClicked() {
        editingRequestId = null;
        loadAllData();
    }

    @FXML
    private void onCancelPendingClicked() {
        if (activePendingRequest == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Room/Seat Change Request");
        confirm.setHeaderText("Cancel Application (" + activePendingRequest.getRequestCode() + ")?");
        confirm.setContentText(
                "Are you sure you want to cancel your pending Room/Seat Change Request to Room " +
                activePendingRequest.getRequestedRoom() + " / Seat " + activePendingRequest.getRequestedSeat() + "?\n\n" +
                "Once cancelled, the request will be withdrawn."
        );

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                roomChangeDAO.cancelRoomChangeRequest(activePendingRequest.getId(), currentUser.getUserId());
                showAlert("Request Cancelled", "Your Room/Seat Change Request has been cancelled.", Alert.AlertType.INFORMATION);
                editingRequestId = null;
                loadAllData();
            } catch (Exception ex) {
                showAlert("Cancellation Error", ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void onReviewPendingClicked() {
        if (activePendingRequest != null) {
            openReviewModal(activePendingRequest);
        }
    }

    private void openReviewModal(RoomChangeRequest req) {
        if (req == null) return;

        lblModalStatus.setText(req.getStatus());
        switch (req.getStatus().toUpperCase()) {
            case "PENDING":
                lblModalStatus.setStyle("-fx-background-color: rgba(245,158,11,0.2); -fx-text-fill: #fbbf24; -fx-border-color: rgba(245,158,11,0.5);");
                break;
            case "APPROVED":
                lblModalStatus.setStyle("-fx-background-color: rgba(34,197,94,0.2); -fx-text-fill: #4ade80; -fx-border-color: rgba(34,197,94,0.5);");
                break;
            case "REJECTED":
                lblModalStatus.setStyle("-fx-background-color: rgba(239,68,68,0.2); -fx-text-fill: #f87171; -fx-border-color: rgba(239,68,68,0.5);");
                break;
            case "CANCELLED":
                lblModalStatus.setStyle("-fx-background-color: rgba(148,163,184,0.2); -fx-text-fill: #cbd5e1; -fx-border-color: rgba(148,163,184,0.5);");
                break;
        }

        lblModalReqCode.setText(req.getRequestCode());
        lblModalReqDate.setText(req.getFormattedRequestDate());
        lblModalCurResidence.setText(req.getCurrentResidenceDisplay());
        lblModalReqResidence.setText(req.getRequestedResidenceDisplay());
        lblModalReason.setText(req.getReason());

        if (req.isApproved() || req.isRejected()) {
            boxModalProcessedInfo.setVisible(true);
            boxModalProcessedInfo.setManaged(true);
            lblModalProcessedBy.setText(req.getProcessedByName() != null ? req.getProcessedByName() : (req.getProcessedBy() != null ? req.getProcessedBy() : "Provost"));
            lblModalProcessedDate.setText(req.getFormattedProcessedDate());

            if (req.isRejected() && req.getRejectionReason() != null) {
                boxModalRejectionReason.setVisible(true);
                boxModalRejectionReason.setManaged(true);
                lblModalRejectionReason.setText(req.getRejectionReason());
            } else {
                boxModalRejectionReason.setVisible(false);
                boxModalRejectionReason.setManaged(false);
            }

            roomChangeDAO.markRequestAsReviewedByStudent(req.getId(), currentUser.getUserId());
            updateSidebarBadges();
        } else {
            boxModalProcessedInfo.setVisible(false);
            boxModalProcessedInfo.setManaged(false);
        }

        boxReviewModal.setVisible(true);
        boxReviewModal.setManaged(true);
    }

    @FXML
    private void onCloseReviewModalClicked() {
        boxReviewModal.setVisible(false);
        boxReviewModal.setManaged(false);
    }

    @FXML
    private void onRefreshClicked() {
        loadAllData();
    }

    @FXML
    private void onDashboardClicked() {
        navigateTo(SceneManager.STUDENT_DASHBOARD_FXML, SceneManager.STUDENT_TITLE);
    }

    @FXML
    private void onProfileClicked() {
        navigateTo(SceneManager.STUDENT_PROFILE_FXML, SceneManager.STUDENT_PROFILE_TITLE);
    }

    @FXML
    private void onHallInformationClicked() {
        navigateTo(SceneManager.HALL_INFORMATION_FXML, SceneManager.HALL_INFORMATION_TITLE);
    }

    @FXML
    private void onBillingClicked() {
        navigateTo(SceneManager.STUDENT_BILLING_FXML, SceneManager.STUDENT_BILLING_TITLE);
    }

    @FXML
    private void onNoticeBoardClicked() {
        navigateTo(SceneManager.NOTICE_BOARD_FXML, SceneManager.NOTICE_BOARD_TITLE);
    }

    @FXML
    private void onHallChangeClicked() {
        navigateTo(SceneManager.STUDENT_HALL_CHANGE_FXML, SceneManager.STUDENT_HALL_CHANGE_TITLE);
    }

    @FXML
    private void onComplaintsClicked() {
        navigateTo(SceneManager.STUDENT_COMPLAINTS_FXML, SceneManager.STUDENT_COMPLAINTS_TITLE);
    }

    @FXML
    private void onHallLeaveClicked() {
        navigateTo(SceneManager.STUDENT_HALL_LEAVE_FXML, SceneManager.STUDENT_HALL_LEAVE_TITLE);
    }

    @FXML
    private void onBackClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.goBack(stage);
        } catch (Exception e) {
            System.err.println("[StudentRoomChange] Error on back: " + e.getMessage());
        }
    }

    @FXML
    private void onLogoutClicked() {
        SessionManager.getInstance().logout();
        navigateTo(SceneManager.LOGIN_FXML, SceneManager.APP_TITLE);
    }

    private void navigateTo(String fxml, String title) {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, fxml, title);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load screen: " + e.getMessage(), Alert.AlertType.ERROR);
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
