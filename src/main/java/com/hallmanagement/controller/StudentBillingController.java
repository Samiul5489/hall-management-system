package com.hallmanagement.controller;

import com.hallmanagement.dao.BillingDAO;
import com.hallmanagement.dao.StudentDAO;
import com.hallmanagement.model.*;
import com.hallmanagement.util.BadgeHelper;
import com.hallmanagement.util.SceneManager;
import com.hallmanagement.util.SessionManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class StudentBillingController implements Initializable {

    private final BillingDAO billingDAO = new BillingDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    private User currentUser;
    private Student currentStudent;
    private Bill currentBill;

    @FXML private Label lblSidebarUser;
    @FXML private Label lblSidebarRole;
    @FXML private Button btnLogout;

    @FXML private Label lblBreadcrumb;
    @FXML private Label lblResidenceInfo;
    @FXML private Label lblStudentMeta;
    @FXML private Label lblTotalBill;
    @FXML private Label lblBillingPeriod;
    @FXML private Label lblPaidAmount;
    @FXML private Label lblDueAmount;
    @FXML private Label lblDueSubtext;
    @FXML private Label lblStatusBadge;

    @FXML private Label lblHallCharge;
    @FXML private Label lblElectricityCharge;
    @FXML private Label lblWaterCharge;
    @FXML private Label lblMaintenanceCharge;
    @FXML private Label lblOtherCharge;
    @FXML private Label lblBreakdownPeriod;
    @FXML private Button btnOpenPaymentRequest;

    @FXML private VBox boxPaymentRequestForm;
    @FXML private Label lblReqStudentDetails;
    @FXML private Label lblReqResidence;
    @FXML private Label lblReqPeriod;
    @FXML private Label lblReqDue;
    @FXML private ComboBox<ProvostInfo> cmbResponsibleProvost;
    @FXML private TextField txtPaymentAmount;
    @FXML private Label lblRequestFeedback;
    @FXML private Button btnSubmitPaymentRequest;

    @FXML private Label lblRequestCount;
    @FXML private TableView<PaymentRequest> tablePaymentRequests;
    @FXML private TableColumn<PaymentRequest, String> colReqCode;
    @FXML private TableColumn<PaymentRequest, String> colReqPeriod;
    @FXML private TableColumn<PaymentRequest, String> colReqAmount;
    @FXML private TableColumn<PaymentRequest, String> colReqProvost;
    @FXML private TableColumn<PaymentRequest, String> colReqDate;
    @FXML private TableColumn<PaymentRequest, String> colReqStatus;
    @FXML private TableColumn<PaymentRequest, String> colReqReason;

    @FXML private Label lblHistoryCount;
    @FXML private TableView<PaymentRecord> tablePaymentHistory;
    @FXML private TableColumn<PaymentRecord, String> colPayCode;
    @FXML private TableColumn<PaymentRecord, String> colPayPeriod;
    @FXML private TableColumn<PaymentRecord, String> colPayAmount;
    @FXML private TableColumn<PaymentRecord, String> colPayDate;
    @FXML private TableColumn<PaymentRecord, String> colPayProvost;
    @FXML private TableColumn<PaymentRecord, String> colPayResidence;
    @FXML private TableColumn<PaymentRecord, String> colPayStatus;
    @FXML private Button btnNavNoticeBoard;
    @FXML private Button btnNavBilling;
    @FXML private Button btnNavHallChange;
    @FXML private Button btnNavRoomChange;
    @FXML private Button btnNavComplaints;
    @FXML private Button btnNavHallLeave;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblSidebarUser.setText("ID: " + currentUser.getUserId());
            lblSidebarRole.setText(currentUser.getName() + " (Student)");
            BadgeHelper.updateAllStudentBadges(btnNavNoticeBoard, btnNavBilling, btnNavHallChange, btnNavRoomChange, btnNavComplaints, btnNavHallLeave, currentUser.getUserId());
        }

        setupTableColumns();
        loadAllData();
    }

    private void setupTableColumns() {

        colReqCode.setCellValueFactory(new PropertyValueFactory<>("requestCode"));
        colReqPeriod.setCellValueFactory(new PropertyValueFactory<>("billingPeriod"));
        colReqAmount.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedAmount()));
        colReqProvost.setCellValueFactory(new PropertyValueFactory<>("provostName"));
        colReqDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedDate()));
        colReqStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colReqReason.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getRejectionReason() != null ? cell.getValue().getRejectionReason() : "-"
        ));

        colReqStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("APPROVED".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");
                    } else if ("REJECTED".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #ef5350; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #fbc02d; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colPayCode.setCellValueFactory(new PropertyValueFactory<>("paymentCode"));
        colPayPeriod.setCellValueFactory(new PropertyValueFactory<>("billingPeriod"));
        colPayAmount.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedAmount()));
        colPayDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedDate()));
        colPayProvost.setCellValueFactory(new PropertyValueFactory<>("provostName"));
        colPayResidence.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getResidenceDisplay()));
        colPayStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colPayStatus.setCellFactory(col -> new TableCell<>() {
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
            currentStudent = studentDAO.getStudentByUserId(currentUser.getUserId());
            currentBill = billingDAO.getCurrentBillForStudent(currentUser.getUserId());

            updateOverviewUI();
            loadPaymentRequests();
            loadPaymentHistory();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load billing records: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateOverviewUI() {
        if (currentStudent != null && currentStudent.getCurrentHall() != null) {
            lblResidenceInfo.setText(currentStudent.getCurrentHall() + "\nRoom " + currentStudent.getCurrentRoom() + ", Seat " + String.format("%02d", currentStudent.getCurrentSeat()));
            lblStudentMeta.setText(currentStudent.getName() + " (" + currentStudent.getUserId() + ") | " + currentStudent.getDepartment());
        } else {
            lblResidenceInfo.setText("Non-Residential / No Active Hall");
            lblStudentMeta.setText(currentUser != null ? currentUser.getName() : "");
        }

        if (currentBill != null) {
            lblTotalBill.setText(currentBill.getFormattedTotal());
            lblBillingPeriod.setText("Period: " + currentBill.getBillingPeriod());
            lblPaidAmount.setText(currentBill.getFormattedPaid());
            lblDueAmount.setText(currentBill.getFormattedDue());

            String status = currentBill.getStatus();
            lblStatusBadge.setText(status);
            if ("PAID".equalsIgnoreCase(status)) {
                lblStatusBadge.setStyle("-fx-background-color: rgba(74,222,128,0.2); -fx-text-fill: #4ade80; -fx-border-color: #4ade80;");
                lblDueSubtext.setText("All dues cleared for this period");
                btnOpenPaymentRequest.setDisable(true);
            } else if ("PARTIALLY PAID".equalsIgnoreCase(status)) {
                lblStatusBadge.setStyle("-fx-background-color: rgba(244,180,0,0.2); -fx-text-fill: #fbc02d; -fx-border-color: #fbc02d;");
                lblDueSubtext.setText("Partial balance payable");
                btnOpenPaymentRequest.setDisable(false);
            } else {
                lblStatusBadge.setStyle("-fx-background-color: rgba(239,83,80,0.2); -fx-text-fill: #ef5350; -fx-border-color: #ef5350;");
                lblDueSubtext.setText("Payable to Provost/Authority");
                btnOpenPaymentRequest.setDisable(false);
            }

            lblHallCharge.setText(String.format("৳%.2f", currentBill.getHallCharge()));
            lblElectricityCharge.setText(String.format("৳%.2f", currentBill.getElectricityCharge()));
            lblWaterCharge.setText(String.format("৳%.2f", currentBill.getWaterCharge()));
            lblMaintenanceCharge.setText(String.format("৳%.2f", currentBill.getMaintenanceCharge()));
            lblOtherCharge.setText(String.format("৳%.2f", currentBill.getOtherCharge()));
            lblBreakdownPeriod.setText(currentBill.getBillingPeriod());

        } else {
            lblTotalBill.setText("৳0.00");
            lblBillingPeriod.setText("No active bill");
            lblPaidAmount.setText("৳0.00");
            lblDueAmount.setText("৳0.00");
            lblStatusBadge.setText("N/A");
            btnOpenPaymentRequest.setDisable(true);
        }
    }

    private void loadPaymentRequests() throws SQLException {
        List<PaymentRequest> requests = billingDAO.getPaymentRequestsByStudent(currentUser.getUserId());
        tablePaymentRequests.setItems(FXCollections.observableArrayList(requests));
        lblRequestCount.setText(requests.size() + " Request" + (requests.size() != 1 ? "s" : ""));
    }

    private void loadPaymentHistory() throws SQLException {
        List<PaymentRecord> history = billingDAO.getPaymentHistoryForStudent(currentUser.getUserId());
        tablePaymentHistory.setItems(FXCollections.observableArrayList(history));
        lblHistoryCount.setText(history.size() + " Payment" + (history.size() != 1 ? "s" : ""));
    }

    @FXML
    private void onOpenPaymentRequestClicked() {
        if (currentBill == null || currentBill.getDueAmount() <= 0.001) {
            showAlert("No Due", "You do not have any pending due amount to pay.", Alert.AlertType.INFORMATION);
            return;
        }

        lblReqStudentDetails.setText(currentUser.getName() + " (Roll: " + currentUser.getUserId() + ", Dept: " + (currentStudent != null ? currentStudent.getDepartment() : "N/A") + ")");
        lblReqResidence.setText(currentBill.getHallName() + " | Room " + currentBill.getRoomNumber() + " | Seat " + String.format("%02d", currentBill.getSeatNumber()));
        lblReqPeriod.setText(currentBill.getBillingPeriod());
        lblReqDue.setText(currentBill.getFormattedDue());

        txtPaymentAmount.setText(String.format("%.2f", currentBill.getDueAmount()));
        lblRequestFeedback.setText("");

        try {
            List<ProvostInfo> provosts = billingDAO.getProvostsForHall(currentBill.getHallId());
            ObservableList<ProvostInfo> provostList = FXCollections.observableArrayList(provosts);
            cmbResponsibleProvost.setItems(provostList);

            if (!provosts.isEmpty()) {
                cmbResponsibleProvost.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        boxPaymentRequestForm.setVisible(true);
        boxPaymentRequestForm.setManaged(true);
    }

    @FXML
    private void onClosePaymentRequestClicked() {
        boxPaymentRequestForm.setVisible(false);
        boxPaymentRequestForm.setManaged(false);
    }

    @FXML
    private void onSubmitPaymentRequestClicked() {
        lblRequestFeedback.setText("");

        ProvostInfo selectedProvost = cmbResponsibleProvost.getValue();
        if (selectedProvost == null) {
            lblRequestFeedback.setText("Please select the responsible Provost.");
            return;
        }

        String amountText = txtPaymentAmount.getText().trim();
        if (amountText.isEmpty()) {
            lblRequestFeedback.setText("Please enter the payment amount.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            lblRequestFeedback.setText("Please enter a valid numeric amount.");
            return;
        }

        if (amount <= 0) {
            lblRequestFeedback.setText("Payment amount must be greater than zero.");
            return;
        }

        if (currentBill != null && amount > currentBill.getDueAmount() + 0.001) {
            lblRequestFeedback.setText(String.format("Payment amount (৳%.2f) cannot exceed current due (৳%.2f).", amount, currentBill.getDueAmount()));
            return;
        }

        try {
            PaymentRequest pr = billingDAO.createPaymentRequest(currentUser.getUserId(), selectedProvost.getUserId(), currentBill.getId(), amount);

            showAlert("Payment Request Submitted",
                    "Payment request " + pr.getRequestCode() + " submitted successfully.\n\n" +
                    "Status: PENDING\n" +
                    "Amount: " + pr.getFormattedAmount() + "\n" +
                    "Assigned Provost: " + selectedProvost.getName() + "\n\n" +
                    "Please wait for the Provost to verify and approve the physical payment.",
                    Alert.AlertType.INFORMATION);

            onClosePaymentRequestClicked();
            loadAllData();

        } catch (Exception e) {
            lblRequestFeedback.setText("Error submitting request: " + e.getMessage());
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
            System.err.println("Failed to navigate to Dashboard: " + e.getMessage());
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
            System.err.println("[StudentBilling] Error on back: " + e.getMessage());
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
