package com.hallmanagement.controller;

import com.hallmanagement.dao.RoomChangeDAO;
import com.hallmanagement.model.RoomChangeRequest;
import com.hallmanagement.model.User;
import com.hallmanagement.util.BadgeHelper;
import com.hallmanagement.util.SceneManager;
import com.hallmanagement.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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

public class ProvostRoomChangeRequestsController implements Initializable {

    private final RoomChangeDAO roomChangeDAO = new RoomChangeDAO();
    private User currentUser;
    private RoomChangeRequest selectedRequest;

    @FXML private Label lblSidebarUser;
    @FXML private Label lblSidebarRole;
    @FXML private Button btnLogout;
    @FXML private Button btnNavPaymentRequests;
    @FXML private Button btnNavHallChangeRequests;
    @FXML private Button btnNavRoomChangeRequests;
    @FXML private Button btnNavComplaints;
    @FXML private Button btnNavHallLeaveRequests;
    @FXML private Button btnNavNoticeBoard;

    @FXML private Label lblPendingCount;
    @FXML private Label lblProcessedCount;
    @FXML private TextField txtSearch;
    @FXML private Label lblTableSummary;

    @FXML private TableView<RoomChangeRequest> tableRequests;
    @FXML private TableColumn<RoomChangeRequest, String> colSerial;
    @FXML private TableColumn<RoomChangeRequest, String> colDate;
    @FXML private TableColumn<RoomChangeRequest, String> colStudentName;
    @FXML private TableColumn<RoomChangeRequest, String> colRoll;
    @FXML private TableColumn<RoomChangeRequest, String> colDept;
    @FXML private TableColumn<RoomChangeRequest, String> colCurRoom;
    @FXML private TableColumn<RoomChangeRequest, String> colCurSeat;
    @FXML private TableColumn<RoomChangeRequest, String> colReqRoom;
    @FXML private TableColumn<RoomChangeRequest, String> colReqSeat;
    @FXML private TableColumn<RoomChangeRequest, String> colStatus;
    @FXML private TableColumn<RoomChangeRequest, Void> colAction;

    @FXML private VBox boxReviewModal;
    @FXML private Label lblModalStatus;
    @FXML private Label lblModalStudentName;
    @FXML private Label lblModalStudentId;
    @FXML private Label lblModalStudentDept;
    @FXML private Label lblModalReqDate;
    @FXML private Label lblModalCurResidence;
    @FXML private Label lblModalReqResidence;
    @FXML private Label lblModalReason;
    @FXML private VBox boxModalProcessedInfo;
    @FXML private Label lblModalProcessedBy;
    @FXML private Label lblModalProcessedDate;
    @FXML private VBox boxModalRejectionReason;
    @FXML private Label lblModalRejectionReason;
    @FXML private Button btnModalApprove;
    @FXML private Button btnModalReject;

