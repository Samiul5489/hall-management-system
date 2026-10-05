package com.hallmanagement.controller;

import com.hallmanagement.dao.HallDAO;
import com.hallmanagement.dao.UserDAO;
import com.hallmanagement.model.Hall;
import com.hallmanagement.model.RoomInfo;
import com.hallmanagement.model.SeatInfo;
import com.hallmanagement.util.SceneManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    private final UserDAO userDAO = new UserDAO();
    private final HallDAO hallDAO = new HallDAO();

    @FXML private ComboBox<String> cmbRole;
    @FXML private Label lblFeedback;
    @FXML private Button btnRegister;

    @FXML private TextField txtUserId;
    @FXML private TextField txtName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPasswordHidden;
    @FXML private TextField txtPasswordVisible;
    @FXML private Button btnTogglePassword;
    private boolean isPasswordVisible = false;

    @FXML private VBox boxStudentFields;
    @FXML private TextField txtDepartment;
    @FXML private ComboBox<String> cmbYear;

    @FXML private ComboBox<Hall> cmbCurrentHall;
    @FXML private ComboBox<RoomInfo> cmbCurrentRoom;
    @FXML private ComboBox<SeatInfo> cmbCurrentSeat;

    @FXML private ComboBox<Hall> cmbPreviousHall;
    @FXML private ComboBox<RoomInfo> cmbPreviousRoom;
    @FXML private ComboBox<Integer> cmbPreviousSeat;
    @FXML private Label lblPreviousRoom;
    @FXML private Label lblPreviousSeat;

    @FXML private VBox boxProvostFields;
    @FXML private ComboBox<Hall> cmbHall;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupRoleToggle();
        setupPasswordToggle();
        setupComboBoxConverters();
        setupComboBoxes();

        onRoleChanged();
    }

    private void setupRoleToggle() {
        cmbRole.setItems(FXCollections.observableArrayList("Student", "Provost / Admin"));
        cmbRole.getSelectionModel().selectFirst();
        cmbRole.valueProperty().addListener((obs, oldVal, newVal) -> onRoleChanged());
    }

    private void setupComboBoxConverters() {

        cmbCurrentRoom.setConverter(new StringConverter<>() {
            @Override
            public String toString(RoomInfo room) {
                if (room == null) return "";
                if (room.getAvailableSeats() > 0) {
                    return "Room " + room.getRoomNumber() + " (" + room.getAvailableSeats() + " seat" + (room.getAvailableSeats() > 1 ? "s" : "") + " available)";
                } else {
                    return "Room " + room.getRoomNumber() + " (FULL)";
                }
            }

            @Override
            public RoomInfo fromString(String string) {
                return null;
            }
        });

        cmbCurrentSeat.setConverter(new StringConverter<>() {
            @Override
            public String toString(SeatInfo seat) {
                if (seat == null) return "";
                return seat.getSeatNumberDisplay();
            }

            @Override
            public SeatInfo fromString(String string) {
                return null;
            }
        });

        cmbPreviousRoom.setConverter(new StringConverter<>() {
            @Override
            public String toString(RoomInfo room) {
                if (room == null) return "";
                return "Room " + room.getRoomNumber();
            }

            @Override
            public RoomInfo fromString(String string) {
                return null;
            }
        });

        cmbPreviousSeat.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer seatNum) {
                if (seatNum == null) return "";
                return String.format("Seat %02d", seatNum);
            }

            @Override
            public Integer fromString(String string) {
                return null;
            }
        });
    }

    private void setupComboBoxes() {
        cmbYear.setItems(FXCollections.observableArrayList(
            "1st Year", "2nd Year", "3rd Year", "4th Year", "Master's"
        ));

        try {
            List<Hall> halls = hallDAO.getAllHalls();
            ObservableList<Hall> observableHalls = FXCollections.observableArrayList(halls);

            cmbHall.setItems(observableHalls);

            cmbCurrentHall.setItems(observableHalls);

            ObservableList<Hall> prevHalls = FXCollections.observableArrayList();
            prevHalls.add(new Hall(-1, "None / 1st Time", "N/A"));
            prevHalls.addAll(halls);
            cmbPreviousHall.setItems(prevHalls);

            cmbPreviousHall.getSelectionModel().selectFirst();
            onPreviousHallChanged();

        } catch (SQLException e) {
            e.printStackTrace();
            showFeedback("Failed to load halls from database.", true);
        }
    }

    @FXML
    private void onCurrentHallChanged() {
        Hall selectedHall = cmbCurrentHall.getValue();
        cmbCurrentRoom.getItems().clear();
        cmbCurrentSeat.getItems().clear();
        cmbCurrentSeat.setDisable(true);

        if (selectedHall != null) {
            try {
                List<RoomInfo> rooms = hallDAO.getRoomsByHallId(selectedHall.getId());
                cmbCurrentRoom.setItems(FXCollections.observableArrayList(rooms));
                if (!rooms.isEmpty()) {
                    cmbCurrentRoom.setPromptText("Select Room (" + rooms.size() + " rooms)");
                } else {
                    cmbCurrentRoom.setPromptText("No rooms configured");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showFeedback("Error loading rooms for " + selectedHall.getHallName(), true);
            }
        }
    }

    @FXML
    private void onCurrentRoomChanged() {
        RoomInfo selectedRoom = cmbCurrentRoom.getValue();
        cmbCurrentSeat.getItems().clear();

        if (selectedRoom != null) {
            try {
                List<SeatInfo> seats = hallDAO.getSeatsByRoomId(selectedRoom.getId());
                List<SeatInfo> availableSeats = new ArrayList<>();
                for (SeatInfo s : seats) {
                    if (!s.isOccupied()) {
                        availableSeats.add(s);
                    }
                }

                if (availableSeats.isEmpty()) {
                    cmbCurrentSeat.setDisable(true);
                    cmbCurrentSeat.setPromptText("Room FULL — No seats");
                    showFeedback("Room " + selectedRoom.getRoomNumber() + " has no available seats. Please choose another room.", true);
                } else {
                    cmbCurrentSeat.setDisable(false);
                    cmbCurrentSeat.setItems(FXCollections.observableArrayList(availableSeats));
                    cmbCurrentSeat.getSelectionModel().selectFirst();
                    showFeedback(availableSeats.size() + " seat(s) available in Room " + selectedRoom.getRoomNumber(), false);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showFeedback("Error loading seats for Room " + selectedRoom.getRoomNumber(), true);
            }
        }
    }

    @FXML
    private void onPreviousHallChanged() {
        Hall selected = cmbPreviousHall.getValue();
        boolean isNone = (selected != null && selected.getId() == -1);

        lblPreviousRoom.setVisible(!isNone);
        lblPreviousRoom.setManaged(!isNone);
        cmbPreviousRoom.setVisible(!isNone);
        cmbPreviousRoom.setManaged(!isNone);

        lblPreviousSeat.setVisible(!isNone);
        lblPreviousSeat.setManaged(!isNone);
        cmbPreviousSeat.setVisible(!isNone);
        cmbPreviousSeat.setManaged(!isNone);

        if (!isNone && selected != null) {
            try {
                List<RoomInfo> rooms = hallDAO.getRoomsByHallId(selected.getId());
                cmbPreviousRoom.setItems(FXCollections.observableArrayList(rooms));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            cmbPreviousRoom.getItems().clear();
            cmbPreviousSeat.getItems().clear();
        }
    }

    @FXML
    private void onPreviousRoomChanged() {
        RoomInfo selectedRoom = cmbPreviousRoom.getValue();
        if (selectedRoom != null) {
            cmbPreviousSeat.setItems(FXCollections.observableArrayList(1, 2, 3, 4));
            cmbPreviousSeat.getSelectionModel().selectFirst();
        } else {
            cmbPreviousSeat.getItems().clear();
        }
    }

    private void setupPasswordToggle() {
        txtPasswordVisible.setManaged(false);
        txtPasswordVisible.setVisible(false);

        txtPasswordVisible.textProperty().bindBidirectional(txtPasswordHidden.textProperty());
    }

    @FXML
    private void onTogglePassword() {
        isPasswordVisible = !isPasswordVisible;

        txtPasswordHidden.setVisible(!isPasswordVisible);
        txtPasswordHidden.setManaged(!isPasswordVisible);

        txtPasswordVisible.setVisible(isPasswordVisible);
        txtPasswordVisible.setManaged(isPasswordVisible);

        btnTogglePassword.setText(isPasswordVisible ? "Hide" : "Show");
    }

    private void onRoleChanged() {
        boolean isStudent = "Student".equals(cmbRole.getValue());

        boxStudentFields.setVisible(isStudent);
        boxStudentFields.setManaged(isStudent);

        boxProvostFields.setVisible(!isStudent);
        boxProvostFields.setManaged(!isStudent);
    }

    @FXML
    private void onRegisterClicked() {
        lblFeedback.setText("");

        String role = cmbRole.getValue();
        String userId = txtUserId.getText().trim();
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPasswordHidden.getText();

        if (userId.isEmpty() || name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showFeedback("Please fill in all common fields (including Email).", true);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showFeedback("Please enter a valid email address.", true);
            return;
        }

        try {
            if ("Student".equals(role)) {
                registerStudent(userId, name, phone, email, password);
            } else {
                registerProvost(userId, name, phone, password);
            }
        } catch (Exception e) {
            showFeedback(e.getMessage(), true);
        }
    }

    private void registerStudent(String userId, String name, String phone, String email, String password) throws Exception {
        String dept = txtDepartment.getText().trim();
        String year = cmbYear.getValue();

        if (dept.isEmpty() || year == null) {
            showFeedback("Please fill in all academic details.", true);
            return;
        }

        Hall currentHall = cmbCurrentHall.getValue();
        RoomInfo currentRoom = cmbCurrentRoom.getValue();
        SeatInfo currentSeat = cmbCurrentSeat.getValue();

        Integer curHallId = null;
        Integer curRoom = null;
        Integer curSeat = null;

        if (currentHall != null) {
            curHallId = currentHall.getId();
            if (currentRoom == null) {
                showFeedback("Please select a room for the selected hall.", true);
                return;
            }
            if (currentSeat == null) {
                showFeedback("Please select an available seat in Room " + currentRoom.getRoomNumber(), true);
                return;
            }
            curRoom = currentRoom.getRoomNumber();
            curSeat = currentSeat.getSeatNumber();
        }

        Hall previousHall = cmbPreviousHall.getValue();
        Integer prevHallId = null;
        Integer prevRoom = null;
        Integer prevSeat = null;

        if (previousHall != null && previousHall.getId() != -1) {
            prevHallId = previousHall.getId();
            RoomInfo prevRoomObj = cmbPreviousRoom.getValue();
            if (prevRoomObj != null) {
                prevRoom = prevRoomObj.getRoomNumber();
            }
            prevSeat = cmbPreviousSeat.getValue();
        }

        userDAO.registerStudent(userId, password, name, phone, email, dept, year, curHallId, curRoom, curSeat, prevHallId, prevRoom, prevSeat);
        showFeedback("Student account created successfully! Please login.", false);
        btnRegister.setDisable(true);
    }

    private void registerProvost(String userId, String name, String phone, String password) throws Exception {
        Hall selectedHall = cmbHall.getValue();
        if (selectedHall == null) {
            showFeedback("Please select a hall for the Provost.", true);
            return;
        }

        userDAO.registerProvost(userId, password, name, phone, selectedHall.getId());
        showFeedback("Provost account created successfully! Please login.", false);
        btnRegister.setDisable(true);
    }

    @FXML
    private void onBackToLoginClicked() {
        try {
            Stage stage = (Stage) btnRegister.getScene().getWindow();
            SceneManager.switchTo(stage, SceneManager.LOGIN_FXML, SceneManager.APP_TITLE);
        } catch (IOException e) {
            System.err.println("Failed to open Login: " + e.getMessage());
        }
    }

    private void showFeedback(String message, boolean isError) {
        lblFeedback.setText(message);
        lblFeedback.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #2ecc71;");
    }
}
