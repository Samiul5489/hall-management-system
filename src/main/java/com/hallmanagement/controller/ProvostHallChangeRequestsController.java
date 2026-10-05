package com.hallmanagement.controller;

import com.hallmanagement.dao.BillingDAO;
import com.hallmanagement.dao.HallChangeDAO;
import com.hallmanagement.model.HallChangeRequest;
import com.hallmanagement.model.User;
import com.hallmanagement.util.BadgeHelper;
import com.hallmanagement.util.SceneManager;
import com.hallmanagement.util.SessionManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProvostHallChangeRequestsController implements Initializable {

    private final HallChangeDAO hallChangeDAO = new HallChangeDAO();
    private final BillingDAO billingDAO = new BillingDAO();

    private User currentUser;

    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavHallInfo;
    @FXML private Button btnNavPaymentRequests;
    @FXML private Button btnNavNoticeBoard;
    @FXML private Button btnNavHallChangeRequests;
    @FXML private Button btnNavRoomChangeRequests;
    @FXML private Button btnNavComplaints;
    @FXML private Button btnNavHallLeaveRequests;
    @FXML private Label lblSidebarUser;
    @FXML private Label lblSidebarRole;
    @FXML private Button btnLogout;

    @FXML private Label lblPendingCount;
    @FXML private Label lblApprovedCount;

    @FXML private TableView<HallChangeRequest> tablePendingRequests;
    @FXML private TableColumn<HallChangeRequest, String> colPendSerial;
    @FXML private TableColumn<HallChangeRequest, String> colPendCode;
    @FXML private TableColumn<HallChangeRequest, String> colPendDate;
    @FXML private TableColumn<HallChangeRequest, String> colPendStudent;
    @FXML private TableColumn<HallChangeRequest, String> colPendRoll;
    @FXML private TableColumn<HallChangeRequest, String> colPendDept;
    @FXML private TableColumn<HallChangeRequest, String> colPendCurResidence;
    @FXML private TableColumn<HallChangeRequest, String> colPendReqResidence;

    @FXML private TextField txtSearchHistory;
    @FXML private TableView<HallChangeRequest> tableHistory;
    @FXML private TableColumn<HallChangeRequest, String> colHistSerial;
    @FXML private TableColumn<HallChangeRequest, String> colHistCode;
    @FXML private TableColumn<HallChangeRequest, String> colHistDate;
    @FXML private TableColumn<HallChangeRequest, String> colHistStudent;
    @FXML private TableColumn<HallChangeRequest, String> colHistCurResidence;
    @FXML private TableColumn<HallChangeRequest, String> colHistReqResidence;
    @FXML private TableColumn<HallChangeRequest, String> colHistStatus;
    @FXML private TableColumn<HallChangeRequest, String> colHistRemarks;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblSidebarUser.setText("ID: " + currentUser.getUserId());
            lblSidebarRole.setText(currentUser.getName() + " (Provost)");
        }

        updateBadges();
        setupTableColumns();
        loadAllData();
    }

    private void updateBadges() {
        if (currentUser != null) {
            BadgeHelper.updateAllProvostBadges(btnNavPaymentRequests, btnNavHallChangeRequests, btnNavRoomChangeRequests, btnNavComplaints, btnNavHallLeaveRequests, currentUser.getUserId());
        }
    }

    private void setupTableColumns() {

        colPendSerial.setCellValueFactory(new PropertyValueFactory<>("serial"));
        colPendCode.setCellValueFactory(new PropertyValueFactory<>("requestCode"));
        colPendDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedRequestDate()));
        colPendStudent.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colPendRoll.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colPendDept.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getStudentDept() != null ? cell.getValue().getStudentDept() : "N/A"
        ));
        colPendCurResidence.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCurrentResidenceDisplay()));
        colPendReqResidence.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRequestedResidenceDisplay()));

        colPendSerial.setCellFactory(col -> new TableCell<>() {
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

        colHistSerial.setCellValueFactory(new PropertyValueFactory<>("serial"));
        colHistCode.setCellValueFactory(new PropertyValueFactory<>("requestCode"));
        colHistDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedRequestDate()));
        colHistStudent.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getStudentName() + " (" + cell.getValue().getStudentId() + ")"
        ));
        colHistCurResidence.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCurrentResidenceDisplay()));
        colHistReqResidence.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRequestedResidenceDisplay()));
        colHistStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colHistRemarks.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getRejectionReason() != null ? cell.getValue().getRejectionReason() :
                        (cell.getValue().isApproved() ? "Approved by " + (cell.getValue().getProcessedByName() != null ? cell.getValue().getProcessedByName() : "Provost") : "Awaiting review")
        ));

        colHistStatus.setCellFactory(col -> new TableCell<>() {
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

    private void loadAllData() {
        if (currentUser == null) return;

        try {

            List<HallChangeRequest> pending = hallChangeDAO.getPendingRequestsForProvost(currentUser.getUserId());
            tablePendingRequests.setItems(FXCollections.observableArrayList(pending));
            lblPendingCount.setText(String.valueOf(pending.size()));

            loadHistoryTable();

            updateBadges();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load hall change requests: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadHistoryTable() throws SQLException {
        String keyword = (txtSearchHistory != null) ? txtSearchHistory.getText() : "";
        List<HallChangeRequest> history = hallChangeDAO.getAllRequestsForProvost(currentUser.getUserId(), keyword);
        tableHistory.setItems(FXCollections.observableArrayList(history));

        long approvedCount = history.stream().filter(HallChangeRequest::isApproved).count();
        lblApprovedCount.setText(String.valueOf(approvedCount));
    }

    @FXML
    private void onSearchHistoryChanged() {
        try {
            loadHistoryTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onViewDetailsClicked() {
        HallChangeRequest selected = tablePendingRequests.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a hall change request from the table to view details.", Alert.AlertType.WARNING);
            return;
        }

        String details =
                "=== STUDENT INFORMATION ===\n" +
                "Student Name : " + selected.getStudentName() + "\n" +
                "Student ID   : " + selected.getStudentId() + "\n" +
                "Roll Number  : " + selected.getStudentRoll() + "\n" +
                "Department   : " + (selected.getStudentDept() != null ? selected.getStudentDept() : "N/A") + "\n\n" +
                "=== CURRENT RESIDENCE ===\n" +
                "Hall         : " + selected.getCurrentHallName() + "\n" +
                "Room         : " + selected.getCurrentRoom() + "\n" +
                "Seat         : " + String.format("%02d", selected.getCurrentSeat()) + "\n\n" +
                "=== REQUESTED NEW RESIDENCE ===\n" +
                "Requested Hall: " + selected.getRequestedHallName() + "\n" +
                "Requested Room: " + selected.getRequestedRoom() + "\n" +
                "Requested Seat: " + String.format("%02d", selected.getRequestedSeat()) + "\n\n" +
                "=== VERIFICATION STATUS ===\n" +
                "Request ID   : " + selected.getRequestCode() + "\n" +
                "Request Date : " + selected.getFormattedRequestDate() + "\n" +
                "Current Due  : Tk. 0.00 (Verified)\n" +
                "Status       : " + selected.getStatus();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hall Change Request Details — " + selected.getRequestCode());
        alert.setHeaderText("Residential Transfer Verification Record");
        alert.setContentText(details);
        alert.showAndWait();
    }

    @FXML
    private void onApproveSelectedClicked() {
        HallChangeRequest selected = tablePendingRequests.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a pending hall change request to approve.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Hall Change Approval");
        confirm.setHeaderText("Confirm Seat Transfer Approval?");
        confirm.setContentText(
                "Student: " + selected.getStudentName() + " (" + selected.getStudentId() + ")\n\n" +
                "Current Residence:\n" +
                "  " + selected.getCurrentResidenceDisplay() + "\n\n" +
                "New Requested Residence:\n" +
                "  " + selected.getRequestedResidenceDisplay() + "\n\n" +
                "Due Status: Tk. 0.00\n\n" +
                "Click OK to execute the transfer and update student residence & hall live seat availability."
        );

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                hallChangeDAO.approveHallChangeRequest(selected.getId(), currentUser.getUserId());
                showAlert("Transfer Approved",
                        "Hall Change Request " + selected.getRequestCode() + " approved successfully.\n" +
                        "Student " + selected.getStudentName() + " has been transferred to " + selected.getRequestedResidenceDisplay() + ".\n" +
                        "Previous seat has been marked AVAILABLE and new seat is now OCCUPIED.",
                        Alert.AlertType.INFORMATION);

                loadAllData();
            } catch (Exception e) {
                showAlert("Approval Failed", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void onRejectSelectedClicked() {
        HallChangeRequest selected = tablePendingRequests.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a pending hall change request to reject.", Alert.AlertType.WARNING);
            return;
        }

        TextInputDialog dialog = new TextInputDialog("Requested room/block allocation quota fulfilled");
        dialog.setTitle("Reject Hall Change Request");
        dialog.setHeaderText("Reject transfer application " + selected.getRequestCode() + " for " + selected.getStudentName());
        dialog.setContentText("Please provide the rejection reason:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String reason = result.get().trim();
            if (reason.isEmpty()) reason = "Transfer request rejected by Hall Provost.";

            try {
                hallChangeDAO.rejectHallChangeRequest(selected.getId(), currentUser.getUserId(), reason);
                showAlert("Request Rejected", "Hall Change Request " + selected.getRequestCode() + " has been rejected.\nReason: " + reason, Alert.AlertType.INFORMATION);
                loadAllData();
            } catch (Exception e) {
                showAlert("Rejection Failed", "Failed to reject request: " + e.getMessage(), Alert.AlertType.ERROR);
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
            SceneManager.switchTo(stage, SceneManager.PROVOST_DASHBOARD_FXML, SceneManager.PROVOST_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to navigate to Provost Dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void onProfileClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_PROFILE_FXML, SceneManager.PROVOST_PROFILE_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to navigate to Provost Profile: " + e.getMessage());
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
    private void onNoticeBoardClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.NOTICE_BOARD_FXML, SceneManager.NOTICE_BOARD_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to navigate to Notice Board: " + e.getMessage());
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
            System.err.println("[ProvostHallChangeRequests] Error on back: " + e.getMessage());
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