    @FXML private VBox boxRejectModal;
    @FXML private Label lblRejectTargetPrompt;
    @FXML private TextArea txtRejectionReason;
    @FXML private Label lblRejectFeedback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblSidebarUser.setText("ID: " + currentUser.getUserId());
            lblSidebarRole.setText(currentUser.getName() + " (Provost)");
        }

        setupTableColumns();
        loadAllData();
    }

    private void setupTableColumns() {
        colSerial.setCellValueFactory(new PropertyValueFactory<>("serial"));
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedRequestDate()));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colRoll.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colDept.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStudentDept() != null ? cell.getValue().getStudentDept() : "RUET"));
        colCurRoom.setCellValueFactory(cell -> new SimpleStringProperty("Room " + cell.getValue().getCurrentRoom()));
        colCurSeat.setCellValueFactory(cell -> new SimpleStringProperty("Seat " + String.format("%02d", cell.getValue().getCurrentSeat())));
        colReqRoom.setCellValueFactory(cell -> new SimpleStringProperty("Room " + cell.getValue().getRequestedRoom()));
        colReqSeat.setCellValueFactory(cell -> new SimpleStringProperty("Seat " + String.format("%02d", cell.getValue().getRequestedSeat())));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

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

        Label lblActionHeader = new Label("Actions");
        lblActionHeader.setStyle("-fx-text-fill: #90caf9; -fx-font-weight: bold; -fx-font-size: 12.5px; -fx-alignment: CENTER;");
        lblActionHeader.setMaxWidth(Double.MAX_VALUE);
        lblActionHeader.setAlignment(Pos.CENTER);
        colAction.setGraphic(lblActionHeader);
        colAction.setText(null);

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnReview = new Button("Review");
            private final Button btnApprove = new Button("Approve");
            private final Button btnReject = new Button("Reject");
            private final HBox container = new HBox(6, btnReview, btnApprove, btnReject);

            {
                container.setAlignment(Pos.CENTER);
                btnReview.setStyle("-fx-background-color: rgba(26,115,232,0.22); -fx-text-fill: #90caf9; -fx-border-color: rgba(26,115,232,0.6); -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 10 4 10;");
                btnApprove.setStyle("-fx-background-color: rgba(34,197,94,0.22); -fx-text-fill: #4ade80; -fx-border-color: rgba(34,197,94,0.6); -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 10 4 10;");
                btnReject.setStyle("-fx-background-color: rgba(239,68,68,0.22); -fx-text-fill: #f87171; -fx-border-color: rgba(239,68,68,0.6); -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 10 4 10;");

                btnReview.setOnAction(event -> {
                    RoomChangeRequest req = getTableView().getItems().get(getIndex());
                    openReviewModal(req);
                });

                btnApprove.setOnAction(event -> {
                    RoomChangeRequest req = getTableView().getItems().get(getIndex());
                    approveRequest(req);
                });

                btnReject.setOnAction(event -> {
                    RoomChangeRequest req = getTableView().getItems().get(getIndex());
                    openRejectModal(req);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    RoomChangeRequest req = getTableView().getItems().get(getIndex());
                    if (req != null && req.isPending()) {
                        btnApprove.setVisible(true);
                        btnApprove.setManaged(true);
                        btnReject.setVisible(true);
                        btnReject.setManaged(true);
                    } else {
                        btnApprove.setVisible(false);
                        btnApprove.setManaged(false);
                        btnReject.setVisible(false);
                        btnReject.setManaged(false);
                    }
                    setGraphic(container);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
    }

    private void loadAllData() {
        if (currentUser == null) return;

        String keyword = txtSearch != null ? txtSearch.getText() : null;

        try {
            List<RoomChangeRequest> list = roomChangeDAO.getRequestsForProvost(currentUser.getUserId(), keyword);
            tableRequests.setItems(FXCollections.observableArrayList(list));
            lblTableSummary.setText("Showing " + list.size() + " request(s)");

            int pending = 0;
            int processed = 0;
            for (RoomChangeRequest r : list) {
                if (r.isPending()) pending++;
                else processed++;
            }

            lblPendingCount.setText(String.valueOf(pending));
            lblProcessedCount.setText(String.valueOf(processed));

            updateSidebarBadges();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load Room Change requests: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateSidebarBadges() {
        BadgeHelper.updateAllProvostBadges(
                btnNavPaymentRequests, btnNavHallChangeRequests,
                btnNavRoomChangeRequests, btnNavComplaints,
                btnNavHallLeaveRequests, currentUser.getUserId()
        );
    }

    @FXML
    private void onSearchKeyReleased() {
        loadAllData();
    }

    @FXML
    private void onRefreshClicked() {
        loadAllData();
    }

    private void approveRequest(RoomChangeRequest req) {
        if (req == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Approve Room/Seat Change");
        confirm.setHeaderText("Approve Transfer for " + req.getStudentName() + " (" + req.getStudentId() + ")?");
        confirm.setContentText(
                "Request Details:\n" +
                "  • Student: " + req.getStudentName() + " (ID: " + req.getStudentId() + ")\n" +
                "  • Current: " + req.getCurrentResidenceDisplay() + "\n" +
                "  • New Desired: " + req.getRequestedResidenceDisplay() + "\n\n" +
                "Executing this approval will atomically:\n" +
                "  1. Re-verify student Due == 0\n" +
                "  2. Release old Room " + req.getCurrentRoom() + " (Seat " + req.getCurrentSeat() + ") to AVAILABLE\n" +
                "  3. Assign new Room " + req.getRequestedRoom() + " (Seat " + req.getRequestedSeat() + ") as OCCUPIED\n" +
                "  4. Update student profile and hall residence history\n\n" +
                "Proceed with approval?"
        );

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                roomChangeDAO.approveRoomChangeRequest(req.getId(), currentUser.getUserId());
                showAlert("Request Approved", "Room/Seat change request " + req.getRequestCode() + " approved successfully!\n\nStudent has been transferred to Room " + req.getRequestedRoom() + " (Seat " + req.getRequestedSeat() + ").", Alert.AlertType.INFORMATION);
                closeAllModals();
                loadAllData();
            } catch (Exception ex) {
                showAlert("Approval Error", ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void openRejectModal(RoomChangeRequest req) {
        if (req == null) return;
        this.selectedRequest = req;

        lblRejectTargetPrompt.setText("Rejecting Application " + req.getRequestCode() + " for " + req.getStudentName() + " (ID: " + req.getStudentId() + "):");
        txtRejectionReason.clear();
        lblRejectFeedback.setText("");

        boxRejectModal.setVisible(true);
        boxRejectModal.setManaged(true);
        boxReviewModal.setVisible(false);
        boxReviewModal.setManaged(false);
    }

    @FXML
    private void onConfirmRejectClicked() {
        if (selectedRequest == null) return;

        String reason = txtRejectionReason.getText();
        if (reason == null || reason.trim().isEmpty()) {
            lblRejectFeedback.setText("Rejection reason is mandatory. Please enter a valid reason.");
            return;
        }

        try {
            roomChangeDAO.rejectRoomChangeRequest(selectedRequest.getId(), currentUser.getUserId(), reason.trim());
            showAlert("Request Rejected", "Room/Seat change request " + selectedRequest.getRequestCode() + " has been rejected.\n\nRejection reason recorded for student review.", Alert.AlertType.INFORMATION);
            closeAllModals();
            loadAllData();
        } catch (Exception ex) {
            lblRejectFeedback.setText(ex.getMessage());
        }
    }

    @FXML
    private void onCloseRejectModalClicked() {
        boxRejectModal.setVisible(false);
        boxRejectModal.setManaged(false);
    }

    private void openReviewModal(RoomChangeRequest req) {
        if (req == null) return;
        this.selectedRequest = req;

        lblModalStatus.setText(req.getStatus());
        switch (req.getStatus().toUpperCase()) {
            case "PENDING":
                lblModalStatus.setStyle("-fx-background-color: rgba(245,158,11,0.2); -fx-text-fill: #fbbf24; -fx-border-color: rgba(245,158,11,0.5);");
                btnModalApprove.setVisible(true);
                btnModalApprove.setManaged(true);
                btnModalReject.setVisible(true);
                btnModalReject.setManaged(true);
                break;
            case "APPROVED":
                lblModalStatus.setStyle("-fx-background-color: rgba(34,197,94,0.2); -fx-text-fill: #4ade80; -fx-border-color: rgba(34,197,94,0.5);");
                btnModalApprove.setVisible(false);
                btnModalApprove.setManaged(false);
                btnModalReject.setVisible(false);
                btnModalReject.setManaged(false);
                break;
            case "REJECTED":
                lblModalStatus.setStyle("-fx-background-color: rgba(239,68,68,0.2); -fx-text-fill: #f87171; -fx-border-color: rgba(239,68,68,0.5);");
                btnModalApprove.setVisible(false);
                btnModalApprove.setManaged(false);
                btnModalReject.setVisible(false);
                btnModalReject.setManaged(false);
                break;
            case "CANCELLED":
                lblModalStatus.setStyle("-fx-background-color: rgba(148,163,184,0.2); -fx-text-fill: #cbd5e1; -fx-border-color: rgba(148,163,184,0.5);");
                btnModalApprove.setVisible(false);
                btnModalApprove.setManaged(false);
                btnModalReject.setVisible(false);
                btnModalReject.setManaged(false);
                break;
        }

        lblModalStudentName.setText(req.getStudentName());
        lblModalStudentId.setText(req.getStudentId());
        lblModalStudentDept.setText(req.getStudentDept() != null ? req.getStudentDept() : "RUET");
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
        } else {
            boxModalProcessedInfo.setVisible(false);
            boxModalProcessedInfo.setManaged(false);
        }

        boxReviewModal.setVisible(true);
        boxReviewModal.setManaged(true);
        boxRejectModal.setVisible(false);
        boxRejectModal.setManaged(false);
    }

    @FXML
    private void onModalApproveClicked() {
        if (selectedRequest != null) {
            approveRequest(selectedRequest);
        }
    }

    @FXML
    private void onModalRejectClicked() {
        if (selectedRequest != null) {
            openRejectModal(selectedRequest);
        }
    }

    @FXML
    private void onCloseReviewModalClicked() {
        boxReviewModal.setVisible(false);
        boxReviewModal.setManaged(false);
    }

    private void closeAllModals() {
        boxReviewModal.setVisible(false);
        boxReviewModal.setManaged(false);
        boxRejectModal.setVisible(false);
        boxRejectModal.setManaged(false);
    }

    @FXML
    private void onDashboardClicked() {
        navigateTo(SceneManager.PROVOST_DASHBOARD_FXML, SceneManager.PROVOST_TITLE);
    }

    @FXML
    private void onProfileClicked() {
        navigateTo(SceneManager.PROVOST_PROFILE_FXML, SceneManager.PROVOST_PROFILE_TITLE);
    }

    @FXML
    private void onHallInformationClicked() {
        navigateTo(SceneManager.HALL_INFORMATION_FXML, SceneManager.HALL_INFORMATION_TITLE);
    }

    @FXML
    private void onPaymentRequestsClicked() {
        navigateTo(SceneManager.PROVOST_PAYMENT_REQUESTS_FXML, SceneManager.PROVOST_PAYMENT_REQUESTS_TITLE);
    }

    @FXML
    private void onNoticeBoardClicked() {
        navigateTo(SceneManager.NOTICE_BOARD_FXML, SceneManager.NOTICE_BOARD_TITLE);
    }

    @FXML
    private void onHallChangeRequestsClicked() {
        navigateTo(SceneManager.PROVOST_HALL_CHANGE_REQUESTS_FXML, SceneManager.PROVOST_HALL_CHANGE_TITLE);
    }

    @FXML
    private void onComplaintsClicked() {
        navigateTo(SceneManager.PROVOST_COMPLAINTS_FXML, SceneManager.PROVOST_COMPLAINTS_TITLE);
    }

    @FXML
    private void onHallLeaveRequestsClicked() {
        navigateTo(SceneManager.PROVOST_HALL_LEAVE_REQUESTS_FXML, SceneManager.PROVOST_HALL_LEAVE_REQUESTS_TITLE);
    }

    @FXML
    private void onBackClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.goBack(stage);
        } catch (Exception e) {
            System.err.println("[ProvostRoomChangeRequests] Error on back: " + e.getMessage());
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
