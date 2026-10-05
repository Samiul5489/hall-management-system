package com.hallmanagement.controller;

import com.hallmanagement.dao.HallLeaveDAO;
import com.hallmanagement.model.HallLeaveRequest;
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

public class ProvostHallLeaveRequestsController implements Initializable {

    private final HallLeaveDAO hallLeaveDAO = new HallLeaveDAO();

    private User currentUser;
    private HallLeaveRequest currentlySelectedRequest;

    @FXML private Label lblSidebarUser;
    @FXML private Label lblSidebarRole;
    @FXML private Button btnLogout;
    @FXML private Button btnNavPaymentRequests;
    @FXML private Button btnNavHallChangeRequests;
    @FXML private Button btnNavRoomChangeRequests;
    @FXML private Button btnNavComplaints;
    @FXML private Button btnNavHallLeaveRequests;

    @FXML private Label lblPendingCount;
    @FXML private Label lblProcessedCount;

    @FXML private TableView<HallLeaveRequest> tablePendingRequests;
    @FXML private TableColumn<HallLeaveRequest, String> colPendingSerial;
    @FXML private TableColumn<HallLeaveRequest, String> colPendingDate;
    @FXML private TableColumn<HallLeaveRequest, String> colPendingStudentName;
    @FXML private TableColumn<HallLeaveRequest, String> colPendingRoll;
    @FXML private TableColumn<HallLeaveRequest, String> colPendingHall;
    @FXML private TableColumn<HallLeaveRequest, String> colPendingRoom;
    @FXML private TableColumn<HallLeaveRequest, String> colPendingSeat;
    @FXML private TableColumn<HallLeaveRequest, String> colPendingStatus;
    @FXML private TableColumn<HallLeaveRequest, Void> colPendingAction;

    @FXML private VBox boxProcessModal;
    @FXML private Label lblModalStatusBadge;
    @FXML private Label lblModalStudentName;
    @FXML private Label lblModalStudentRoll;
    @FXML private Label lblModalStudentDept;
    @FXML private Label lblModalDue;
    @FXML private Label lblModalHall;
    @FXML private Label lblModalRoom;
    @FXML private Label lblModalSeat;
    @FXML private Label lblModalReqDate;
    @FXML private Label lblModalReason;
    @FXML private HBox boxModalActionButtons;
    @FXML private Button btnModalApprove;
    @FXML private Button btnModalDecline;

    @FXML private VBox boxDeclineModal;
    @FXML private TextArea txtDeclineReason;
    @FXML private Label lblDeclineFeedback;

    @FXML private TextField txtSearchHistory;
    @FXML private TableView<HallLeaveRequest> tableAllHistory;
    @FXML private TableColumn<HallLeaveRequest, String> colHistSerial;
    @FXML private TableColumn<HallLeaveRequest, String> colHistDate;
    @FXML private TableColumn<HallLeaveRequest, String> colHistStudentName;
    @FXML private TableColumn<HallLeaveRequest, String> colHistRoll;
    @FXML private TableColumn<HallLeaveRequest, String> colHistHall;
    @FXML private TableColumn<HallLeaveRequest, String> colHistRoomSeat;
    @FXML private TableColumn<HallLeaveRequest, String> colHistStatus;
    @FXML private TableColumn<HallLeaveRequest, String> colHistProcessedBy;
    @FXML private TableColumn<HallLeaveRequest, String> colHistProcessedDate;
    @FXML private TableColumn<HallLeaveRequest, String> colHistNote;

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

