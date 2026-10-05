package com.hallmanagement.controller;

import com.hallmanagement.dao.ComplaintDAO;
import com.hallmanagement.dao.StudentDAO;
import com.hallmanagement.model.Complaint;
import com.hallmanagement.model.ProvostInfo;
import com.hallmanagement.model.Student;
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
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class StudentComplaintsController implements Initializable {

    @FXML private Label lblSidebarUser;
    @FXML private Label lblSidebarRole;
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavProfile;
    @FXML private Button btnNavHallInfo;
    @FXML private Button btnNavBilling;
    @FXML private Button btnNavNoticeBoard;
    @FXML private Button btnNavHallChange;
    @FXML private Button btnNavRoomChange;
    @FXML private Button btnNavComplaints;
    @FXML private Button btnNavHallLeave;
    @FXML private Button btnLogout;

    @FXML private Button btnToggleNewComplaint;

    @FXML private VBox boxComplaintForm;
    @FXML private Label lblFormTitle;
    @FXML private Label lblStudentName;
    @FXML private Label lblStudentId;
    @FXML private Label lblStudentDept;
    @FXML private Label lblStudentResidence;
    @FXML private ComboBox<ProvostInfo> cmbProvost;
    @FXML private TextField txtComplaintTitle;
    @FXML private TextArea txtComplaintMessage;
    @FXML private Label lblFormFeedback;
    @FXML private Button btnSubmitComplaint;

    @FXML private TableView<Complaint> tblComplaints;
    @FXML private TableColumn<Complaint, String> colSerial;
    @FXML private TableColumn<Complaint, String> colDate;
    @FXML private TableColumn<Complaint, String> colTitle;
    @FXML private TableColumn<Complaint, String> colProvost;
    @FXML private TableColumn<Complaint, String> colStatus;
    @FXML private TableColumn<Complaint, Complaint> colActions;
    @FXML private Label lblComplaintCount;

    @FXML private VBox boxViewModal;
    @FXML private Label lblViewTitle;
    @FXML private Label lblViewStatus;
    @FXML private Label lblViewProvost;
    @FXML private Label lblViewSubmittedAt;
    @FXML private Label lblViewMessage;
    @FXML private VBox boxProvostResponse;
    @FXML private Label lblViewResponse;
    @FXML private Label lblViewRespondedAt;
    @FXML private Button btnModalEdit;
    @FXML private Button btnModalCancel;
    @FXML private Button btnModalDelete;

    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    private User currentUser;
    private Student currentStudent;
    private Integer editingComplaintId = null;
    private Complaint currentlyViewedComplaint = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblSidebarUser.setText("ID: " + currentUser.getUserId());
            lblSidebarRole.setText(currentUser.getName() + " (Student)");
            BadgeHelper.updateAllStudentBadges(btnNavNoticeBoard, btnNavBilling, btnNavHallChange, btnNavRoomChange, btnNavComplaints, btnNavHallLeave, currentUser.getUserId());
            loadStudentInfo();
        }

        setupProvostComboBox();
        setupTableColumns();
        loadComplaints();
    }

    private void loadStudentInfo() {
        try {
            currentStudent = studentDAO.getStudentByUserId(currentUser.getUserId());
            if (currentStudent != null) {
                lblStudentName.setText("Name: " + currentStudent.getName());
                lblStudentId.setText("ID: " + currentStudent.getUserId());
                lblStudentDept.setText("Dept: " + currentStudent.getDepartment() + " (Year " + currentStudent.getAcademicYear() + ")");
                lblStudentResidence.setText("Residence: " + currentStudent.getResidenceDisplay());
            } else {
                lblStudentName.setText("Name: " + currentUser.getName());
                lblStudentId.setText("ID: " + currentUser.getUserId());
                lblStudentDept.setText("Dept: RUET");
                lblStudentResidence.setText("Residence: General Resident");
            }
        } catch (SQLException e) {
            System.err.println("Failed to load student info for complaint: " + e.getMessage());
        }
    }

    private void setupProvostComboBox() {
        cmbProvost.setConverter(new StringConverter<>() {
            @Override
            public String toString(ProvostInfo p) {
                if (p == null) return "";
                return p.getName() + " — " + p.getHallName();
            }

            @Override
            public ProvostInfo fromString(String string) {
                return null;
            }
        });

        try {
            List<ProvostInfo> provosts = complaintDAO.getProvostListWithHalls();
            cmbProvost.setItems(FXCollections.observableArrayList(provosts));

            if (currentStudent != null && provosts != null) {
                for (ProvostInfo p : provosts) {
                    if (p.getHallId() == currentStudent.getCurrentHallId()) {
                        cmbProvost.setValue(p);
                        break;
                    }
                }
            }
            if (cmbProvost.getValue() == null && provosts != null && !provosts.isEmpty()) {
                cmbProvost.setValue(provosts.get(0));
            }
        } catch (SQLException e) {
            System.err.println("Failed to load provost list: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        tblComplaints.setRowFactory(tv -> {
            TableRow<Complaint> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    openViewModal(row.getItem());
                }
            });
            return row;
        });

        colSerial.setCellValueFactory(cellData -> {
            int index = tblComplaints.getItems().indexOf(cellData.getValue()) + 1;
            return new ReadOnlyObjectWrapper<>(String.format("%02d", index));
        });

        colDate.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getSubmittedAtFormatted()));
        colTitle.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getTitle()));
        colProvost.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
            cellData.getValue().getProvostName() != null ? cellData.getValue().getProvostName() : cellData.getValue().getProvostId()
        ));

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
                    HBox box = new HBox(6);
                    box.setAlignment(Pos.CENTER);

                    Button btnReview = new Button("👁 Review");
                    btnReview.setStyle("-fx-background-color: rgba(26,115,232,0.18); -fx-text-fill: #90caf9; -fx-border-color: rgba(26,115,232,0.5); -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 8 4 8;");
                    btnReview.setOnAction(e -> openViewModal(c));
                    box.getChildren().add(btnReview);

                    if (c.isPending()) {
                        Button btnEdit = new Button("✏️ Edit");
                        btnEdit.setStyle("-fx-background-color: rgba(245,158,11,0.18); -fx-text-fill: #fbbf24; -fx-border-color: rgba(245,158,11,0.5); -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 8 4 8;");
                        btnEdit.setOnAction(e -> openEditForm(c));
                        box.getChildren().add(btnEdit);
                    }

                    Button btnDelete = new Button("🗑️ Delete");
                    btnDelete.setStyle("-fx-background-color: rgba(239,83,80,0.18); -fx-text-fill: #ef9a9a; -fx-border-color: rgba(239,83,80,0.5); -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 8 4 8;");
                    btnDelete.setOnAction(e -> handleDeleteComplaint(c));
                    box.getChildren().add(btnDelete);

                    setGraphic(box);
                }
            }
        });
    }

    private void loadComplaints() {
        if (currentUser == null) return;
        try {
            List<Complaint> list = complaintDAO.getComplaintsByStudent(currentUser.getUserId());
            tblComplaints.setItems(FXCollections.observableArrayList(list));
            lblComplaintCount.setText(list.size() + " total complaint(s)");
            BadgeHelper.updateAllStudentBadges(btnNavNoticeBoard, btnNavBilling, btnNavHallChange, btnNavComplaints, currentUser.getUserId());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load complaints: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onToggleNewComplaintClicked() {
        editingComplaintId = null;
        lblFormTitle.setText("SUBMIT NEW COMPLAINT");
        btnSubmitComplaint.setText("Submit Complaint");
        txtComplaintTitle.clear();
        txtComplaintMessage.clear();
        lblFormFeedback.setText("");

        if (currentStudent != null && cmbProvost.getItems() != null) {
            for (ProvostInfo p : cmbProvost.getItems()) {
                if (p.getHallId() == currentStudent.getCurrentHallId()) {
                    cmbProvost.setValue(p);
                    break;
                }
            }
        }

        boxComplaintForm.setVisible(true);
        boxComplaintForm.setManaged(true);
        boxViewModal.setVisible(false);
        boxViewModal.setManaged(false);
    }

    private void openEditForm(Complaint c) {
        if (!c.isPending()) {
            showAlert("Action Denied", "Only PENDING complaints can be edited.", Alert.AlertType.WARNING);
            return;
        }

        editingComplaintId = c.getId();
        lblFormTitle.setText("EDIT PENDING COMPLAINT (#" + c.getId() + ")");
        btnSubmitComplaint.setText("Save Changes");
        txtComplaintTitle.setText(c.getTitle());
        txtComplaintMessage.setText(c.getMessage());
        lblFormFeedback.setText("");

        if (cmbProvost.getItems() != null) {
            for (ProvostInfo p : cmbProvost.getItems()) {
                if (p.getUserId().equalsIgnoreCase(c.getProvostId())) {
                    cmbProvost.setValue(p);
                    break;
                }
            }
        }

        boxComplaintForm.setVisible(true);
        boxComplaintForm.setManaged(true);
        boxViewModal.setVisible(false);
        boxViewModal.setManaged(false);
    }

    @FXML
    private void onCancelFormClicked() {
        editingComplaintId = null;
        boxComplaintForm.setVisible(false);
        boxComplaintForm.setManaged(false);
    }

    @FXML
    private void onSubmitComplaintClicked() {
        lblFormFeedback.setText("");
        String title = txtComplaintTitle.getText() != null ? txtComplaintTitle.getText().trim() : "";
        String message = txtComplaintMessage.getText() != null ? txtComplaintMessage.getText().trim() : "";
        ProvostInfo selectedProvost = cmbProvost.getValue();

        if (title.isEmpty()) {
            lblFormFeedback.setText("Please enter a complaint title.");
            lblFormFeedback.setStyle("-fx-text-fill: #ef5350;");
            return;
        }

        if (message.isEmpty()) {
            lblFormFeedback.setText("Please enter a complaint message.");
            lblFormFeedback.setStyle("-fx-text-fill: #ef5350;");
            return;
        }

        if (selectedProvost == null) {
            lblFormFeedback.setText("Please select an assigned Provost.");
            lblFormFeedback.setStyle("-fx-text-fill: #ef5350;");
            return;
        }

        try {
            if (editingComplaintId == null) {

                complaintDAO.createComplaint(
                    currentUser.getUserId(),
                    selectedProvost.getUserId(),
                    title,
                    message
                );
                showAlert("Complaint Submitted", "Your complaint has been submitted successfully.\nStatus: PENDING", Alert.AlertType.INFORMATION);
            } else {

                boolean ok = complaintDAO.updatePendingComplaint(
                    editingComplaintId,
                    currentUser.getUserId(),
                    title,
                    message,
                    selectedProvost.getUserId()
                );
                if (ok) {
                    showAlert("Complaint Updated", "Your complaint changes have been saved successfully.", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Update Failed", "Could not update complaint. It may no longer be in PENDING status.", Alert.AlertType.WARNING);
                }
            }

            onCancelFormClicked();
            loadComplaints();

        } catch (SQLException e) {
            lblFormFeedback.setText("Database error: " + e.getMessage());
            lblFormFeedback.setStyle("-fx-text-fill: #ef5350;");
        } catch (IllegalArgumentException e) {
            lblFormFeedback.setText(e.getMessage());
            lblFormFeedback.setStyle("-fx-text-fill: #ef5350;");
        }
    }

    private void handleCancelComplaint(Complaint c) {
        if (c == null) return;
        if (!c.isPending()) {
            showAlert("Action Denied", "Only PENDING complaints can be cancelled.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Complaint Confirmation");
        confirm.setHeaderText("Cancel Complaint: " + c.getTitle() + "?");
        confirm.setContentText("Are you sure you want to cancel this complaint?\nOnce cancelled, the Provost will not process it.");

        ButtonType btnYes = new ButtonType("Cancel Complaint", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnKeep = new ButtonType("Keep Complaint", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnYes, btnKeep);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == btnYes) {
            try {
                boolean ok = complaintDAO.cancelComplaint(c.getId(), currentUser.getUserId());
                if (ok) {
                    showAlert("Complaint Cancelled", "The complaint has been cancelled.", Alert.AlertType.INFORMATION);
                    onCloseViewModalClicked();
                    loadComplaints();
                } else {
                    showAlert("Cancellation Failed", "Could not cancel complaint. It might already be processed.", Alert.AlertType.WARNING);
                }
            } catch (SQLException e) {
                showAlert("Database Error", "Error cancelling complaint: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void handleDeleteComplaint(Complaint c) {
        if (c == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Complaint Confirmation");
        confirm.setHeaderText("Delete Complaint: " + c.getTitle() + "?");
        confirm.setContentText("Are you sure you want to permanently delete this complaint?\nThis action cannot be undone.");

        ButtonType btnYes = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Keep", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnYes, btnCancel);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == btnYes) {
            try {
                boolean ok = complaintDAO.deleteComplaint(c.getId(), currentUser.getUserId());
                if (ok) {
                    showAlert("Complaint Deleted", "Complaint #" + c.getId() + " was deleted successfully.", Alert.AlertType.INFORMATION);
                    onCloseViewModalClicked();
                    loadComplaints();
                } else {
                    showAlert("Delete Failed", "Could not delete complaint. It may not belong to your account.", Alert.AlertType.WARNING);
                }
            } catch (SQLException e) {
                showAlert("Database Error", "Error deleting complaint: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void openViewModal(Complaint c) {
        if (c == null) return;
        currentlyViewedComplaint = c;

        lblViewTitle.setText(c.getTitle());
        lblViewStatus.setText(c.getStatus());
        lblViewProvost.setText((c.getProvostName() != null ? c.getProvostName() : c.getProvostId()) + " (" + (c.getProvostHallName() != null ? c.getProvostHallName() : "Administration") + ")");
        lblViewSubmittedAt.setText(c.getSubmittedAtFullFormatted());
        lblViewMessage.setText(c.getMessage());

        if (c.getResponseMessage() != null && !c.getResponseMessage().isEmpty()) {
            boxProvostResponse.setVisible(true);
            boxProvostResponse.setManaged(true);
            lblViewResponse.setText(c.getResponseMessage());
            lblViewRespondedAt.setText(c.getRespondedAtFormatted());
        } else {
            boxProvostResponse.setVisible(false);
            boxProvostResponse.setManaged(false);
        }

        btnModalEdit.setVisible(c.isPending());
        btnModalEdit.setManaged(c.isPending());
        btnModalCancel.setVisible(c.isPending());
        btnModalCancel.setManaged(c.isPending());
        btnModalDelete.setVisible(true);
        btnModalDelete.setManaged(true);

        if (c.isProcessed() && currentUser != null) {
            complaintDAO.markComplaintAsReviewedByStudent(c.getId(), currentUser.getUserId());
            c.setStudentViewedAt(java.time.LocalDateTime.now());
            BadgeHelper.updateAllStudentBadges(btnNavNoticeBoard, btnNavBilling, btnNavHallChange, btnNavComplaints, currentUser.getUserId());
        }

        boxViewModal.setVisible(true);
        boxViewModal.setManaged(true);
        boxComplaintForm.setVisible(false);
        boxComplaintForm.setManaged(false);
    }

    @FXML
    private void onModalEditClicked() {
        if (currentlyViewedComplaint != null) {
            openEditForm(currentlyViewedComplaint);
        }
    }

    @FXML
    private void onModalCancelClicked() {
        if (currentlyViewedComplaint != null) {
            handleCancelComplaint(currentlyViewedComplaint);
        }
    }

    @FXML
    private void onModalDeleteClicked() {
        if (currentlyViewedComplaint != null) {
            handleDeleteComplaint(currentlyViewedComplaint);
        }
    }

    @FXML
    private void onCloseViewModalClicked() {
        currentlyViewedComplaint = null;
        boxViewModal.setVisible(false);
        boxViewModal.setManaged(false);
    }

    @FXML
    private void onRefreshClicked() {
        loadComplaints();
    }

    @FXML
    private void onDashboardClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_DASHBOARD_FXML, SceneManager.STUDENT_TITLE);
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void onMyProfileClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_PROFILE_FXML, SceneManager.STUDENT_PROFILE_TITLE);
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
    private void onBillingClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_BILLING_FXML, SceneManager.STUDENT_BILLING_TITLE);
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
    private void onHallChangeClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_HALL_CHANGE_FXML, SceneManager.STUDENT_HALL_CHANGE_TITLE);
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void onRoomChangeClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_ROOM_CHANGE_FXML, SceneManager.STUDENT_ROOM_CHANGE_TITLE);
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void onHallLeaveClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_HALL_LEAVE_FXML, SceneManager.STUDENT_HALL_LEAVE_TITLE);
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
            System.err.println("[StudentComplaints] Error on back: " + e.getMessage());
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
