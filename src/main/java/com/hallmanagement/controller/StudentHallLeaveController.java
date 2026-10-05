package com.hallmanagement.controller;

import com.hallmanagement.dao.HallLeaveDAO;
import com.hallmanagement.dao.StudentDAO;
import com.hallmanagement.model.HallLeaveRequest;
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

public class StudentHallLeaveController implements Initializable {

    private final HallLeaveDAO hallLeaveDAO = new HallLeaveDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    private User currentUser;
    private Student currentStudent;
    private HallLeaveDAO.EligibilityStatus currentEligibility;
    private HallLeaveRequest activePendingRequest;

    @FXML private Label lblSidebarUser;
    @FXML private Label lblSidebarRole;
    @FXML private Button btnLogout;
    @FXML private Button btnNavNoticeBoard;
    @FXML private Button btnNavBilling;
    @FXML private Button btnNavHallChange;
    @FXML private Button btnNavRoomChange;
    @FXML private Button btnNavComplaints;
    @FXML private Button btnNavHallLeave;

    @FXML private VBox boxEligibilityBanner;
    @FXML private Label lblEligibilityTitle;
    @FXML private Label lblEligibilityMessage;
    @FXML private Button btnOpenApplyForm;
    @FXML private Button btnPayDueAction;
    @FXML private HBox boxPendingActions;
    @FXML private Button btnViewPendingDetails;
    @FXML private Button btnCancelPendingRequest;

    @FXML private Label lblResHall;
    @FXML private Label lblResRoom;
    @FXML private Label lblResSeat;
    @FXML private Label lblResDue;

    @FXML private VBox boxLeaveForm;
    @FXML private Label lblFormStudentName;
    @FXML private Label lblFormStudentId;
    @FXML private Label lblFormStudentDept;
    @FXML private Label lblFormDue;
    @FXML private Label lblFormHall;
    @FXML private Label lblFormRoom;
    @FXML private Label lblFormSeat;
    @FXML private TextArea txtReason;
    @FXML private Label lblFormFeedback;
    @FXML private Button btnSubmitLeaveRequest;

    @FXML private VBox boxViewModal;
    @FXML private Label lblModalTitle;
    @FXML private Label lblModalStatusBadge;
    @FXML private Label lblModalReqCode;
    @FXML private Label lblModalReqDate;
    @FXML private Label lblModalHall;
    @FXML private Label lblModalRoomSeat;
    @FXML private Label lblModalReason;
    @FXML private VBox boxProvostDecision;
    @FXML private Label lblModalProcessedBy;
    @FXML private Label lblModalProcessedDate;
    @FXML private VBox boxDeclineReason;
    @FXML private Label lblModalDeclineReason;
    @FXML private Button btnModalCancelRequest;

    @FXML private TableView<HallLeaveRequest> tableLeaveHistory;
    @FXML private TableColumn<HallLeaveRequest, String> colSerial;
    @FXML private TableColumn<HallLeaveRequest, String> colDate;
    @FXML private TableColumn<HallLeaveRequest, String> colHall;
    @FXML private TableColumn<HallLeaveRequest, String> colRoom;
    @FXML private TableColumn<HallLeaveRequest, String> colSeat;
    @FXML private TableColumn<HallLeaveRequest, String> colStatus;
    @FXML private TableColumn<HallLeaveRequest, Void> colAction;
    @FXML private Label lblHistoryCount;

