package com.hallmanagement.controller;

import com.hallmanagement.dao.NoticeDAO;
import com.hallmanagement.model.Notice;
import com.hallmanagement.model.User;
import com.hallmanagement.util.BadgeHelper;
import com.hallmanagement.util.SceneManager;
import com.hallmanagement.util.SessionManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class NoticeBoardController implements Initializable {

    private final NoticeDAO noticeDAO = new NoticeDAO();
    private User currentUser;
    private boolean isProvost;
    private List<Notice> masterNoticeList = new ArrayList<>();

    @FXML private VBox sidebar;
    @FXML private Label lblPortalTitle;
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavProfile;
    @FXML private Button btnNavHallInfo;
    @FXML private Button btnNavBillingOrRequests;
    @FXML private Button btnNavNoticeBoard;
    @FXML private Label lblNavSecondarySection;
    @FXML private VBox boxSecondaryNavStudent;
    @FXML private VBox boxSecondaryNavProvost;
    @FXML private Button btnNavHallChangeRequests;
    @FXML private Button btnNavRoomChangeRequests;
    @FXML private Button btnNavComplaints;
    @FXML private Button btnNavHallLeaveRequests;
    @FXML private Button btnNavStudentHallChange;
    @FXML private Button btnNavStudentRoomChange;
    @FXML private Button btnNavStudentComplaints;
    @FXML private Button btnNavStudentHallLeave;
    @FXML private Label lblSidebarUser;
    @FXML private Label lblSidebarRole;
    @FXML private Button btnLogout;

    @FXML private Label lblBreadcrumb;
    @FXML private Button btnNewNotice;
    @FXML private Label lblTotalNotices;
    @FXML private TextField txtSearchNotice;

    @FXML private TableView<Notice> tableNotices;
    @FXML private TableColumn<Notice, String> colSerial;
    @FXML private TableColumn<Notice, String> colDate;
    @FXML private TableColumn<Notice, String> colTitle;
    @FXML private TableColumn<Notice, String> colPreview;
    @FXML private TableColumn<Notice, Void> colAction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        isProvost = currentUser != null && currentUser.isProvost();

        setupSidebar();
        setupTableColumns();
        setupRowDoubleClickHandler();
        loadAllData();
    }

    private void setupSidebar() {
        if (currentUser != null) {
            lblSidebarUser.setText("ID: " + currentUser.getUserId());
            lblSidebarRole.setText(currentUser.getName() + (isProvost ? " (Provost)" : " (Student)"));
            lblPortalTitle.setText(isProvost ? "PROVOST PORTAL" : "STUDENT PORTAL");
            lblBreadcrumb.setText((isProvost ? "Provost Portal" : "Student Portal") + "  ›  Notice Board");

            if (isProvost) {
                sidebar.getStyleClass().add("provost-sidebar");
                btnNavProfile.setVisible(true);
                btnNavProfile.setManaged(true);
                btnNavBillingOrRequests.setText("💳  Payment Requests");
                btnNewNotice.setVisible(true);
                btnNewNotice.setManaged(true);
                lblNavSecondarySection.setText("RESIDENCE MANAGEMENT");
                boxSecondaryNavStudent.setVisible(false);
                boxSecondaryNavStudent.setManaged(false);
                boxSecondaryNavProvost.setVisible(true);
                boxSecondaryNavProvost.setManaged(true);
                BadgeHelper.updateAllProvostBadges(btnNavBillingOrRequests, btnNavHallChangeRequests, btnNavRoomChangeRequests, btnNavComplaints, btnNavHallLeaveRequests, currentUser.getUserId());
            } else {
                btnNavProfile.setVisible(true);
                btnNavProfile.setManaged(true);
                btnNavBillingOrRequests.setText("💳  Bill & Due");
                btnNewNotice.setVisible(false);
                btnNewNotice.setManaged(false);
                lblNavSecondarySection.setText("RESIDENCE SERVICES");
                boxSecondaryNavStudent.setVisible(true);
                boxSecondaryNavStudent.setManaged(true);
                boxSecondaryNavProvost.setVisible(false);
                boxSecondaryNavProvost.setManaged(false);
                BadgeHelper.updateAllStudentBadges(btnNavNoticeBoard, btnNavBillingOrRequests, btnNavStudentHallChange, btnNavStudentRoomChange, btnNavStudentComplaints, btnNavStudentHallLeave, currentUser.getUserId());
            }
        }
    }

    private void setupTableColumns() {
        colSerial.setCellValueFactory(new PropertyValueFactory<>("serial"));
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedDate()));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colPreview.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getContentPreview()));

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

        colTitle.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Notice n = getTableRow() != null ? getTableRow().getItem() : null;
                    if (n != null && !n.isRead() && !isProvost) {
                        setText("🔴 " + item + "  [NEW]");
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff;");
                    } else {
                        setText(item);
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #e8eaf6;");
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

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnView = new Button("👁 View");
            private final Button btnEdit = new Button("✏️ Edit");
            private final Button btnDelete = new Button("🗑️");

            {
                btnView.getStyleClass().add("btn-outline-small");
                btnEdit.getStyleClass().add("btn-outline-small");
                btnDelete.setStyle("-fx-background-color: rgba(239,83,80,0.15); -fx-text-fill: #ef5350; -fx-border-color: rgba(239,83,80,0.4); -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 10 5 10;");

                btnView.setOnAction(event -> {
                    Notice notice = getTableView().getItems().get(getIndex());
                    showNoticeDetailsDialog(notice);
                });

                btnEdit.setOnAction(event -> {
                    Notice notice = getTableView().getItems().get(getIndex());
                    showEditNoticeDialog(notice);
                });

                btnDelete.setOnAction(event -> {
                    Notice notice = getTableView().getItems().get(getIndex());
                    showDeleteNoticeConfirmation(notice);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    if (isProvost) {
                        HBox container = new HBox(6, btnView, btnEdit, btnDelete);
                        container.setAlignment(Pos.CENTER);
                        setGraphic(container);
                    } else {
                        btnView.setText("👁 View Details");
                        HBox container = new HBox(btnView);
                        container.setAlignment(Pos.CENTER);
                        setGraphic(container);
                    }
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
    }

    private void setupRowDoubleClickHandler() {
        tableNotices.setRowFactory(tv -> {
            TableRow<Notice> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Notice rowData = row.getItem();
                    showNoticeDetailsDialog(rowData);
                }
            });
            return row;
        });
    }

    private void loadNotices() {
        try {
            if (!isProvost && currentUser != null) {
                masterNoticeList = noticeDAO.getAllNoticesForStudent(currentUser.getUserId());
            } else {
                masterNoticeList = noticeDAO.getAllNotices();
            }
            applyFilter();
            lblTotalNotices.setText(String.valueOf(masterNoticeList.size()));
            if (!isProvost && currentUser != null) {
                BadgeHelper.updateStudentNoticeBadge(btnNavNoticeBoard, currentUser.getUserId());
            } else if (isProvost && currentUser != null) {
                BadgeHelper.updateProvostPaymentBadge(btnNavBillingOrRequests, currentUser.getUserId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load notices: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onSearchNoticeChanged() {
        applyFilter();
    }

    private void applyFilter() {
        String keyword = txtSearchNotice != null ? txtSearchNotice.getText().trim().toLowerCase() : "";
        List<Notice> filtered = new ArrayList<>();

        int index = 1;
        for (Notice n : masterNoticeList) {
            boolean match = keyword.isEmpty() ||
                    n.getTitle().toLowerCase().contains(keyword) ||
                    n.getContent().toLowerCase().contains(keyword) ||
                    n.getFormattedDate().toLowerCase().contains(keyword);

            if (match) {

                n.setSerial(String.format("%02d", index++));
                filtered.add(n);
            }
        }

        tableNotices.setItems(FXCollections.observableArrayList(filtered));
    }

    private void showNoticeDetailsDialog(Notice notice) {
        if (notice == null) return;

        if (!isProvost && currentUser != null && !notice.isRead()) {
            noticeDAO.markNoticeAsRead(currentUser.getUserId(), notice.getId());
            notice.setRead(true);
            BadgeHelper.updateStudentNoticeBadge(btnNavNoticeBoard, currentUser.getUserId());
            tableNotices.refresh();
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Notice Details — " + notice.getTitle());
        dialog.setHeaderText(null);

        VBox content = new VBox(14);
        content.setPadding(new Insets(20));
        content.setPrefWidth(540);
        content.setStyle("-fx-background-color: #0f1923;");

        Label lblBadge = new Label("OFFICIAL NOTICE");
        lblBadge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #1a73e8; -fx-background-color: rgba(26,115,232,0.15); -fx-background-radius: 4; -fx-padding: 3 8 3 8;");

        Label lblTitle = new Label(notice.getTitle());
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e8eaf6;");
        lblTitle.setWrapText(true);

        Label lblMeta = new Label("Date: " + notice.getFormattedDate() + "   |   Posted by: " + notice.getAuthorDisplay());
        lblMeta.setStyle("-fx-font-size: 12px; -fx-text-fill: #90a4ae;");

        Separator sep = new Separator();

        TextArea txtNoticeContent = new TextArea(notice.getContent());
        txtNoticeContent.setEditable(false);
        txtNoticeContent.setWrapText(true);
        txtNoticeContent.setPrefRowCount(10);
        txtNoticeContent.setStyle("-fx-control-inner-background: #0a1628; -fx-text-fill: #e8eaf6; -fx-font-size: 13.5px; -fx-font-family: 'Segoe UI', Arial, sans-serif; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 6;");

        content.getChildren().addAll(lblBadge, lblTitle, lblMeta, sep, txtNoticeContent);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    @FXML
    private void onNewNoticeClicked() {
        if (!isProvost) {
            showAlert("Permission Denied", "Students cannot post notices.", Alert.AlertType.WARNING);
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Official Notice");
        dialog.setHeaderText("Post a new circular to the Hall Notice Board");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 24, 10, 24));
        grid.setPrefWidth(520);
        grid.setStyle("-fx-background-color: #0f1923;");

        TextField txtTitle = new TextField();
        txtTitle.setPromptText("Enter notice title (e.g. Hall Meeting, Dining Notice)...");
        txtTitle.setStyle("-fx-background-color: #0a1628; -fx-text-fill: #e8eaf6; -fx-border-color: rgba(255,255,255,0.15); -fx-border-radius: 6; -fx-padding: 8 12 8 12;");

        String todayFormatted = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        Label lblDateVal = new Label(todayFormatted + " (Auto-generated using current date)");
        lblDateVal.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold; -fx-font-size: 13px;");

        TextArea txtContent = new TextArea();
        txtContent.setPromptText("Enter full notice description and instructions for students...");
        txtContent.setWrapText(true);
        txtContent.setPrefRowCount(8);
        txtContent.setStyle("-fx-control-inner-background: #0a1628; -fx-text-fill: #e8eaf6; -fx-font-size: 13px; -fx-border-color: rgba(255,255,255,0.15); -fx-border-radius: 6;");

        Label l1 = new Label("Notice Title *:");
        l1.setStyle("-fx-text-fill: #90a4ae; -fx-font-weight: bold;");
        Label l2 = new Label("Date:");
        l2.setStyle("-fx-text-fill: #90a4ae; -fx-font-weight: bold;");
        Label l3 = new Label("Notice Content *:");
        l3.setStyle("-fx-text-fill: #90a4ae; -fx-font-weight: bold;");

        grid.add(l1, 0, 0);
        grid.add(txtTitle, 0, 1);
        grid.add(l2, 0, 2);
        grid.add(lblDateVal, 0, 3);
        grid.add(l3, 0, 4);
        grid.add(txtContent, 0, 5);

        dialog.getDialogPane().setContent(grid);
        ButtonType btnPostType = new ButtonType("Post Notice", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnPostType, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == btnPostType) {
            String title = txtTitle.getText().trim();
            String content = txtContent.getText().trim();

            if (title.isEmpty()) {
                showAlert("Validation Error", "Notice title cannot be empty.", Alert.AlertType.WARNING);
                return;
            }
            if (content.isEmpty()) {
                showAlert("Validation Error", "Notice content cannot be empty.", Alert.AlertType.WARNING);
                return;
            }

            try {
                int id = noticeDAO.createNotice(title, content, currentUser.getUserId());
                if (id > 0) {
                    showAlert("Notice Posted", "Notice posted successfully.", Alert.AlertType.INFORMATION);
                    loadNotices();
                } else {
                    showAlert("Failed", "Failed to post notice. Please try again.", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                showAlert("Error", "Could not post notice: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void showEditNoticeDialog(Notice notice) {
        if (!isProvost) {
            showAlert("Permission Denied", "Students cannot edit notices.", Alert.AlertType.WARNING);
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Notice — #" + notice.getSerial());
        dialog.setHeaderText("Modify notice title or description");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 24, 10, 24));
        grid.setPrefWidth(520);
        grid.setStyle("-fx-background-color: #0f1923;");

        TextField txtTitle = new TextField(notice.getTitle());
        txtTitle.setStyle("-fx-background-color: #0a1628; -fx-text-fill: #e8eaf6; -fx-border-color: rgba(255,255,255,0.15); -fx-border-radius: 6; -fx-padding: 8 12 8 12;");

        Label lblDateVal = new Label(notice.getFormattedDate() + " (Original posting date preserved)");
        lblDateVal.setStyle("-fx-text-fill: #90a4ae; -fx-font-size: 12.5px;");

        TextArea txtContent = new TextArea(notice.getContent());
        txtContent.setWrapText(true);
        txtContent.setPrefRowCount(8);
        txtContent.setStyle("-fx-control-inner-background: #0a1628; -fx-text-fill: #e8eaf6; -fx-font-size: 13px; -fx-border-color: rgba(255,255,255,0.15); -fx-border-radius: 6;");

        Label l1 = new Label("Notice Title *:");
        l1.setStyle("-fx-text-fill: #90a4ae; -fx-font-weight: bold;");
        Label l2 = new Label("Posting Date:");
        l2.setStyle("-fx-text-fill: #90a4ae; -fx-font-weight: bold;");
        Label l3 = new Label("Notice Content *:");
        l3.setStyle("-fx-text-fill: #90a4ae; -fx-font-weight: bold;");

        grid.add(l1, 0, 0);
        grid.add(txtTitle, 0, 1);
        grid.add(l2, 0, 2);
        grid.add(lblDateVal, 0, 3);
        grid.add(l3, 0, 4);
        grid.add(txtContent, 0, 5);

        dialog.getDialogPane().setContent(grid);
        ButtonType btnSaveType = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == btnSaveType) {
            String newTitle = txtTitle.getText().trim();
            String newContent = txtContent.getText().trim();

            if (newTitle.isEmpty() || newContent.isEmpty()) {
                showAlert("Validation Error", "Notice title and content cannot be empty.", Alert.AlertType.WARNING);
                return;
            }

            try {
                boolean ok = noticeDAO.updateNotice(notice.getId(), newTitle, newContent, currentUser.getUserId());
                if (ok) {
                    showAlert("Notice Updated", "Notice updated successfully.", Alert.AlertType.INFORMATION);
                    loadNotices();
                } else {
                    showAlert("Failed", "Failed to update notice.", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                showAlert("Error", "Could not update notice: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void showDeleteNoticeConfirmation(Notice notice) {
        if (!isProvost) {
            showAlert("Permission Denied", "Students cannot delete notices.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Notice Deletion");
        confirm.setHeaderText("Are you sure you want to delete this notice?");
        confirm.setContentText("Title: " + notice.getTitle() + "\nDate: " + notice.getFormattedDate() + "\n\nThis action cannot be undone.");

        ButtonType btnDelete = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnDelete, btnCancel);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == btnDelete) {
            try {
                boolean ok = noticeDAO.deleteNotice(notice.getId(), currentUser.getUserId());
                if (ok) {
                    showAlert("Notice Deleted", "Notice deleted successfully.", Alert.AlertType.INFORMATION);
                    loadNotices();
                } else {
                    showAlert("Failed", "Failed to delete notice.", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                showAlert("Error", "Could not delete notice: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void onRefreshClicked() {
        loadNotices();
    }

    @FXML
    private void onDashboardClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            if (isProvost) {
                SceneManager.switchTo(stage, SceneManager.PROVOST_DASHBOARD_FXML, SceneManager.PROVOST_TITLE);
            } else {
                SceneManager.switchTo(stage, SceneManager.STUDENT_DASHBOARD_FXML, SceneManager.STUDENT_TITLE);
            }
        } catch (IOException e) {
            System.err.println("[NoticeBoard] Failed to navigate to Dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void onMyProfileClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            if (isProvost) {
                SceneManager.switchTo(stage, SceneManager.PROVOST_PROFILE_FXML, SceneManager.PROVOST_PROFILE_TITLE);
            } else {
                SceneManager.switchTo(stage, SceneManager.STUDENT_PROFILE_FXML, SceneManager.STUDENT_PROFILE_TITLE);
            }
        } catch (IOException e) {
            System.err.println("[NoticeBoard] Failed to navigate to Profile: " + e.getMessage());
        }
    }

    @FXML
    private void onHallInformationClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.HALL_INFORMATION_FXML, SceneManager.HALL_INFORMATION_TITLE);
        } catch (IOException e) {
            System.err.println("[NoticeBoard] Failed to navigate to Hall Information: " + e.getMessage());
        }
    }

    @FXML
    private void onBillingOrRequestsClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            if (isProvost) {
                SceneManager.switchTo(stage, SceneManager.PROVOST_PAYMENT_REQUESTS_FXML, SceneManager.PROVOST_PAYMENT_REQUESTS_TITLE);
            } else {
                SceneManager.switchTo(stage, SceneManager.STUDENT_BILLING_FXML, SceneManager.STUDENT_BILLING_TITLE);
            }
        } catch (IOException e) {
            System.err.println("[NoticeBoard] Failed to navigate to Billing / Requests: " + e.getMessage());
        }
    }

    @FXML
    private void onHallChangeClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_HALL_CHANGE_FXML, SceneManager.STUDENT_HALL_CHANGE_TITLE);
        } catch (IOException e) {
            System.err.println("[NoticeBoard] Failed to navigate to Hall Change: " + e.getMessage());
        }
    }

    @FXML
    private void onHallChangeRequestsClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_HALL_CHANGE_REQUESTS_FXML, SceneManager.PROVOST_HALL_CHANGE_TITLE);
        } catch (IOException e) {
            System.err.println("[NoticeBoard] Failed to navigate to Hall Change Requests: " + e.getMessage());
        }
    }

    @FXML
    private void onRoomChangeRequestsClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_ROOM_CHANGE_REQUESTS_FXML, SceneManager.PROVOST_ROOM_CHANGE_REQUESTS_TITLE);
        } catch (IOException e) {
            System.err.println("[NoticeBoard] Failed to navigate to Room Change Requests: " + e.getMessage());
        }
    }

    @FXML
    private void onRoomChangeClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_ROOM_CHANGE_FXML, SceneManager.STUDENT_ROOM_CHANGE_TITLE);
        } catch (IOException e) {
            System.err.println("[NoticeBoard] Failed to navigate to Room Change: " + e.getMessage());
        }
    }

    private void loadAllData() {
        loadNotices();
    }

    @FXML
    private void onHallLeaveClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.STUDENT_HALL_LEAVE_FXML, SceneManager.STUDENT_HALL_LEAVE_TITLE);
        } catch (IOException e) {
            System.err.println("[NoticeBoard] Failed to navigate to Hall Leave: " + e.getMessage());
        }
    }

    @FXML
    private void onHallLeaveRequestsClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_HALL_LEAVE_REQUESTS_FXML, SceneManager.PROVOST_HALL_LEAVE_REQUESTS_TITLE);
        } catch (IOException e) {
            System.err.println("[NoticeBoard] Failed to navigate to Hall Leave Requests: " + e.getMessage());
        }
    }

    @FXML
    private void onComplaintsClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            if (isProvost) {
                SceneManager.switchTo(stage, SceneManager.PROVOST_COMPLAINTS_FXML, SceneManager.PROVOST_COMPLAINTS_TITLE);
            } else {
                SceneManager.switchTo(stage, SceneManager.STUDENT_COMPLAINTS_FXML, SceneManager.STUDENT_COMPLAINTS_TITLE);
            }
        } catch (IOException e) {
            System.err.println("[NoticeBoard] Failed to navigate to Complaints: " + e.getMessage());
        }
    }

    @FXML
    private void onBackClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.goBack(stage);
        } catch (Exception e) {
            System.err.println("[NoticeBoard] Error on back: " + e.getMessage());
        }
    }

    @FXML
    private void onLogoutClicked() {
        SessionManager.getInstance().logout();
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.LOGIN_FXML, SceneManager.APP_TITLE);
        } catch (IOException e) {
            System.err.println("[NoticeBoard] Failed to return to login: " + e.getMessage());
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
