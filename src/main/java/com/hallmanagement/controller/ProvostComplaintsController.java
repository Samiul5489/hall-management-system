package com.hallmanagement.controller;

import com.hallmanagement.dao.ComplaintDAO;
import com.hallmanagement.model.Complaint;
import com.hallmanagement.model.User;
import com.hallmanagement.util.BadgeHelper;
import com.hallmanagement.util.SceneManager;
import com.hallmanagement.util.SessionManager;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ProvostComplaintsController implements Initializable {

    @FXML private Label lblSidebarUser;
    @FXML private Label lblSidebarRole;
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavProfile;
    @FXML private Button btnNavHallInfo;
    @FXML private Button btnNavPaymentRequests;
    @FXML private Button btnNavNoticeBoard;
    @FXML private Button btnNavHallChangeRequests;
    @FXML private Button btnNavRoomChangeRequests;
    @FXML private Button btnNavComplaints;
    @FXML private Button btnNavHallLeaveRequests;
    @FXML private Button btnLogout;

    @FXML private TableView<Complaint> tblComplaints;
    @FXML private TableColumn<Complaint, String> colSerial;
    @FXML private TableColumn<Complaint, String> colDate;
    @FXML private TableColumn<Complaint, String> colStudentName;
    @FXML private TableColumn<Complaint, String> colStudentId;
    @FXML private TableColumn<Complaint, String> colResidence;
    @FXML private TableColumn<Complaint, String> colTitle;
    @FXML private TableColumn<Complaint, String> colStatus;
    @FXML private TableColumn<Complaint, Complaint> colActions;
    @FXML private Label lblComplaintCount;

    @FXML private VBox boxReviewModal;
    @FXML private Label lblStudentName;
    @FXML private Label lblStudentId;
    @FXML private Label lblStudentDept;
    @FXML private Label lblStudentResidence;

    @FXML private Label lblComplaintTitle;
    @FXML private Label lblSubmittedAt;
    @FXML private Label lblStatusBadge;
    @FXML private Label lblComplaintMessage;

    @FXML private VBox boxProcessAction;
    @FXML private TextArea txtProvostReply;
    @FXML private Label lblWordCounter;
    @FXML private Label lblFeedback;

    @FXML private VBox boxProcessedResponse;
    @FXML private Label lblExistingResponse;
    @FXML private Label lblRespondedAt;

    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private User currentUser;
    private Complaint selectedComplaint;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblSidebarUser.setText("ID: " + currentUser.getUserId());
            lblSidebarRole.setText(currentUser.getName() + " (Provost)");
            BadgeHelper.updateAllProvostBadges(btnNavPaymentRequests, btnNavHallChangeRequests, btnNavRoomChangeRequests, btnNavComplaints, btnNavHallLeaveRequests, currentUser.getUserId());
        }

        setupWordCounter();
        setupTableColumns();
        loadComplaints();
    }

    private void setupWordCounter() {
        txtProvostReply.textProperty().addListener((obs, oldVal, newVal) -> {
            int words = ComplaintDAO.countWords(newVal);
            lblWordCounter.setText(words + " / 100 words");
            if (words > 100) {
                lblWordCounter.setStyle("-fx-text-fill: #ef5350; -fx-font-weight: bold;");
                lblFeedback.setText("Response exceeds 100 words (" + words + " words). Please shorten your message.");
                lblFeedback.setStyle("-fx-text-fill: #ef5350;");
            } else {
                lblWordCounter.setStyle("-fx-text-fill: #90a4ae;");
                lblFeedback.setText("");
            }
        });
    }

    private void setupTableColumns() {
        tblComplaints.setRowFactory(tv -> {
            TableRow<Complaint> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    openReviewModal(row.getItem());
                }
            });
            return row;
        });

        colSerial.setCellValueFactory(cellData -> {
            int index = tblComplaints.getItems().indexOf(cellData.getValue()) + 1;
            return new ReadOnlyObjectWrapper<>(String.format("%02d", index));
        });

        colDate.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getSubmittedAtFormatted()));
        colStudentName.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
            cellData.getValue().getStudentName() != null ? cellData.getValue().getStudentName() : "-"
        ));
        colStudentId.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getStudentId()));
        colResidence.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getResidenceDisplay()));
        colTitle.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getTitle()));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Complaint c = getTableRow().getItem();
                    Label badge = new Label(c.getStatus());
                    badge.getStyleClass().add("seat-badge");

                    if (c.isPending()) {
                        badge.setStyle("-fx-background-color: rgba(245,158,11,0.18); -fx-text-fill: #f59e0b; -fx-border-color: rgba(245,158,11,0.4); -fx-border-radius: 12; -fx-border-width: 1; -fx-font-weight: bold;");
                    } else if (c.isApproved()) {
                        badge.setStyle("-fx-background-color: rgba(74,222,128,0.18); -fx-text-fill: #4ade80; -fx-border-color: rgba(74,222,128,0.4); -fx-border-radius: 12; -fx-border-width: 1; -fx-font-weight: bold;");
                    } else if (c.isRejected()) {
                        badge.setStyle("-fx-background-color: rgba(239,83,80,0.18); -fx-text-fill: #ef5350; -fx-border-color: rgba(239,83,80,0.4); -fx-border-radius: 12; -fx-border-width: 1; -fx-font-weight: bold;");
                    } else {
                        badge.setStyle("-fx-background-color: rgba(144,164,174,0.18); -fx-text-fill: #90a4ae; -fx-border-color: rgba(144,164,174,0.4); -fx-border-radius: 12; -fx-border-width: 1; -fx-font-weight: bold;");
                    }

                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        Label lblActionHeader = new Label("Action");
        lblActionHeader.setStyle("-fx-text-fill: #90caf9; -fx-font-weight: bold; -fx-font-size: 12.5px; -fx-alignment: CENTER;");
        lblActionHeader.setMaxWidth(Double.MAX_VALUE);
        lblActionHeader.setAlignment(Pos.CENTER);
        colActions.setGraphic(lblActionHeader);
        colActions.setText(null);

        colActions.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue()));
        colActions.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Complaint c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) {
                    setGraphic(null);
                } else {
                    Button btnAction = new Button(c.isPending() ? "🔍 Review & Respond" : "👁 View Details");
                    if (c.isPending()) {
                        btnAction.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 4 10 4 10;");
                    } else {
                        btnAction.setStyle("-fx-background-color: rgba(26,115,232,0.15); -fx-text-fill: #90caf9; -fx-border-color: rgba(26,115,232,0.4); -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 10 4 10;");
                    }
                    btnAction.setOnAction(e -> openReviewModal(c));
                    setGraphic(btnAction);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void loadComplaints() {
        if (currentUser == null) return;
        try {
            List<Complaint> list = complaintDAO.getComplaintsByProvost(currentUser.getUserId());
            tblComplaints.setItems(FXCollections.observableArrayList(list));
            lblComplaintCount.setText(list.size() + " total complaint(s)");
            BadgeHelper.updateAllProvostBadges(btnNavPaymentRequests, btnNavHallChangeRequests, btnNavComplaints, currentUser.getUserId());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load provost complaints: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openReviewModal(Complaint c) {
        if (c == null) return;
        this.selectedComplaint = c;

        lblStudentName.setText("Student: " + (c.getStudentName() != null ? c.getStudentName() : "-"));
        lblStudentId.setText("ID / Roll: " + c.getStudentId());
        lblStudentDept.setText("Dept: " + (c.getStudentDepartment() != null ? c.getStudentDepartment() : "-"));
        lblStudentResidence.setText("Residence: " + c.getResidenceDisplay());

        lblComplaintTitle.setText(c.getTitle());
        lblSubmittedAt.setText(c.getSubmittedAtFullFormatted());
        lblStatusBadge.setText(c.getStatus());
        lblComplaintMessage.setText(c.getMessage());

        txtProvostReply.clear();
        lblFeedback.setText("");

        if (c.isPending()) {
            boxProcessAction.setVisible(true);
            boxProcessAction.setManaged(true);
            boxProcessedResponse.setVisible(false);
            boxProcessedResponse.setManaged(false);
        } else if (c.isProcessed()) {
            boxProcessAction.setVisible(false);
            boxProcessAction.setManaged(false);
            boxProcessedResponse.setVisible(true);
            boxProcessedResponse.setManaged(true);
            lblExistingResponse.setText(c.getResponseMessage() != null ? c.getResponseMessage() : "-");
            lblRespondedAt.setText(c.getRespondedAtFormatted());
        } else {
            boxProcessAction.setVisible(false);
            boxProcessAction.setManaged(false);
            boxProcessedResponse.setVisible(true);
            boxProcessedResponse.setManaged(true);
            lblExistingResponse.setText("This complaint was cancelled by the student and is read-only.");
            lblRespondedAt.setText("");
        }

        boxReviewModal.setVisible(true);
        boxReviewModal.setManaged(true);
    }

    @FXML
    private void onCloseReviewModalClicked() {
        selectedComplaint = null;
        boxReviewModal.setVisible(false);
        boxReviewModal.setManaged(false);
    }

    @FXML
    private void onTemplateDefaultClicked() {
        txtProvostReply.setText("We will look into this matter.");
    }

    @FXML
    private void onTemplateMaintenanceClicked() {
        txtProvostReply.setText("The hall maintenance team has been notified and scheduled to inspect the issue.");
    }

    @FXML
    private void onApproveDefaultClicked() {
        if (selectedComplaint == null) return;
        submitResponse("APPROVED", "We will look into this matter.");
    }

    @FXML
    private void onApproveCustomClicked() {
        if (selectedComplaint == null) return;
        String text = txtProvostReply.getText() != null ? txtProvostReply.getText().trim() : "";
        if (text.isEmpty()) {
            text = "We will look into this matter.";
        }
        submitResponse("APPROVED", text);
    }

    @FXML
    private void onRejectClicked() {
        if (selectedComplaint == null) return;
        String text = txtProvostReply.getText() != null ? txtProvostReply.getText().trim() : "";
        if (text.isEmpty()) {
            text = "This complaint cannot be accommodated at this time.";
        }
        submitResponse("REJECTED", text);
    }

    private void submitResponse(String status, String responseMsg) {
        lblFeedback.setText("");
        int words = ComplaintDAO.countWords(responseMsg);
        if (words > 100) {
            lblFeedback.setText("Response cannot exceed 100 words. Current words: " + words);
            lblFeedback.setStyle("-fx-text-fill: #ef5350;");
            return;
        }

        try {
            complaintDAO.processComplaint(
                selectedComplaint.getId(),
                currentUser.getUserId(),
                status,
                responseMsg
            );

            showAlert(
                "Complaint Processed",
                "Complaint #" + selectedComplaint.getId() + " marked as " + status + ".\nResponse recorded successfully.",
                Alert.AlertType.INFORMATION
            );

            onCloseReviewModalClicked();
            loadComplaints();

        } catch (SQLException | IllegalArgumentException | IllegalStateException | SecurityException e) {
            lblFeedback.setText(e.getMessage());
            lblFeedback.setStyle("-fx-text-fill: #ef5350;");
            showAlert("Processing Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onRefreshClicked() {
        loadComplaints();
    }

    @FXML
    private void onDashboardClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_DASHBOARD_FXML, SceneManager.PROVOST_TITLE);
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void onProfileClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_PROFILE_FXML, SceneManager.PROVOST_PROFILE_TITLE);
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void onHallInformationClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.HALL_INFORMATION_FXML, SceneManager.HALL_INFORMATION_TITLE);
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void onPaymentRequestsClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_PAYMENT_REQUESTS_FXML, SceneManager.PROVOST_PAYMENT_REQUESTS_TITLE);
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void onNoticeBoardClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.NOTICE_BOARD_FXML, SceneManager.NOTICE_BOARD_TITLE);
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void onHallChangeRequestsClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_HALL_CHANGE_REQUESTS_FXML, SceneManager.PROVOST_HALL_CHANGE_TITLE);
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void onRoomChangeRequestsClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_ROOM_CHANGE_REQUESTS_FXML, SceneManager.PROVOST_ROOM_CHANGE_REQUESTS_TITLE);
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void onHallLeaveRequestsClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_HALL_LEAVE_REQUESTS_FXML, SceneManager.PROVOST_HALL_LEAVE_REQUESTS_TITLE);
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void onBackClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.goBack(stage);
        } catch (Exception e) {
            System.err.println("[ProvostComplaints] Error on back: " + e.getMessage());
        }
    }

    @FXML
    private void onLogoutClicked() {
        SessionManager.getInstance().logout();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.LOGIN_FXML, SceneManager.APP_TITLE);
        } catch (IOException e) {
            System.err.println("Logout error: " + e.getMessage());
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