    private HallLeaveRequest currentlyViewedRequest;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblSidebarUser.setText("ID: " + currentUser.getUserId());
            lblSidebarRole.setText(currentUser.getName() + " (Student)");
        }

        setupTableColumns();
        loadAllData();
    }

    private void setupTableColumns() {
        colSerial.setCellValueFactory(new PropertyValueFactory<>("serial"));
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedRequestDate()));
        colHall.setCellValueFactory(new PropertyValueFactory<>("hallName"));
        colRoom.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getRoomNumber())));
        colSeat.setCellValueFactory(cell -> new SimpleStringProperty(String.format("%02d", cell.getValue().getSeatNumber())));
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
                            setStyle("-fx-text-fill: #fbc02d; -fx-font-weight: bold;");
                            break;
                        case "APPROVED":
                            setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");
                            break;
                        case "DECLINED":
                            setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");
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
                    HallLeaveRequest req = getTableView().getItems().get(getIndex());
                    openViewModal(req);
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

    private void loadAllData() {
        if (currentUser == null) return;

        try {
            currentStudent = studentDAO.getStudentByUserId(currentUser.getUserId());
            currentEligibility = hallLeaveDAO.checkStudentEligibility(currentUser.getUserId());

            updateResidenceAndEligibilityUI();
            loadRequestHistory();
            updateBadges();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load leave eligibility data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateResidenceAndEligibilityUI() {
        if (currentStudent == null || currentEligibility == null) return;

        if (currentStudent.getCurrentHall() != null && !currentStudent.getCurrentHall().isEmpty()) {
            lblResHall.setText(currentStudent.getCurrentHall());
            lblResRoom.setText(String.valueOf(currentStudent.getCurrentRoom()));
            lblResSeat.setText(String.format("%02d", currentStudent.getCurrentSeat()));
        } else {
            lblResHall.setText("NONE");
            lblResRoom.setText("NONE");
            lblResSeat.setText("NONE");
        }

        double due = currentEligibility.getTotalDue();
        lblResDue.setText(String.format("Tk. %.2f", due));
        if (due > 0) {
            lblResDue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f87171;");
        } else {
            lblResDue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4ade80;");
        }

        btnOpenApplyForm.setVisible(false);
        btnOpenApplyForm.setManaged(false);
        btnPayDueAction.setVisible(false);
        btnPayDueAction.setManaged(false);
        boxPendingActions.setVisible(false);
        boxPendingActions.setManaged(false);

        activePendingRequest = currentEligibility.getPendingRequest();

        if (!currentEligibility.isHasActiveHall()) {

            lblEligibilityTitle.setText("Ineligible: No Active Hall Assignment");
            lblEligibilityTitle.setStyle("-fx-text-fill: #94a3b8;");
            lblEligibilityMessage.setText("You are not currently assigned to any hall. Hall leave application is not available.");

        } else if (due > 0) {

            lblEligibilityTitle.setText("Ineligible: Outstanding Hall Due Found");
            lblEligibilityTitle.setStyle("-fx-text-fill: #f87171;");
            lblEligibilityMessage.setText(currentEligibility.getMessage());
            btnPayDueAction.setVisible(true);
            btnPayDueAction.setManaged(true);

        } else if (currentEligibility.isHasPendingRequest()) {

            lblEligibilityTitle.setText("Application In Progress");
            lblEligibilityTitle.setStyle("-fx-text-fill: #fbc02d;");
            lblEligibilityMessage.setText(currentEligibility.getMessage());
            boxPendingActions.setVisible(true);
            boxPendingActions.setManaged(true);

        } else {

            lblEligibilityTitle.setText("Eligible for Hall Leave");
            lblEligibilityTitle.setStyle("-fx-text-fill: #4ade80;");
            lblEligibilityMessage.setText("Your hall dues are fully cleared (Tk. 0). You can now submit an official leave request.");
            btnOpenApplyForm.setVisible(true);
            btnOpenApplyForm.setManaged(true);
        }
    }

    private void loadRequestHistory() {
        try {
            List<HallLeaveRequest> requests = hallLeaveDAO.getRequestsForStudent(currentUser.getUserId());
            ObservableList<HallLeaveRequest> observableList = FXCollections.observableArrayList(requests);
            tableLeaveHistory.setItems(observableList);
            lblHistoryCount.setText(requests.size() + " total request(s)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateBadges() {
        if (currentUser != null) {
            BadgeHelper.updateAllStudentBadges(
                btnNavNoticeBoard,
                btnNavBilling,
                btnNavHallChange,
                btnNavRoomChange,
                btnNavComplaints,
                btnNavHallLeave,
                currentUser.getUserId()
            );
        }
    }

    @FXML
    private void onOpenApplyFormClicked() {
        if (currentStudent == null || !currentEligibility.isEligible()) {
            showAlert("Ineligible", "You are not eligible to apply for hall leave at this time.", Alert.AlertType.WARNING);
            return;
        }

        lblFormStudentName.setText(currentStudent.getName());
        lblFormStudentId.setText(currentStudent.getUserId());
        lblFormStudentDept.setText(currentStudent.getDepartment() != null ? currentStudent.getDepartment() : "N/A");
        lblFormDue.setText("Tk. 0.00");
        lblFormHall.setText(currentStudent.getCurrentHall());
        lblFormRoom.setText("Room " + currentStudent.getCurrentRoom());
        lblFormSeat.setText("Seat " + String.format("%02d", currentStudent.getCurrentSeat()));

        txtReason.clear();
        lblFormFeedback.setText("");

        boxLeaveForm.setVisible(true);
        boxLeaveForm.setManaged(true);
        boxViewModal.setVisible(false);
        boxViewModal.setManaged(false);
    }

    @FXML
    private void onCloseFormClicked() {
        boxLeaveForm.setVisible(false);
        boxLeaveForm.setManaged(false);
        lblFormFeedback.setText("");
    }

    @FXML
    private void onSubmitLeaveRequestClicked() {
        String reason = txtReason.getText().trim();
        if (reason.isEmpty()) {
            lblFormFeedback.setText("Please enter a valid reason for leaving the hall.");
            return;
        }

        try {
            HallLeaveRequest created = hallLeaveDAO.createHallLeaveRequest(
                currentUser.getUserId(),
                currentStudent.getCurrentHallId(),
                currentStudent.getCurrentRoom(),
                currentStudent.getCurrentSeat(),
                reason
            );

            showAlert("Request Submitted", "Your Hall Leave Request (" + created.getRequestCode() + ") has been submitted to the Provost for review.", Alert.AlertType.INFORMATION);

            onCloseFormClicked();
            loadAllData();

        } catch (Exception e) {
            lblFormFeedback.setText(e.getMessage());
        }
    }

    private void openViewModal(HallLeaveRequest req) {
        if (req == null) return;
        currentlyViewedRequest = req;

        lblModalReqCode.setText(req.getRequestCode());
        lblModalReqDate.setText(req.getFormattedRequestDate());
        lblModalHall.setText(req.getHallName());
        lblModalRoomSeat.setText("Room " + req.getRoomNumber() + ", Seat " + String.format("%02d", req.getSeatNumber()));
        lblModalReason.setText(req.getReason());

        lblModalStatusBadge.setText(req.getStatus());
        switch (req.getStatus().toUpperCase()) {
            case "PENDING":
                lblModalStatusBadge.setStyle("-fx-background-color: #3b2d08; -fx-text-fill: #fbc02d; -fx-padding: 4 10; -fx-background-radius: 4px; -fx-font-weight: bold;");
                btnModalCancelRequest.setVisible(true);
                btnModalCancelRequest.setManaged(true);
                break;
            case "APPROVED":
                lblModalStatusBadge.setStyle("-fx-background-color: #064e3b; -fx-text-fill: #34d399; -fx-padding: 4 10; -fx-background-radius: 4px; -fx-font-weight: bold;");
                btnModalCancelRequest.setVisible(false);
                btnModalCancelRequest.setManaged(false);
                break;
            case "DECLINED":
                lblModalStatusBadge.setStyle("-fx-background-color: #450a0a; -fx-text-fill: #f87171; -fx-padding: 4 10; -fx-background-radius: 4px; -fx-font-weight: bold;");
                btnModalCancelRequest.setVisible(false);
                btnModalCancelRequest.setManaged(false);
                break;
            default:
                lblModalStatusBadge.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #94a3b8; -fx-padding: 4 10; -fx-background-radius: 4px; -fx-font-weight: bold;");
                btnModalCancelRequest.setVisible(false);
                btnModalCancelRequest.setManaged(false);
        }

        if (req.isProcessed()) {
            boxProvostDecision.setVisible(true);
            boxProvostDecision.setManaged(true);
            lblModalProcessedBy.setText(req.getProcessedByName() != null ? req.getProcessedByName() : (req.getProcessedBy() != null ? req.getProcessedBy() : "Provost Administration"));
            lblModalProcessedDate.setText(req.getFormattedProcessedDate());

            if (req.isDeclined() && req.getDeclineReason() != null && !req.getDeclineReason().isEmpty()) {
                boxDeclineReason.setVisible(true);
                boxDeclineReason.setManaged(true);
                lblModalDeclineReason.setText(req.getDeclineReason());
            } else {
                boxDeclineReason.setVisible(false);
                boxDeclineReason.setManaged(false);
            }

            hallLeaveDAO.markLeaveRequestAsReviewedByStudent(req.getId(), currentUser.getUserId());
            req.setStudentViewedAt(java.time.LocalDateTime.now());
            updateBadges();

        } else {
            boxProvostDecision.setVisible(false);
            boxProvostDecision.setManaged(false);
        }

        boxViewModal.setVisible(true);
        boxViewModal.setManaged(true);
        boxLeaveForm.setVisible(false);
        boxLeaveForm.setManaged(false);
    }

    @FXML
    private void onCloseModalClicked() {
        boxViewModal.setVisible(false);
        boxViewModal.setManaged(false);
        currentlyViewedRequest = null;
    }

    @FXML
    private void onViewPendingDetailsClicked() {
        if (activePendingRequest != null) {
            openViewModal(activePendingRequest);
        }
    }

    @FXML
    private void onCancelPendingRequestClicked() {
        if (activePendingRequest != null) {
            cancelRequest(activePendingRequest);
        }
    }

    @FXML
    private void onModalCancelRequestClicked() {
        if (currentlyViewedRequest != null) {
            cancelRequest(currentlyViewedRequest);
        }
    }

    private void cancelRequest(HallLeaveRequest req) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Hall Leave Request");
        confirm.setHeaderText("Cancel Request " + req.getRequestCode());
        confirm.setContentText("Are you sure you want to cancel this hall leave request? Your hall residence will remain unchanged.");

        ButtonType btnCancelReq = new ButtonType("Cancel Request", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnKeep = new ButtonType("Keep Request", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnCancelReq, btnKeep);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == btnCancelReq) {
            try {
                hallLeaveDAO.cancelHallLeaveRequest(req.getId(), currentUser.getUserId());
                showAlert("Request Cancelled", "Your hall leave request has been cancelled.", Alert.AlertType.INFORMATION);
                onCloseModalClicked();
                loadAllData();
            } catch (Exception e) {
                showAlert("Cancellation Failed", e.getMessage(), Alert.AlertType.ERROR);
            }
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
            System.err.println("Failed to navigate to Hall Information: " + e.getMessage());
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
    private void onHallChangeClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_HALL_CHANGE_FXML, SceneManager.STUDENT_HALL_CHANGE_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to navigate to Hall Change: " + e.getMessage());
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
    private void onBackClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.goBack(stage);
        } catch (Exception e) {
            System.err.println("[StudentHallLeave] Error on back: " + e.getMessage());
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
