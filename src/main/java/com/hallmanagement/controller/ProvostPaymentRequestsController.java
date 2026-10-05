package com.hallmanagement.controller;

import com.hallmanagement.dao.BillingDAO;
import com.hallmanagement.dao.HallDAO;
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
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProvostPaymentRequestsController implements Initializable {

    private final BillingDAO billingDAO = new BillingDAO();
    private final HallDAO hallDAO = new HallDAO();

    private User currentUser;
    private Hall assignedHall;

    @FXML private Label lblSidebarUser;
    @FXML private Label lblSidebarRole;
    @FXML private Button btnLogout;

    @FXML private Label lblAssignedHall;
    @FXML private Label lblProvostName;
    @FXML private Label lblPendingCount;
    @FXML private Label lblApprovedCount;
    @FXML private Label lblApprovedAmountTotal;

    @FXML private TabPane tabPanePayments;

    @FXML private TableView<PaymentRequest> tablePendingRequests;
    @FXML private TableColumn<PaymentRequest, String> colReqCode;
    @FXML private TableColumn<PaymentRequest, String> colStudentName;
    @FXML private TableColumn<PaymentRequest, String> colStudentRoll;
    @FXML private TableColumn<PaymentRequest, String> colStudentDept;
    @FXML private TableColumn<PaymentRequest, String> colRoomSeat;
    @FXML private TableColumn<PaymentRequest, String> colBillingPeriod;
    @FXML private TableColumn<PaymentRequest, String> colAmount;
    @FXML private TableColumn<PaymentRequest, String> colRequestDate;
    @FXML private TableColumn<PaymentRequest, String> colStatus;
    @FXML private Button btnNavPaymentRequests;
    @FXML private Button btnNavHallChangeRequests;
    @FXML private Button btnNavRoomChangeRequests;
    @FXML private Button btnNavComplaints;
    @FXML private Button btnNavHallLeaveRequests;

    @FXML private Button btnApproveSelected;
    @FXML private Button btnRejectSelected;
    @FXML private Button btnViewDetails;

    @FXML private TextField txtSearchHistory;
    @FXML private TableView<PaymentRecord> tablePaymentHistory;
    @FXML private TableColumn<PaymentRecord, String> colHistCode;
    @FXML private TableColumn<PaymentRecord, String> colHistStudentName;
    @FXML private TableColumn<PaymentRecord, String> colHistRoll;
    @FXML private TableColumn<PaymentRecord, String> colHistResidence;
    @FXML private TableColumn<PaymentRecord, String> colHistPeriod;
    @FXML private TableColumn<PaymentRecord, String> colHistAmount;
    @FXML private TableColumn<PaymentRecord, String> colHistDate;
    @FXML private TableColumn<PaymentRecord, String> colHistStatus;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblSidebarUser.setText("ID: " + currentUser.getUserId());
            lblSidebarRole.setText(currentUser.getName() + " (Provost)");
            lblProvostName.setText(currentUser.getName() + " (" + currentUser.getUserId() + ")");
            BadgeHelper.updateAllProvostBadges(btnNavPaymentRequests, btnNavHallChangeRequests, btnNavRoomChangeRequests, btnNavComplaints, btnNavHallLeaveRequests, currentUser.getUserId());
        }

        setupTableColumns();
        loadAllData();
    }

    private void setupTableColumns() {

        colReqCode.setCellValueFactory(new PropertyValueFactory<>("requestCode"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colStudentRoll.setCellValueFactory(new PropertyValueFactory<>("studentRoll"));
        colStudentDept.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getStudentDept() != null ? cell.getValue().getStudentDept() : "N/A"
        ));
        colRoomSeat.setCellValueFactory(cell -> new SimpleStringProperty(
                "Rm " + cell.getValue().getRoomNumber() + ", St " + String.format("%02d", cell.getValue().getSeatNumber())
        ));
        colBillingPeriod.setCellValueFactory(new PropertyValueFactory<>("billingPeriod"));
        colAmount.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedAmount()));
        colRequestDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedDate()));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #fbc02d; -fx-font-weight: bold;");
                }
            }
        });

        colHistCode.setCellValueFactory(new PropertyValueFactory<>("paymentCode"));
        colHistStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colHistRoll.setCellValueFactory(new PropertyValueFactory<>("studentRoll"));
        colHistResidence.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getResidenceDisplay()));
        colHistPeriod.setCellValueFactory(new PropertyValueFactory<>("billingPeriod"));
        colHistAmount.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedAmount()));
        colHistDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedDate()));
        colHistStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colHistStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void loadAllData() {
        if (currentUser == null) return;

        try {
            assignedHall = hallDAO.getHallByProvostUserId(currentUser.getUserId());
            if (assignedHall != null) {
                lblAssignedHall.setText(assignedHall.getHallName());
            } else {
                lblAssignedHall.setText("General Administrative Access");
            }

            loadPendingRequests();
            loadPaymentHistory();
            BadgeHelper.updateProvostPaymentBadge(btnNavPaymentRequests, currentUser.getUserId());
            BadgeHelper.updateProvostHallChangeBadge(btnNavHallChangeRequests, currentUser.getUserId());

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load payment records: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadPendingRequests() throws SQLException {
        List<PaymentRequest> pending = billingDAO.getPendingPaymentRequestsForProvost(currentUser.getUserId());
        tablePendingRequests.setItems(FXCollections.observableArrayList(pending));
        lblPendingCount.setText(String.valueOf(pending.size()));
    }

    private void loadPaymentHistory() throws SQLException {
        String keyword = (txtSearchHistory != null) ? txtSearchHistory.getText() : "";
        List<PaymentRecord> history = billingDAO.getPaymentHistoryForProvost(currentUser.getUserId(), keyword);
        tablePaymentHistory.setItems(FXCollections.observableArrayList(history));
        lblApprovedCount.setText(String.valueOf(history.size()));

        double totalConfirmed = 0.0;
        for (PaymentRecord r : history) {
            totalConfirmed += r.getAmount();
        }
        lblApprovedAmountTotal.setText(String.format("Total: ৳%.2f Confirmed", totalConfirmed));
    }

    @FXML
    private void onSearchHistoryChanged() {
        try {
            loadPaymentHistory();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onViewDetailsClicked() {
        PaymentRequest selected = tablePendingRequests.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a payment request from the table to view details.", Alert.AlertType.WARNING);
            return;
        }

        String details = "=== STUDENT & RESIDENCE DETAILS ===\n" +
                "Student Name : " + selected.getStudentName() + "\n" +
                "Student ID   : " + selected.getStudentId() + "\n" +
                "Roll Number  : " + selected.getStudentRoll() + "\n" +
                "Department   : " + (selected.getStudentDept() != null ? selected.getStudentDept() : "N/A") + "\n" +
                "Hall         : " + selected.getHallName() + "\n" +
                "Room Number  : " + selected.getRoomNumber() + "\n" +
                "Seat Number  : " + String.format("%02d", selected.getSeatNumber()) + "\n\n" +
                "=== BILL & DUE DETAILS ===\n" +
                "Billing Period: " + selected.getBillingPeriod() + "\n" +
                "Total Bill   : " + selected.getFormattedTotalBill() + "\n" +
                "Previously Paid: " + selected.getFormattedPrevPaid() + "\n" +
                "Current Due  : " + selected.getFormattedCurrentDue() + "\n\n" +
                "=== PAYMENT REQUEST ===\n" +
                "Request ID   : " + selected.getRequestCode() + "\n" +
                "Requested Pay: " + selected.getFormattedAmount() + "\n" +
                "Request Date : " + selected.getFormattedDate() + "\n" +
                "Status       : " + selected.getStatus();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Payment Request Details — " + selected.getRequestCode());
        alert.setHeaderText("Verification Details for Physical Payment");
        alert.setContentText(details);
        alert.showAndWait();
    }

    @FXML
    private void onApproveSelectedClicked() {
        PaymentRequest selected = tablePendingRequests.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a pending payment request from the table to approve.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Payment Approval");
        confirm.setHeaderText("Verify Physical Cash Payment Receipt");
        confirm.setContentText("Confirm physical cash payment receipt?\n\n" +
                "Student: " + selected.getStudentName() + " (" + selected.getStudentRoll() + ")\n" +
                "Hall: " + selected.getHallName() + "\n" +
                "Room & Seat: Room " + selected.getRoomNumber() + ", Seat " + String.format("%02d", selected.getSeatNumber()) + "\n" +
                "Billing Period: " + selected.getBillingPeriod() + "\n" +
                "Payment Amount: " + selected.getFormattedAmount() + "\n\n" +
                "Click OK to record the payment and update student due.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                billingDAO.approvePaymentRequest(selected.getId(), currentUser.getUserId());
                showAlert("Payment Approved",
                        "Payment request " + selected.getRequestCode() + " approved successfully.\n" +
                        "Amount: " + selected.getFormattedAmount() + "\n" +
                        "Payment history record created and student's due updated.",
                        Alert.AlertType.INFORMATION);

                loadAllData();
            } catch (Exception e) {
                showAlert("Approval Failed", "Failed to approve payment: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void onRejectSelectedClicked() {
        PaymentRequest selected = tablePendingRequests.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a pending payment request to reject.", Alert.AlertType.WARNING);
            return;
        }

        TextInputDialog dialog = new TextInputDialog("Payment not received in cash");
        dialog.setTitle("Reject Payment Request");
        dialog.setHeaderText("Reject request " + selected.getRequestCode() + " for " + selected.getStudentName());
        dialog.setContentText("Please provide the rejection reason:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String reason = result.get().trim();
            if (reason.isEmpty()) reason = "Physical payment not verified by Provost.";

            try {
                billingDAO.rejectPaymentRequest(selected.getId(), currentUser.getUserId(), reason);
                showAlert("Request Rejected", "Payment request " + selected.getRequestCode() + " has been rejected.\nReason: " + reason, Alert.AlertType.INFORMATION);
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
    private void onNoticeBoardClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.NOTICE_BOARD_FXML, SceneManager.NOTICE_BOARD_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to navigate to Notice Board: " + e.getMessage());
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
            System.err.println("[ProvostPaymentRequests] Error on back: " + e.getMessage());
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