        colPendingSerial.setCellValueFactory(new PropertyValueFactory<>("serial"));
        colPendingDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedRequestDate()));
        colPendingStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colPendingRoll.setCellValueFactory(new PropertyValueFactory<>("studentRoll"));
        colPendingHall.setCellValueFactory(new PropertyValueFactory<>("hallName"));
        colPendingRoom.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getRoomNumber())));
        colPendingSeat.setCellValueFactory(cell -> new SimpleStringProperty(String.format("%02d", cell.getValue().getSeatNumber())));
        colPendingStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colPendingStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    setStyle("-fx-text-fill: #fbc02d; -fx-font-weight: bold;");
                }
            }
        });

        Label lblActionHeader = new Label("Actions");
        lblActionHeader.setStyle("-fx-text-fill: #90caf9; -fx-font-weight: bold; -fx-font-size: 12.5px; -fx-alignment: CENTER;");
        lblActionHeader.setMaxWidth(Double.MAX_VALUE);
        lblActionHeader.setAlignment(Pos.CENTER);
        colPendingAction.setGraphic(lblActionHeader);
        colPendingAction.setText(null);

        colPendingAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnReview = new Button("Review");
            private final Button btnApprove = new Button("Approve");
            private final Button btnDecline = new Button("Decline");
            private final HBox container = new HBox(6, btnReview, btnApprove, btnDecline);

            {
                container.setAlignment(Pos.CENTER);
                btnReview.setStyle("-fx-background-color: rgba(26,115,232,0.22); -fx-text-fill: #90caf9; -fx-border-color: rgba(26,115,232,0.6); -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 10 4 10;");
                btnApprove.setStyle("-fx-background-color: rgba(34,197,94,0.22); -fx-text-fill: #4ade80; -fx-border-color: rgba(34,197,94,0.6); -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 10 4 10;");
                btnDecline.setStyle("-fx-background-color: rgba(239,68,68,0.22); -fx-text-fill: #f87171; -fx-border-color: rgba(239,68,68,0.6); -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 10 4 10;");

                btnReview.setOnAction(event -> {
                    HallLeaveRequest req = getTableView().getItems().get(getIndex());
                    openProcessModal(req);
                });

                btnApprove.setOnAction(event -> {
                    HallLeaveRequest req = getTableView().getItems().get(getIndex());
                    approveRequest(req);
                });

                btnDecline.setOnAction(event -> {
                    HallLeaveRequest req = getTableView().getItems().get(getIndex());
                    openDeclineDialog(req);
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

        colHistSerial.setCellValueFactory(new PropertyValueFactory<>("serial"));
        colHistDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedRequestDate()));
        colHistStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colHistRoll.setCellValueFactory(new PropertyValueFactory<>("studentRoll"));
        colHistHall.setCellValueFactory(new PropertyValueFactory<>("hallName"));
        colHistRoomSeat.setCellValueFactory(cell -> new SimpleStringProperty(
            "Rm " + cell.getValue().getRoomNumber() + ", St " + String.format("%02d", cell.getValue().getSeatNumber())
        ));
        colHistStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colHistStatus.setCellFactory(col -> new TableCell<>() {
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

        colHistProcessedBy.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getProcessedByName() != null ? cell.getValue().getProcessedByName() :
            (cell.getValue().getProcessedBy() != null ? cell.getValue().getProcessedBy() : "-")
        ));

        colHistProcessedDate.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getProcessedDate() != null ? cell.getValue().getFormattedProcessedDate() : "-"
        ));

        colHistNote.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getDeclineReason() != null ? cell.getValue().getDeclineReason() : "-"
        ));
    }

    private void loadAllData() {
        if (currentUser == null) return;

        loadPendingRequests();
        loadHistoryRequests();
        updateBadges();
    }

    private void loadPendingRequests() {
        try {
            List<HallLeaveRequest> pendingList = hallLeaveDAO.getPendingRequestsForProvost(currentUser.getUserId());
            ObservableList<HallLeaveRequest> observableList = FXCollections.observableArrayList(pendingList);
            tablePendingRequests.setItems(observableList);
            lblPendingCount.setText(String.valueOf(pendingList.size()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadHistoryRequests() {
        try {
            String keyword = txtSearchHistory != null ? txtSearchHistory.getText().trim() : "";
            List<HallLeaveRequest> allList = hallLeaveDAO.getAllRequestsForProvost(currentUser.getUserId(), keyword);
            ObservableList<HallLeaveRequest> observableList = FXCollections.observableArrayList(allList);
            tableAllHistory.setItems(observableList);
            lblProcessedCount.setText(String.valueOf(allList.size()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateBadges() {
        if (currentUser != null) {
            BadgeHelper.updateAllProvostBadges(
                btnNavPaymentRequests,
                btnNavHallChangeRequests,
                btnNavRoomChangeRequests,
                btnNavComplaints,
                btnNavHallLeaveRequests,
                currentUser.getUserId()
            );
        }
    }

    @FXML
    private void onSearchHistoryChanged() {
        loadHistoryRequests();
    }

    @FXML
    private void onViewSelectedClicked() {
        HallLeaveRequest selected = tablePendingRequests.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a leave application to view details.", Alert.AlertType.WARNING);
            return;
        }
        openProcessModal(selected);
    }

    @FXML
    private void onApproveSelectedClicked() {
        HallLeaveRequest selected = tablePendingRequests.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a leave application to approve.", Alert.AlertType.WARNING);
            return;
        }
        approveRequest(selected);
    }

    @FXML
    private void onDeclineSelectedClicked() {
        HallLeaveRequest selected = tablePendingRequests.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a leave application to decline.", Alert.AlertType.WARNING);
            return;
        }
        openDeclineDialog(selected);
    }

    private void openProcessModal(HallLeaveRequest req) {
        if (req == null) return;
        currentlySelectedRequest = req;

        lblModalStudentName.setText(req.getStudentName());
        lblModalStudentRoll.setText(req.getStudentRoll());
        lblModalStudentDept.setText(req.getStudentDept() != null ? req.getStudentDept() : "N/A");
        lblModalDue.setText("Tk. 0.00 (Verified)");
        lblModalHall.setText(req.getHallName());
        lblModalRoom.setText("Room " + req.getRoomNumber());
        lblModalSeat.setText("Seat " + String.format("%02d", req.getSeatNumber()));
        lblModalReqDate.setText(req.getFormattedRequestDate());
        lblModalReason.setText(req.getReason());

        lblModalStatusBadge.setText(req.getStatus());

        boxProcessModal.setVisible(true);
        boxProcessModal.setManaged(true);
        boxDeclineModal.setVisible(false);
        boxDeclineModal.setManaged(false);
    }

    @FXML
    private void onCloseProcessModalClicked() {
        boxProcessModal.setVisible(false);
        boxProcessModal.setManaged(false);
        currentlySelectedRequest = null;
    }

    @FXML
    private void onModalApproveClicked() {
        if (currentlySelectedRequest != null) {
            approveRequest(currentlySelectedRequest);
        }
    }

    @FXML
    private void onModalDeclineClicked() {
        if (currentlySelectedRequest != null) {
            openDeclineDialog(currentlySelectedRequest);
        }
    }

    private void approveRequest(HallLeaveRequest req) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Approve Hall Leave & Vacate Seat");
        confirm.setHeaderText("Approve Clearance for " + req.getStudentName() + " (" + req.getStudentRoll() + ")");
        confirm.setContentText(
            "Are you sure you want to APPROVE this Hall Leave application?\n\n" +
            "• Student residence will be cleared (Current Hall: NONE).\n" +
            "• " + req.getHallName() + " (Room " + req.getRoomNumber() + ", Seat " + String.format("%02d", req.getSeatNumber()) + ") will become AVAILABLE immediately.\n" +
            "• Hall history will be updated with today's leave clearance date."
        );

        ButtonType btnApprove = new ButtonType("Approve & Release Seat", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnApprove, btnCancel);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == btnApprove) {
            try {
                hallLeaveDAO.approveHallLeaveRequest(req.getId(), currentUser.getUserId());
                showAlert("Hall Leave Approved", "Hall Leave request #" + req.getId() + " has been approved successfully. The seat is now vacant and available.", Alert.AlertType.INFORMATION);
                onCloseProcessModalClicked();
                loadAllData();
            } catch (Exception e) {
                showAlert("Approval Failed", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void openDeclineDialog(HallLeaveRequest req) {
        currentlySelectedRequest = req;
        txtDeclineReason.clear();
        lblDeclineFeedback.setText("");

        boxDeclineModal.setVisible(true);
        boxDeclineModal.setManaged(true);
        boxProcessModal.setVisible(false);
        boxProcessModal.setManaged(false);
    }

    @FXML
    private void onCloseDeclineModalClicked() {
        boxDeclineModal.setVisible(false);
        boxDeclineModal.setManaged(false);
        lblDeclineFeedback.setText("");
    }

    @FXML
    private void onConfirmDeclineClicked() {
        if (currentlySelectedRequest == null) return;

        String reason = txtDeclineReason.getText().trim();
        if (reason.isEmpty()) {
            lblDeclineFeedback.setText("Please enter a valid administrative reason for declining.");
            return;
        }

        try {
            hallLeaveDAO.declineHallLeaveRequest(currentlySelectedRequest.getId(), currentUser.getUserId(), reason);
            showAlert("Request Declined", "Hall Leave request #" + currentlySelectedRequest.getId() + " has been declined.", Alert.AlertType.INFORMATION);
            onCloseDeclineModalClicked();
            loadAllData();
        } catch (Exception e) {
            lblDeclineFeedback.setText(e.getMessage());
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
    private void onComplaintsClicked() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.PROVOST_COMPLAINTS_FXML, SceneManager.PROVOST_COMPLAINTS_TITLE);
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
            System.err.println("[ProvostHallLeaveRequests] Error on back: " + e.getMessage());
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
