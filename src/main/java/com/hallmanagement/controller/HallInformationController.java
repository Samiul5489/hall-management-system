package com.hallmanagement.controller;

import com.hallmanagement.dao.HallDAO;
import com.hallmanagement.dao.StudentDAO;
import com.hallmanagement.model.*;
import com.hallmanagement.util.BadgeHelper;
import com.hallmanagement.util.SceneManager;
import com.hallmanagement.util.SessionManager;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;

public class HallInformationController implements Initializable {

    private final HallDAO hallDAO = new HallDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    private User currentUser;
    private Student currentStudent;
    private boolean isProvost;
    private int selectedHallId = 1;

    private final ObservableList<FloorInfo> floorList = FXCollections.observableArrayList();
    private final ObservableList<RoomFullProfile> roomList = FXCollections.observableArrayList();
    private final ObservableList<RoomFullProfile> roomElecList = FXCollections.observableArrayList();
    private final ObservableList<RoomFullProfile> roomFurnList = FXCollections.observableArrayList();
    private final ObservableList<EquipmentItem> acList = FXCollections.observableArrayList();
    private final ObservableList<EquipmentItem> lightingList = FXCollections.observableArrayList();
    private final ObservableList<EquipmentItem> networkList = FXCollections.observableArrayList();
    private final ObservableList<EquipmentItem> cctvList = FXCollections.observableArrayList();
    private final ObservableList<EquipmentItem> fireSafetyList = FXCollections.observableArrayList();
    private final ObservableList<MaintenanceRecord> maintenanceList = FXCollections.observableArrayList();
    private final ObservableList<CleaningSchedule> cleaningList = FXCollections.observableArrayList();
    private final ObservableList<HallContact> contactList = FXCollections.observableArrayList();
    private final ObservableList<UtilityRecord> utilityList = FXCollections.observableArrayList();
    private final ObservableList<AssetItem> assetList = FXCollections.observableArrayList();

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
    @FXML private Label lblUserId;
    @FXML private Label lblRole;
    @FXML private Button btnLogout;

    @FXML private Label lblBreadcrumb;
    @FXML private ComboBox<String> cmbSelectHall;
    @FXML private HBox boxHallCardsSerial;
    @FXML private Button btnBack;
    @FXML private Button btnRefresh;
    @FXML private Label lblHallNameHeader;
    @FXML private Label lblHallTypeBadge;
    @FXML private Label lblHallProvostSummary;
    @FXML private ProgressBar progressOccupancy;
    @FXML private Label lblOccupancyPercent;
    @FXML private Label lblFeedback;

    private int userAssignedHallId = 1;
    private final List<Hall> serialHallsList = new ArrayList<>();

    @FXML private Label lblKpiFloors;
    @FXML private Label lblKpiRooms;
    @FXML private Label lblKpiSeats;
    @FXML private Label lblKpiOccupied;
    @FXML private Label lblKpiVacant;
    @FXML private Label lblKpiStaff;
    @FXML private Label lblKpiAc;
    @FXML private Label lblKpiFans;
    @FXML private Label lblKpiLights;
    @FXML private Label lblKpiWashrooms;
    @FXML private Label lblKpiCctv;
    @FXML private Label lblKpiFireSafety;
    @FXML private Label lblKpiMaintenance;

    @FXML private ScrollPane scrollTabBar;
    @FXML private HBox boxTabButtons;
    @FXML private Button btnTabScrollLeft;
    @FXML private Button btnTabScrollRight;

    @FXML private Button tabBtnOverview;
    @FXML private Button tabBtnBuilding;
    @FXML private Button tabBtnFloors;
    @FXML private Button tabBtnRooms;
    @FXML private Button tabBtnElectrical;
    @FXML private Button tabBtnFurniture;
    @FXML private Button tabBtnAc;
    @FXML private Button tabBtnLighting;
    @FXML private Button tabBtnWashrooms;
    @FXML private Button tabBtnWater;
    @FXML private Button tabBtnDining;
    @FXML private Button tabBtnNetwork;
    @FXML private Button tabBtnCctv;
    @FXML private Button tabBtnFire;
    @FXML private Button tabBtnMaintenance;
    @FXML private Button tabBtnCleaning;
    @FXML private Button tabBtnContacts;
    @FXML private Button tabBtnUtilities;
    @FXML private Button tabBtnAssets;

    @FXML private StackPane tabContentStack;
    @FXML private VBox paneOverview;
    @FXML private VBox paneBuilding;
    @FXML private VBox paneFloors;
    @FXML private VBox paneRooms;
    @FXML private VBox paneElectrical;
    @FXML private VBox paneFurniture;
    @FXML private VBox paneAc;
    @FXML private VBox paneLighting;
    @FXML private VBox paneWashrooms;
    @FXML private VBox paneWater;
    @FXML private VBox paneDining;
    @FXML private VBox paneNetwork;
    @FXML private VBox paneCctv;
    @FXML private VBox paneFire;
    @FXML private VBox paneMaintenance;
    @FXML private VBox paneCleaning;
    @FXML private VBox paneContacts;
    @FXML private VBox paneUtilities;
    @FXML private VBox paneAssets;

    @FXML private Label lblOverviewName;
    @FXML private Label lblOverviewCode;
    @FXML private Label lblOverviewEst;
    @FXML private Label lblOverviewAddress;
    @FXML private Label lblOverviewResStudents;
    @FXML private Label lblOverviewNonResStudents;
    @FXML private Label lblOverviewProvost;
    @FXML private Label lblOverviewAsstProvost;
    @FXML private Label lblOverviewOfficePhone;
    @FXML private Label lblOverviewEmergencyPhone;
    @FXML private Label lblOverviewEmail;
    @FXML private Label lblOverviewStaffCount;
    @FXML private Label lblOverviewDesc;

    @FXML private FlowPane flowBuildingSpecs;

    @FXML private TableView<FloorInfo> tableFloors;
    @FXML private TableColumn<FloorInfo, Integer> colFloorNum;
    @FXML private TableColumn<FloorInfo, Integer> colFloorRooms;
    @FXML private TableColumn<FloorInfo, Integer> colFloorSeats;
    @FXML private TableColumn<FloorInfo, Integer> colFloorOccupied;
    @FXML private TableColumn<FloorInfo, Integer> colFloorVacant;
    @FXML private TableColumn<FloorInfo, String> colFloorOccRate;
    @FXML private TableColumn<FloorInfo, Integer> colFloorAc;
    @FXML private TableColumn<FloorInfo, Integer> colFloorFans;
    @FXML private TableColumn<FloorInfo, Integer> colFloorLights;
    @FXML private TableColumn<FloorInfo, Integer> colFloorWashrooms;
    @FXML private TableColumn<FloorInfo, Integer> colFloorTaps;
    @FXML private TableColumn<FloorInfo, Integer> colFloorExits;
    @FXML private TableColumn<FloorInfo, Void> colFloorAction;

    @FXML private TextField txtSearchRoom;
    @FXML private ComboBox<String> cmbFloorFilter;
    @FXML private TableView<RoomFullProfile> tableRooms;
    @FXML private TableColumn<RoomFullProfile, Integer> colRoomNum;
    @FXML private TableColumn<RoomFullProfile, Integer> colRoomFloor;
    @FXML private TableColumn<RoomFullProfile, String> colRoomType;
    @FXML private TableColumn<RoomFullProfile, Integer> colRoomCapacity;
    @FXML private TableColumn<RoomFullProfile, Integer> colRoomOccupiedSeats;
    @FXML private TableColumn<RoomFullProfile, Integer> colRoomVacantSeats;
    @FXML private TableColumn<RoomFullProfile, String> colRoomStatus;
    @FXML private TableColumn<RoomFullProfile, Integer> colRoomAcCount;
    @FXML private TableColumn<RoomFullProfile, Integer> colRoomFansCount;
    @FXML private TableColumn<RoomFullProfile, Integer> colRoomLightsCount;
    @FXML private TableColumn<RoomFullProfile, String> colRoomCondition;
    @FXML private TableColumn<RoomFullProfile, Void> colRoomAction;

    @FXML private Label lblElecAcRollup;
    @FXML private Label lblElecFansRollup;
    @FXML private Label lblElecLightsRollup;
    @FXML private Label lblElecEmergLights;
    @FXML private TableView<RoomFullProfile> tableRoomElec;
    @FXML private TableColumn<RoomFullProfile, Integer> colElecRoomNum;
    @FXML private TableColumn<RoomFullProfile, Integer> colElecAc;
    @FXML private TableColumn<RoomFullProfile, Integer> colElecCeilingFans;
    @FXML private TableColumn<RoomFullProfile, Integer> colElecLedLights;
    @FXML private TableColumn<RoomFullProfile, Integer> colElecTubeLights;
    @FXML private TableColumn<RoomFullProfile, Integer> colElecSockets;
    @FXML private TableColumn<RoomFullProfile, Integer> colElecSwitches;

    @FXML private TableView<RoomFullProfile> tableRoomFurn;
    @FXML private TableColumn<RoomFullProfile, Integer> colFurnRoomNum;
    @FXML private TableColumn<RoomFullProfile, Integer> colFurnBeds;
    @FXML private TableColumn<RoomFullProfile, Integer> colFurnTables;
    @FXML private TableColumn<RoomFullProfile, Integer> colFurnChairs;
    @FXML private TableColumn<RoomFullProfile, Integer> colFurnWardrobes;
    @FXML private TableColumn<RoomFullProfile, Integer> colFurnBookshelves;
    @FXML private TableColumn<RoomFullProfile, Integer> colFurnGood;
    @FXML private TableColumn<RoomFullProfile, Integer> colFurnDamaged;
    @FXML private TableColumn<RoomFullProfile, Integer> colFurnUnderRepair;

    @FXML private TableView<EquipmentItem> tableAcInventory;
    @FXML private TableColumn<EquipmentItem, String> colAcCode;
    @FXML private TableColumn<EquipmentItem, String> colAcType;
    @FXML private TableColumn<EquipmentItem, String> colAcLocation;
    @FXML private TableColumn<EquipmentItem, String> colAcBrand;
    @FXML private TableColumn<EquipmentItem, String> colAcCapacity;
    @FXML private TableColumn<EquipmentItem, String> colAcCondition;
    @FXML private TableColumn<EquipmentItem, String> colAcNextService;

    @FXML private TableView<EquipmentItem> tableLightingInventory;
    @FXML private TableColumn<EquipmentItem, String> colLightCode;
    @FXML private TableColumn<EquipmentItem, String> colLightType;
    @FXML private TableColumn<EquipmentItem, String> colLightLocation;
    @FXML private TableColumn<EquipmentItem, String> colLightRating;
    @FXML private TableColumn<EquipmentItem, String> colLightCondition;
    @FXML private TableColumn<EquipmentItem, String> colLightRemarks;

    @FXML private FlowPane flowWashrooms;

    @FXML private Label lblWaterSource;
    @FXML private Label lblWaterDeepWell;
    @FXML private Label lblWaterPumps;
    @FXML private Label lblWaterTanks;
    @FXML private Label lblWaterDrinkingPoints;
    @FXML private Label lblWaterFilters;
    @FXML private Label lblWaterLastClean;
    @FXML private Label lblWaterNextClean;

    @FXML private Label lblDiningCapacity;
    @FXML private Label lblDiningTables;
    @FXML private Label lblDiningChairs;
    @FXML private Label lblDiningCooling;
    @FXML private Label lblDiningBasins;
    @FXML private Label lblKitchenArea;
    @FXML private Label lblKitchenBurners;
    @FXML private Label lblKitchenFridges;
    @FXML private Label lblKitchenExhaust;
    @FXML private Label lblKitchenStaff;

    @FXML private TableView<EquipmentItem> tableNetwork;
    @FXML private TableColumn<EquipmentItem, String> colNetCode;
    @FXML private TableColumn<EquipmentItem, String> colNetType;
    @FXML private TableColumn<EquipmentItem, String> colNetLocation;
    @FXML private TableColumn<EquipmentItem, String> colNetBrand;
    @FXML private TableColumn<EquipmentItem, String> colNetCapacity;
    @FXML private TableColumn<EquipmentItem, String> colNetStatus;

    @FXML private TableView<EquipmentItem> tableCctv;
    @FXML private TableColumn<EquipmentItem, String> colCctvCode;
    @FXML private TableColumn<EquipmentItem, String> colCctvType;
    @FXML private TableColumn<EquipmentItem, String> colCctvLocation;
    @FXML private TableColumn<EquipmentItem, String> colCctvModel;
    @FXML private TableColumn<EquipmentItem, String> colCctvStatus;
    @FXML private TableColumn<EquipmentItem, String> colCctvRemarks;

    @FXML private TableView<EquipmentItem> tableFireSafety;
    @FXML private TableColumn<EquipmentItem, String> colFireCode;
    @FXML private TableColumn<EquipmentItem, String> colFireType;
    @FXML private TableColumn<EquipmentItem, String> colFireLocation;
    @FXML private TableColumn<EquipmentItem, String> colFireCapacity;
    @FXML private TableColumn<EquipmentItem, String> colFireStatus;
    @FXML private TableColumn<EquipmentItem, String> colFireNextService;

    @FXML private ComboBox<String> cmbMaintenanceFilter;
    @FXML private Button btnNewMaintenance;
    @FXML private TableView<MaintenanceRecord> tableMaintenance;
    @FXML private TableColumn<MaintenanceRecord, String> colMntCode;
    @FXML private TableColumn<MaintenanceRecord, String> colMntLocation;
    @FXML private TableColumn<MaintenanceRecord, String> colMntCategory;
    @FXML private TableColumn<MaintenanceRecord, String> colMntProblem;
    @FXML private TableColumn<MaintenanceRecord, String> colMntReportedBy;
    @FXML private TableColumn<MaintenanceRecord, String> colMntPriority;
    @FXML private TableColumn<MaintenanceRecord, String> colMntStatus;
    @FXML private TableColumn<MaintenanceRecord, Double> colMntCost;
    @FXML private TableColumn<MaintenanceRecord, Void> colMntAction;

    @FXML private TableView<CleaningSchedule> tableCleaning;
    @FXML private TableColumn<CleaningSchedule, String> colCleanArea;
    @FXML private TableColumn<CleaningSchedule, String> colCleanFreq;
    @FXML private TableColumn<CleaningSchedule, String> colCleanStaff;
    @FXML private TableColumn<CleaningSchedule, String> colCleanStatus;
    @FXML private TableColumn<CleaningSchedule, String> colCleanLastDate;
    @FXML private TableColumn<CleaningSchedule, String> colCleanNextDate;

    @FXML private TableView<HallContact> tableContacts;
    @FXML private TableColumn<HallContact, String> colContactCategory;
    @FXML private TableColumn<HallContact, String> colContactDesignation;
    @FXML private TableColumn<HallContact, String> colContactName;
    @FXML private TableColumn<HallContact, String> colContactPhone;
    @FXML private TableColumn<HallContact, String> colContactEmail;
    @FXML private TableColumn<HallContact, String> colContactAvailability;

    @FXML private TableView<UtilityRecord> tableUtilities;
    @FXML private TableColumn<UtilityRecord, String> colUtilMonth;
    @FXML private TableColumn<UtilityRecord, Double> colUtilElec;
    @FXML private TableColumn<UtilityRecord, Double> colUtilWater;
    @FXML private TableColumn<UtilityRecord, Double> colUtilInternet;
    @FXML private TableColumn<UtilityRecord, Double> colUtilGas;
    @FXML private TableColumn<UtilityRecord, Double> colUtilMnt;
    @FXML private TableColumn<UtilityRecord, Double> colUtilTotal;
    @FXML private TableColumn<UtilityRecord, String> colUtilStatus;

    @FXML private TextField txtSearchAsset;
    @FXML private ComboBox<String> cmbAssetCategory;
    @FXML private Button btnAddAsset;
    @FXML private TableView<AssetItem> tableAssets;
    @FXML private TableColumn<AssetItem, String> colAssetCode;
    @FXML private TableColumn<AssetItem, String> colAssetName;
    @FXML private TableColumn<AssetItem, String> colAssetCategory;
    @FXML private TableColumn<AssetItem, Integer> colAssetQty;
    @FXML private TableColumn<AssetItem, String> colAssetLocation;
    @FXML private TableColumn<AssetItem, Double> colAssetPrice;
    @FXML private TableColumn<AssetItem, String> colAssetCondition;
    @FXML private TableColumn<AssetItem, String> colAssetWarranty;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        isProvost = currentUser != null && currentUser.isProvost();

        setupSidebar();
        setupHallSelector();
        setupTableColumns();
        setupFilters();

        loadAllData();
    }

    private void setupSidebar() {
        if (currentUser != null) {
            lblUserId.setText("User ID: " + currentUser.getUserId());
            lblRole.setText("Role: " + (isProvost ? "Provost" : "Student"));

            if (isProvost) {
                lblPortalTitle.setText("PROVOST PORTAL");
                boxSecondaryNavStudent.setVisible(false);
                boxSecondaryNavStudent.setManaged(false);
                boxSecondaryNavProvost.setVisible(true);
                boxSecondaryNavProvost.setManaged(true);
                lblNavSecondarySection.setText("RESIDENCE MANAGEMENT");
                btnNavBillingOrRequests.setText("📋  Payment Requests");
                BadgeHelper.updateAllProvostBadges(btnNavBillingOrRequests, btnNavHallChangeRequests, btnNavRoomChangeRequests, btnNavComplaints, btnNavHallLeaveRequests, currentUser.getUserId());

                try {
                    Hall provostHall = hallDAO.getHallByProvostUserId(currentUser.getUserId());
                    if (provostHall != null) {
                        selectedHallId = provostHall.getId();
                        userAssignedHallId = provostHall.getId();
                    }
                } catch (SQLException ignored) {}
            } else {
                lblPortalTitle.setText("STUDENT PORTAL");
                boxSecondaryNavStudent.setVisible(true);
                boxSecondaryNavStudent.setManaged(true);
                boxSecondaryNavProvost.setVisible(false);
                boxSecondaryNavProvost.setManaged(false);
                lblNavSecondarySection.setText("RESIDENCE SERVICES");
                btnNavBillingOrRequests.setText("💳  Bill & Due");
                BadgeHelper.updateAllStudentBadges(btnNavNoticeBoard, btnNavBillingOrRequests, btnNavStudentHallChange, btnNavStudentRoomChange, btnNavStudentComplaints, btnNavStudentHallLeave, currentUser.getUserId());

                try {
                    currentStudent = studentDAO.getStudentByUserId(currentUser.getUserId());
                    if (currentStudent != null && currentStudent.getCurrentHallId() != null) {
                        selectedHallId = currentStudent.getCurrentHallId();
                        userAssignedHallId = currentStudent.getCurrentHallId();
                    }
                } catch (SQLException ignored) {}
            }
        }
    }

    private void setupHallSelector() {
        try {
            List<Hall> allHalls = hallDAO.getAllHalls();
            serialHallsList.clear();

            Hall assignedHall = null;
            for (Hall h : allHalls) {
                if (h.getId() == userAssignedHallId) {
                    assignedHall = h;
                    break;
                }
            }
            if (assignedHall != null) {
                serialHallsList.add(assignedHall);
            }

            for (Hall h : allHalls) {
                if (assignedHall == null || h.getId() != assignedHall.getId()) {
                    serialHallsList.add(h);
                }
            }

            ObservableList<String> hallNames = FXCollections.observableArrayList();
            int selectedIndex = 0;
            for (int i = 0; i < serialHallsList.size(); i++) {
                Hall h = serialHallsList.get(i);
                boolean isUserHall = (h.getId() == userAssignedHallId);
                hallNames.add((isUserHall ? "⭐ " : "🏢 ") + h.getHallName() + " (" + h.getHallType() + ")" + (isUserHall ? " [YOUR RESIDENCE]" : ""));
                if (h.getId() == selectedHallId) {
                    selectedIndex = i;
                }
            }
            cmbSelectHall.setItems(hallNames);
            if (!hallNames.isEmpty()) {
                cmbSelectHall.getSelectionModel().select(selectedIndex);
            }

            renderHallCardsSerial();
        } catch (SQLException e) {
            System.err.println("[HallInfoController] Error loading halls: " + e.getMessage());
        }
    }

    private void renderHallCardsSerial() {
        if (boxHallCardsSerial == null) return;
        boxHallCardsSerial.getChildren().clear();

        for (int i = 0; i < serialHallsList.size(); i++) {
            Hall h = serialHallsList.get(i);
            boolean isUserHall = (h.getId() == userAssignedHallId);
            boolean isSelected = (h.getId() == selectedHallId);

            VBox card = new VBox(6);
            card.setPrefWidth(250);
            card.setMinWidth(230);
            card.getStyleClass().add(isSelected ? "hall-card-active" : "hall-card");

            HBox topRow = new HBox(8);
            topRow.setAlignment(Pos.CENTER_LEFT);
            Label badge = new Label(isUserHall ? "⭐ YOUR RESIDENCE" : ("🏢 HALL 0" + (i + 1)));
            badge.getStyleClass().add(isUserHall ? "hall-card-current-badge" : "hall-card-other-badge");

            Label typeBadge = new Label(h.getHallType());
            typeBadge.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #90caf9; -fx-padding: 2 6; -fx-background-radius: 6; -fx-font-size: 10px; -fx-font-weight: bold;");

            topRow.getChildren().addAll(badge, typeBadge);

            Label lblName = new Label(h.getHallName());
            lblName.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13.5px; -fx-font-weight: bold;");
            lblName.setWrapText(true);

            Label lblStats = new Label("Loading stats...");
            lblStats.setStyle("-fx-text-fill: #81c784; -fx-font-size: 11.5px; -fx-font-weight: bold;");

            try {
                HallSummaryStats s = hallDAO.getHallSummaryStats(h.getId());
                lblStats.setText("Occupancy: " + s.getOccupancyPercentageFormatted() + " (" + s.getOccupiedSeats() + "/" + s.getTotalSeats() + " Seats)");
            } catch (SQLException ignored) {
                lblStats.setText("Capacity: 28 Seats");
            }

            Label lblAction = new Label(isSelected ? "✓ Active Profile" : "Click to view profile ➔");
            lblAction.setStyle(isSelected ? "-fx-text-fill: #64b5f6; -fx-font-size: 11px; -fx-font-weight: bold;" : "-fx-text-fill: #78909c; -fx-font-size: 11px;");

            card.getChildren().addAll(topRow, lblName, lblStats, lblAction);

            card.setOnMouseClicked(event -> {
                if (selectedHallId != h.getId()) {
                    selectedHallId = h.getId();
                    int idx = serialHallsList.indexOf(h);
                    if (idx >= 0 && idx < cmbSelectHall.getItems().size()) {
                        cmbSelectHall.getSelectionModel().select(idx);
                    }
                    renderHallCardsSerial();
                    loadAllData();
                }
            });

            boxHallCardsSerial.getChildren().add(card);
        }
    }

    @FXML
    private void onHallSelected(ActionEvent event) {
        int idx = cmbSelectHall.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < serialHallsList.size()) {
            Hall h = serialHallsList.get(idx);
            if (selectedHallId != h.getId()) {
                selectedHallId = h.getId();
                renderHallCardsSerial();
                loadAllData();
            }
        }
    }

    private void setupFilters() {
        cmbFloorFilter.setItems(FXCollections.observableArrayList("All Floors", "Floor 1", "Floor 2", "Floor 3", "Floor 4"));
        cmbFloorFilter.getSelectionModel().selectFirst();

        cmbMaintenanceFilter.setItems(FXCollections.observableArrayList("ALL", "PENDING", "IN_PROGRESS", "COMPLETED"));
        cmbMaintenanceFilter.getSelectionModel().selectFirst();

        cmbAssetCategory.setItems(FXCollections.observableArrayList("ALL", "ELECTRICAL", "FURNITURE", "IT_EQUIPMENT", "SPORTS", "SAFETY", "CLEANING", "OTHER"));
        cmbAssetCategory.getSelectionModel().selectFirst();

        if (!isProvost) {
            btnAddAsset.setVisible(false);
            btnAddAsset.setManaged(false);
        }
    }

    private void setupTableColumns() {

        colFloorNum.setCellValueFactory(new PropertyValueFactory<>("floorNumber"));
        colFloorRooms.setCellValueFactory(new PropertyValueFactory<>("totalRooms"));
        colFloorSeats.setCellValueFactory(new PropertyValueFactory<>("totalSeats"));
        colFloorOccupied.setCellValueFactory(new PropertyValueFactory<>("occupiedSeats"));
        colFloorVacant.setCellValueFactory(new PropertyValueFactory<>("vacantSeats"));
        colFloorOccRate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getOccupancyRateDisplay()));
        colFloorAc.setCellValueFactory(new PropertyValueFactory<>("acCount"));
        colFloorFans.setCellValueFactory(new PropertyValueFactory<>("fansCount"));
        colFloorLights.setCellValueFactory(new PropertyValueFactory<>("lightsCount"));
        colFloorWashrooms.setCellValueFactory(new PropertyValueFactory<>("washroomsCount"));
        colFloorTaps.setCellValueFactory(new PropertyValueFactory<>("waterTaps"));
        colFloorExits.setCellValueFactory(new PropertyValueFactory<>("emergencyExits"));
        setupFloorActionColumn();

        colRoomNum.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colRoomFloor.setCellValueFactory(new PropertyValueFactory<>("floorNumber"));
        colRoomType.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        colRoomCapacity.setCellValueFactory(new PropertyValueFactory<>("totalSeats"));
        colRoomOccupiedSeats.setCellValueFactory(new PropertyValueFactory<>("occupiedSeats"));
        colRoomVacantSeats.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
        colRoomStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colRoomAcCount.setCellValueFactory(new PropertyValueFactory<>("acCount"));
        colRoomFansCount.setCellValueFactory(new PropertyValueFactory<>("totalFans"));
        colRoomLightsCount.setCellValueFactory(new PropertyValueFactory<>("totalLights"));
        colRoomCondition.setCellValueFactory(new PropertyValueFactory<>("conditionStatus"));
        setupRoomActionColumn();

        colElecRoomNum.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colElecAc.setCellValueFactory(new PropertyValueFactory<>("acCount"));
        colElecCeilingFans.setCellValueFactory(new PropertyValueFactory<>("ceilingFanCount"));
        colElecLedLights.setCellValueFactory(new PropertyValueFactory<>("ledLightCount"));
        colElecTubeLights.setCellValueFactory(new PropertyValueFactory<>("tubeLightCount"));
        colElecSockets.setCellValueFactory(new PropertyValueFactory<>("socketCount"));
        colElecSwitches.setCellValueFactory(new PropertyValueFactory<>("switchCount"));

        colFurnRoomNum.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colFurnBeds.setCellValueFactory(new PropertyValueFactory<>("bedCount"));
        colFurnTables.setCellValueFactory(new PropertyValueFactory<>("tableCount"));
        colFurnChairs.setCellValueFactory(new PropertyValueFactory<>("chairCount"));
        colFurnWardrobes.setCellValueFactory(new PropertyValueFactory<>("wardrobeCount"));
        colFurnBookshelves.setCellValueFactory(new PropertyValueFactory<>("bookshelfCount"));
        colFurnGood.setCellValueFactory(new PropertyValueFactory<>("goodConditionCount"));
        colFurnDamaged.setCellValueFactory(new PropertyValueFactory<>("damagedCount"));
        colFurnUnderRepair.setCellValueFactory(new PropertyValueFactory<>("underRepairCount"));

        colAcCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        colAcType.setCellValueFactory(new PropertyValueFactory<>("equipmentType"));
        colAcLocation.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLocationDisplay()));
        colAcBrand.setCellValueFactory(new PropertyValueFactory<>("brandModel"));
        colAcCapacity.setCellValueFactory(new PropertyValueFactory<>("capacityRating"));
        colAcCondition.setCellValueFactory(new PropertyValueFactory<>("conditionStatus"));
        colAcNextService.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNextServiceDateDisplay()));

        colLightCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        colLightType.setCellValueFactory(new PropertyValueFactory<>("equipmentType"));
        colLightLocation.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLocationDisplay()));
        colLightRating.setCellValueFactory(new PropertyValueFactory<>("capacityRating"));
        colLightCondition.setCellValueFactory(new PropertyValueFactory<>("conditionStatus"));
        colLightRemarks.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        colNetCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        colNetType.setCellValueFactory(new PropertyValueFactory<>("equipmentType"));
        colNetLocation.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLocationDisplay()));
        colNetBrand.setCellValueFactory(new PropertyValueFactory<>("brandModel"));
        colNetCapacity.setCellValueFactory(new PropertyValueFactory<>("capacityRating"));
        colNetStatus.setCellValueFactory(new PropertyValueFactory<>("conditionStatus"));

        colCctvCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        colCctvType.setCellValueFactory(new PropertyValueFactory<>("equipmentType"));
        colCctvLocation.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLocationDisplay()));
        colCctvModel.setCellValueFactory(new PropertyValueFactory<>("brandModel"));
        colCctvStatus.setCellValueFactory(new PropertyValueFactory<>("conditionStatus"));
        colCctvRemarks.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        colFireCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        colFireType.setCellValueFactory(new PropertyValueFactory<>("equipmentType"));
        colFireLocation.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLocationDisplay()));
        colFireCapacity.setCellValueFactory(new PropertyValueFactory<>("capacityRating"));
        colFireStatus.setCellValueFactory(new PropertyValueFactory<>("conditionStatus"));
        colFireNextService.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNextServiceDateDisplay()));

        colMntCode.setCellValueFactory(new PropertyValueFactory<>("recordCode"));
        colMntLocation.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLocationDisplay()));
        colMntCategory.setCellValueFactory(new PropertyValueFactory<>("equipmentCategory"));
        colMntProblem.setCellValueFactory(new PropertyValueFactory<>("problemDescription"));
        colMntReportedBy.setCellValueFactory(new PropertyValueFactory<>("reportedBy"));
        colMntPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colMntStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colMntCost.setCellValueFactory(new PropertyValueFactory<>("estimatedCost"));
        setupMaintenanceActionColumn();

        colCleanArea.setCellValueFactory(new PropertyValueFactory<>("areaType"));
        colCleanFreq.setCellValueFactory(new PropertyValueFactory<>("frequency"));
        colCleanStaff.setCellValueFactory(new PropertyValueFactory<>("responsibleStaff"));
        colCleanStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colCleanLastDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLastCleanedDateDisplay()));
        colCleanNextDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNextScheduledDateDisplay()));

        colContactCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colContactDesignation.setCellValueFactory(new PropertyValueFactory<>("designation"));
        colContactName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colContactPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colContactEmail.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmail() != null ? cell.getValue().getEmail() : "—"));
        colContactAvailability.setCellValueFactory(new PropertyValueFactory<>("availability"));

        colUtilMonth.setCellValueFactory(new PropertyValueFactory<>("billingMonth"));
        colUtilElec.setCellValueFactory(new PropertyValueFactory<>("electricityBill"));
        colUtilWater.setCellValueFactory(new PropertyValueFactory<>("waterBill"));
        colUtilInternet.setCellValueFactory(new PropertyValueFactory<>("internetBill"));
        colUtilGas.setCellValueFactory(new PropertyValueFactory<>("gasBill"));
        colUtilMnt.setCellValueFactory(new PropertyValueFactory<>("maintenanceCost"));
        colUtilTotal.setCellValueFactory(new PropertyValueFactory<>("totalExpense"));
        colUtilStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        colAssetCode.setCellValueFactory(new PropertyValueFactory<>("assetCode"));
        colAssetName.setCellValueFactory(new PropertyValueFactory<>("assetName"));
        colAssetCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colAssetQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colAssetLocation.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLocationDisplay()));
        colAssetPrice.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        colAssetCondition.setCellValueFactory(new PropertyValueFactory<>("conditionStatus"));
        colAssetWarranty.setCellValueFactory(new PropertyValueFactory<>("warrantyInfo"));
    }

    private void setupFloorActionColumn() {
        colFloorAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnView = new Button("🔍 View Rooms");
            {
                btnView.getStyleClass().add("btn-action-primary");
                btnView.setOnAction(event -> {
                    FloorInfo f = getTableView().getItems().get(getIndex());
                    cmbFloorFilter.getSelectionModel().select("Floor " + f.getFloorNumber());
                    selectTab(3);
                    loadRoomData();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnView);
            }
        });
    }

    private void setupRoomActionColumn() {
        colRoomAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnDetail = new Button("🔍 View Details");
            {
                btnDetail.getStyleClass().add("btn-action-primary");
                btnDetail.setOnAction(event -> {
                    RoomFullProfile r = getTableView().getItems().get(getIndex());
                    showRoomDetailModal(r.getRoomId());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDetail);
            }
        });
    }

    private void setupMaintenanceActionColumn() {
        colMntAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnUpdate = new Button("⚙️ Update");
            {
                btnUpdate.getStyleClass().add("btn-action-secondary");
                btnUpdate.setOnAction(event -> {
                    MaintenanceRecord rec = getTableView().getItems().get(getIndex());
                    showUpdateMaintenanceDialog(rec);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !isProvost) {
                    setGraphic(null);
                } else {
                    setGraphic(btnUpdate);
                }
            }
        });
    }

    public void loadAllData() {
        try {

            HallSummaryStats stats = hallDAO.getHallSummaryStats(selectedHallId);
            boolean isUserResidence = (selectedHallId == userAssignedHallId);
            lblHallNameHeader.setText(stats.getHallName());
            if (isUserResidence) {
                lblHallTypeBadge.setText("⭐ YOUR CURRENT RESIDENCE | " + stats.getHallType());
                lblHallTypeBadge.setStyle("-fx-background-color: rgba(0,230,118,0.2); -fx-text-fill: #00e676; -fx-padding: 3 10; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
            } else {
                lblHallTypeBadge.setText("🏢 RUET RESIDENTIAL HALL | " + stats.getHallType());
                lblHallTypeBadge.setStyle("-fx-background-color: rgba(66,165,245,0.2); -fx-text-fill: #90caf9; -fx-padding: 3 10; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
            }

            progressOccupancy.setProgress(stats.getTotalSeats() > 0 ? ((double) stats.getOccupiedSeats() / stats.getTotalSeats()) : 0.0);
            lblOccupancyPercent.setText(stats.getOccupancyPercentageFormatted() + " Occupied (" + stats.getOccupiedSeats() + "/" + stats.getTotalSeats() + ")");

            lblKpiFloors.setText(String.valueOf(stats.getTotalFloors()));
            lblKpiRooms.setText(String.valueOf(stats.getTotalRooms()));
            lblKpiSeats.setText(String.valueOf(stats.getTotalSeats()));
            lblKpiOccupied.setText(String.valueOf(stats.getOccupiedSeats()));
            lblKpiVacant.setText(String.valueOf(stats.getVacantSeats()));
            lblKpiStaff.setText(String.valueOf(stats.getTotalStaff()));

            lblKpiAc.setText(String.valueOf(stats.getTotalAc()));
            lblKpiFans.setText(String.valueOf(stats.getTotalFans()));
            lblKpiLights.setText(String.valueOf(stats.getTotalLights()));
            lblKpiWashrooms.setText(String.valueOf(stats.getTotalWashrooms()));
            lblKpiCctv.setText(String.valueOf(stats.getTotalCctv()));
            lblKpiFireSafety.setText(String.valueOf(stats.getTotalFireSafety()));
            lblKpiMaintenance.setText(String.valueOf(stats.getPendingMaintenance()));

            lblElecAcRollup.setText(String.valueOf(stats.getTotalAc()) + " Units");
            lblElecFansRollup.setText(String.valueOf(stats.getTotalFans()) + " Units");
            lblElecLightsRollup.setText(String.valueOf(stats.getTotalLights()) + " Fixtures");
            lblElecEmergLights.setText(String.valueOf(stats.getTotalRooms()) + " Backup Lights");

            HallDetail detail = hallDAO.getHallDetail(selectedHallId);
            if (detail != null) {
                lblOverviewName.setText(detail.getHallName() + " (" + detail.getHallType() + ")");
                lblOverviewCode.setText(detail.getHallCode());
                lblOverviewEst.setText(String.valueOf(detail.getEstablishedYear()));
                lblOverviewAddress.setText(detail.getAddress());
                lblOverviewResStudents.setText(String.valueOf(stats.getResidentialStudents()) + " Students");
                lblOverviewNonResStudents.setText(String.valueOf(detail.getNonResidentialStudents()) + " Attached Students");
                lblOverviewProvost.setText(detail.getProvostName() + (detail.getProvostPhone() != null ? " (" + detail.getProvostPhone() + ")" : ""));
                lblOverviewAsstProvost.setText(detail.getAssistantProvosts() != null ? detail.getAssistantProvosts() : "Dr. M. S. Rahman, Dr. K. Ahmed");
                lblOverviewOfficePhone.setText(detail.getOfficeContact());
                lblOverviewEmergencyPhone.setText(detail.getEmergencyContact());
                lblOverviewEmail.setText(detail.getEmail());
                lblOverviewStaffCount.setText(detail.getTotalStaff() + " Administrative & Support Staff");
                lblOverviewDesc.setText(detail.getDescription());
            }

            renderBuildingSpecs();

            floorList.setAll(hallDAO.getFloorsByHallId(selectedHallId));
            tableFloors.setItems(floorList);

            loadRoomData();

            acList.setAll(hallDAO.getEquipmentInventory(selectedHallId, "AC"));
            tableAcInventory.setItems(acList);

            lightingList.setAll(hallDAO.getEquipmentInventory(selectedHallId, "LIGHT"));
            tableLightingInventory.setItems(lightingList);

            networkList.setAll(hallDAO.getEquipmentInventory(selectedHallId, "NETWORK"));
            tableNetwork.setItems(networkList);

            cctvList.setAll(hallDAO.getEquipmentInventory(selectedHallId, "CCTV"));
            tableCctv.setItems(cctvList);

            fireSafetyList.setAll(hallDAO.getEquipmentInventory(selectedHallId, "FIRE_SAFETY"));
            tableFireSafety.setItems(fireSafetyList);

            renderWashroomCards();

            WaterSystemInfo water = hallDAO.getWaterSystemByHallId(selectedHallId);
            if (water != null) {
                lblWaterSource.setText(water.getSourceType());
                lblWaterDeepWell.setText(water.getDeepTubeWell());
                lblWaterPumps.setText(water.getPumpCount() + " Centrifugal Submersible (" + water.getPumpCondition() + ")");
                lblWaterTanks.setText(water.getTankCount() + " Tanks (Total: " + String.format("%,d", water.getTotalTankCapacityLiters()) + " Liters)");
                lblWaterDrinkingPoints.setText(water.getDrinkingWaterPoints() + " Filtered Drinking Points (" + water.getTapCount() + " Total Taps)");
                lblWaterFilters.setText(water.getFiltersCount() + " Heavy Sediment & " + water.getPurifiersCount() + " UV Purifiers");
                lblWaterLastClean.setText(water.getLastCleaningDateDisplay());
                lblWaterNextClean.setText(water.getNextCleaningDateDisplay());
            }

            KitchenDiningInfo kd = hallDAO.getKitchenDiningByHallId(selectedHallId);
            if (kd != null) {
                lblDiningCapacity.setText(kd.getDiningCapacity() + " Seats Simultaneous Dining");
                lblDiningTables.setText(kd.getDiningTableCount() + " Stainless Steel Tables");
                lblDiningChairs.setText(kd.getDiningChairCount() + " Ergonomic Chairs");
                lblDiningCooling.setText(kd.getDiningAc() + " Cassette ACs, " + kd.getDiningFans() + " Ceiling Fans");
                lblDiningBasins.setText(kd.getWashBasinCount() + " Handwash Basins with Liquid Soap");
                lblKitchenArea.setText(kd.getKitchenSizeSqft() + " Sq Ft (" + kd.getCookingAreaDesc() + ")");
                lblKitchenBurners.setText(kd.getStoveCount() + " Industrial Stoves (" + kd.getGasBurnerCount() + " Burners)");
                lblKitchenFridges.setText(kd.getRefrigeratorCount() + " Commercial Fridges, " + kd.getFreezerCount() + " Deep Freezers");
                lblKitchenExhaust.setText(kd.getExhaustFanCount() + " High-Velocity Industrial Exhaust Fans");
                lblKitchenStaff.setText(kd.getKitchenStaffCount() + " Full-Time Cooks & Helpers");
            }

            loadMaintenanceData();

            cleaningList.setAll(hallDAO.getCleaningSchedules(selectedHallId));
            tableCleaning.setItems(cleaningList);

            contactList.setAll(hallDAO.getHallContacts(selectedHallId));
            tableContacts.setItems(contactList);

            utilityList.setAll(hallDAO.getUtilityRecords(selectedHallId));
            tableUtilities.setItems(utilityList);

            loadAssetData();

        } catch (SQLException e) {
            System.err.println("[HallInfoController] Error loading data: " + e.getMessage());
        }
    }

    private void renderBuildingSpecs() {
        flowBuildingSpecs.getChildren().clear();
        String[][] specs = {
            {"Main Complex Name", "Main Residential Complex (Bhaban " + selectedHallId + ")", "#42a5f5"},
            {"Total Floors", "4 Stories (RCC Frame)", "#ab47bc"},
            {"Total Residential Rooms", "74 Student Rooms", "#66bb6a"},
            {"Guest Rooms", "2 VIP Guest Suites (Attached Bath & AC)", "#ffa726"},
            {"Central Dining Hall", "Capacity: 200 Students", "#26a69a"},
            {"Central Kitchen", "650 Sq Ft Industrial Culinary Block", "#ef5350"},
            {"Main Reading Room", "Capacity: 60 Students (24/7 Wi-Fi)", "#5c6bc0"},
            {"Mosque & Prayer Hall", "Capacity: 80 Persons (Carpeted)", "#29b6f6"},
            {"TV & Recreation Room", "Satellite TV & Carrom Board", "#ec407a"},
            {"Indoor Gymnasium", "Cardio & Free Weight Training", "#8d6e63"},
            {"Medical First-Aid Room", "Emergency First Aid & BP Monitor", "#26c6da"},
            {"IT & Computer Lab", "20 Workstations & Gigabit LAN", "#7e57c2"},
            {"Motorcycle & Cycle Garage", "Capacity: 120 Vehicles (Covered Shed)", "#78909c"},
            {"Emergency Staircases", "2 Fire-Rated Concrete Staircases", "#ff7043"}
        };

        for (String[] s : specs) {
            VBox card = new VBox(4);
            card.setPrefWidth(280);
            card.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 8; -fx-padding: 12 16; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 8;");

            Label title = new Label(s[0]);
            title.setStyle("-fx-font-size: 11px; -fx-text-fill: #90a4ae; -fx-font-weight: bold;");

            Label val = new Label(s[1]);
            val.setStyle("-fx-font-size: 13px; -fx-text-fill: " + s[2] + "; -fx-font-weight: bold;");
            val.setWrapText(true);

            card.getChildren().addAll(title, val);
            flowBuildingSpecs.getChildren().add(card);
        }
    }

    private void renderWashroomCards() {
        flowWashrooms.getChildren().clear();
        for (int f = 1; f <= 4; f++) {
            VBox card = new VBox(8);
            card.setPrefWidth(300);
            card.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 10; -fx-padding: 16; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10;");

            Label header = new Label("🚿 FLOOR " + f + " CENTRAL WASHROOM BLOCK");
            header.setStyle("-fx-font-size: 13px; -fx-text-fill: #64b5f6; -fx-font-weight: bold;");

            GridPane g = new GridPane();
            g.setHgap(15);
            g.setVgap(6);

            g.add(createDetailLabel("Toilets & Urinals:"), 0, 0);
            g.add(createValueLabel("6 Toilets + 4 Urinals"), 1, 0);

            g.add(createDetailLabel("Showers & Taps:"), 0, 1);
            g.add(createValueLabel("4 Showers + 8 Water Taps"), 1, 1);

            g.add(createDetailLabel("Basins & Mirrors:"), 0, 2);
            g.add(createValueLabel("4 Basins + 4 Mirrors"), 1, 2);

            g.add(createDetailLabel("Exhaust & Geyser:"), 0, 3);
            g.add(createValueLabel("2 Exhaust Fans + 1 Geyser"), 1, 3);

            g.add(createDetailLabel("Sanitation Status:"), 0, 4);
            Label st = new Label("GOOD / CLEAN");
            st.setStyle("-fx-text-fill: #00e676; -fx-font-weight: bold;");
            g.add(st, 1, 4);

            card.getChildren().addAll(header, g);
            flowWashrooms.getChildren().add(card);
        }
    }

    private Label createDetailLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #90a4ae; -fx-font-size: 11px; -fx-font-weight: bold;");
        return l;
    }

    private Label createValueLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 11px;");
        return l;
    }

    private void loadRoomData() {
        try {
            Integer floor = null;
            String selFloor = cmbFloorFilter.getSelectionModel().getSelectedItem();
            if (selFloor != null && selFloor.startsWith("Floor ")) {
                floor = Integer.parseInt(selFloor.replace("Floor ", "").trim());
            }
            String search = txtSearchRoom.getText();

            List<RoomFullProfile> rooms = hallDAO.getRoomFullProfiles(selectedHallId, floor, search);
            roomList.setAll(rooms);
            tableRooms.setItems(roomList);

            roomElecList.setAll(rooms);
            tableRoomElec.setItems(roomElecList);

            roomFurnList.setAll(rooms);
            tableRoomFurn.setItems(roomFurnList);
        } catch (SQLException e) {
            System.err.println("[HallInfoController] Error loading rooms: " + e.getMessage());
        }
    }

    private void loadMaintenanceData() {
        try {
            String filter = cmbMaintenanceFilter.getSelectionModel().getSelectedItem();
            maintenanceList.setAll(hallDAO.getMaintenanceRecords(selectedHallId, filter));
            tableMaintenance.setItems(maintenanceList);
        } catch (SQLException e) {
            System.err.println("[HallInfoController] Error loading maintenance: " + e.getMessage());
        }
    }

    private void loadAssetData() {
        try {
            String filter = cmbAssetCategory.getSelectionModel().getSelectedItem();
            assetList.setAll(hallDAO.getHallAssets(selectedHallId, filter));
            tableAssets.setItems(assetList);
        } catch (SQLException e) {
            System.err.println("[HallInfoController] Error loading assets: " + e.getMessage());
        }
    }

    private void showRoomDetailModal(int roomId) {
        try {
            RoomFullProfile r = hallDAO.getRoomFullProfileById(roomId);
            if (r == null) return;

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle("Room Profile — Room " + r.getRoomNumber() + " (" + r.getHallName() + ")");

            VBox root = new VBox(16);
            root.setPadding(new Insets(24));
            root.setStyle("-fx-background-color: #0f1923;");
            root.setPrefWidth(680);

            HBox header = new HBox(15);
            header.setAlignment(Pos.CENTER_LEFT);
            Label lblTitle = new Label("🚪 ROOM " + r.getRoomNumber() + " PROFILE");
            lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
            Label lblBadge = new Label(r.getStatus());
            lblBadge.setStyle("-fx-background-color: " + (r.getAvailableSeats() > 0 ? "rgba(0,230,118,0.2)" : "rgba(239,83,80,0.2)") + "; -fx-text-fill: " + (r.getAvailableSeats() > 0 ? "#00e676" : "#ef5350") + "; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-weight: bold;");
            header.getChildren().addAll(lblTitle, lblBadge);

            TabPane tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

            Tab tabOcc = new Tab("👨‍🎓 Resident Students");
            VBox boxOcc = new VBox(10);
            boxOcc.setPadding(new Insets(12, 0, 0, 0));
            if (r.getOccupants().isEmpty()) {
                boxOcc.getChildren().add(new Label("No resident students currently assigned to this room."));
            } else {
                for (SeatInfo s : r.getOccupants()) {
                    HBox row = new HBox(12);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 6; -fx-padding: 8 12; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 6;");

                    Label seat = new Label("Seat " + s.getSeatNumber());
                    seat.setStyle("-fx-font-weight: bold; -fx-text-fill: #64b5f6; -fx-min-width: 60px;");

                    Label name = new Label(s.isOccupied() ? s.getStudentName() + " (" + s.getStudentUserId() + ")" : "AVAILABLE / VACANT");
                    name.setStyle("-fx-text-fill: " + (s.isOccupied() ? "#ffffff" : "#00e676") + "; -fx-font-weight: bold;");

                    Label dept = new Label(s.isOccupied() ? s.getStudentDept() + " (" + s.getStudentYear() + ")" : "—");
                    dept.setStyle("-fx-text-fill: #90a4ae;");

                    row.getChildren().addAll(seat, name, dept);
                    boxOcc.getChildren().add(row);
                }
            }
            tabOcc.setContent(boxOcc);

            Tab tabElec = new Tab("⚡ Electrical Breakdown");
            GridPane gElec = new GridPane();
            gElec.setHgap(20); gElec.setVgap(10);
            gElec.setPadding(new Insets(14));
            gElec.add(createDetailLabel("Air Conditioner (AC):"), 0, 0); gElec.add(createValueLabel(r.getAcCount() + " Unit(s)"), 1, 0);
            gElec.add(createDetailLabel("Ceiling Fans:"), 0, 1); gElec.add(createValueLabel(r.getCeilingFanCount() + " Fans"), 1, 1);
            gElec.add(createDetailLabel("LED Lights:"), 0, 2); gElec.add(createValueLabel(r.getLedLightCount() + " Fixtures"), 1, 2);
            gElec.add(createDetailLabel("Tube Lights:"), 0, 3); gElec.add(createValueLabel(r.getTubeLightCount() + " Fixtures"), 1, 3);
            gElec.add(createDetailLabel("Power Sockets:"), 0, 4); gElec.add(createValueLabel(r.getSocketCount() + " Sockets"), 1, 4);
            gElec.add(createDetailLabel("Switches:"), 0, 5); gElec.add(createValueLabel(r.getSwitchCount() + " Switches"), 1, 5);
            tabElec.setContent(gElec);

            Tab tabFurn = new Tab("🪑 Furniture Inventory");
            GridPane gFurn = new GridPane();
            gFurn.setHgap(20); gFurn.setVgap(10);
            gFurn.setPadding(new Insets(14));
            gFurn.add(createDetailLabel("Beds / Cots:"), 0, 0); gFurn.add(createValueLabel(r.getBedCount() + " Wooden Beds"), 1, 0);
            gFurn.add(createDetailLabel("Study Tables:"), 0, 1); gFurn.add(createValueLabel(r.getTableCount() + " Individual Desks"), 1, 1);
            gFurn.add(createDetailLabel("Study Chairs:"), 0, 2); gFurn.add(createValueLabel(r.getChairCount() + " Chairs"), 1, 2);
            gFurn.add(createDetailLabel("Wardrobes:"), 0, 3); gFurn.add(createValueLabel(r.getWardrobeCount() + " Wardrobes"), 1, 3);
            gFurn.add(createDetailLabel("Bookshelves:"), 0, 4); gFurn.add(createValueLabel(r.getBookshelfCount() + " Wall Shelves"), 1, 4);
            gFurn.add(createDetailLabel("Overall Condition:"), 0, 5); gFurn.add(createValueLabel(r.getGoodConditionCount() + " Good, " + r.getDamagedCount() + " Damaged"), 1, 5);
            tabFurn.setContent(gFurn);

            tabPane.getTabs().addAll(tabOcc, tabElec, tabFurn);

            HBox actions = new HBox(12);
            actions.setAlignment(Pos.CENTER_RIGHT);
            Button btnClose = new Button("Close");
            btnClose.getStyleClass().add("btn-action-secondary");
            btnClose.setOnAction(e -> modal.close());

            if (isProvost) {
                Button btnEdit = new Button("✏️ Edit Room Equipment");
                btnEdit.getStyleClass().add("btn-action-primary");
                btnEdit.setOnAction(e -> {
                    modal.close();
                    showEditRoomEquipmentDialog(r);
                });
                actions.getChildren().add(btnEdit);
            }

            actions.getChildren().add(btnClose);

            root.getChildren().addAll(header, tabPane, actions);
            modal.setScene(new javafx.scene.Scene(root));
            modal.showAndWait();
        } catch (SQLException e) {
            System.err.println("[HallInfoController] Error displaying room modal: " + e.getMessage());
        }
    }

    private void showEditRoomEquipmentDialog(RoomFullProfile r) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Equipment & Furniture — Room " + r.getRoomNumber());
        dialog.setHeaderText("Update electrical and furniture counts for Room " + r.getRoomNumber());

        DialogPane dp = dialog.getDialogPane();
        dp.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dp.setStyle("-fx-background-color: #0f1923;");

        GridPane g = new GridPane();
        g.setHgap(15); g.setVgap(10);
        g.setPadding(new Insets(20));

        TextField txtAc = new TextField(String.valueOf(r.getAcCount()));
        TextField txtFans = new TextField(String.valueOf(r.getCeilingFanCount()));
        TextField txtLed = new TextField(String.valueOf(r.getLedLightCount()));
        TextField txtTube = new TextField(String.valueOf(r.getTubeLightCount()));
        TextField txtSockets = new TextField(String.valueOf(r.getSocketCount()));

        TextField txtBeds = new TextField(String.valueOf(r.getBedCount()));
        TextField txtTables = new TextField(String.valueOf(r.getTableCount()));
        TextField txtChairs = new TextField(String.valueOf(r.getChairCount()));
        TextField txtWardrobes = new TextField(String.valueOf(r.getWardrobeCount()));
        TextField txtBookshelves = new TextField(String.valueOf(r.getBookshelfCount()));

        g.add(new Label("AC Units:"), 0, 0); g.add(txtAc, 1, 0);
        g.add(new Label("Ceiling Fans:"), 0, 1); g.add(txtFans, 1, 1);
        g.add(new Label("LED Lights:"), 0, 2); g.add(txtLed, 1, 2);
        g.add(new Label("Tube Lights:"), 0, 3); g.add(txtTube, 1, 3);
        g.add(new Label("Power Sockets:"), 0, 4); g.add(txtSockets, 1, 4);

        g.add(new Label("Beds:"), 2, 0); g.add(txtBeds, 3, 0);
        g.add(new Label("Tables:"), 2, 1); g.add(txtTables, 3, 1);
        g.add(new Label("Chairs:"), 2, 2); g.add(txtChairs, 3, 2);
        g.add(new Label("Wardrobes:"), 2, 3); g.add(txtWardrobes, 3, 3);
        g.add(new Label("Bookshelves:"), 2, 4); g.add(txtBookshelves, 3, 4);

        dp.setContent(g);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    int ac = Integer.parseInt(txtAc.getText().trim());
                    int fans = Integer.parseInt(txtFans.getText().trim());
                    int led = Integer.parseInt(txtLed.getText().trim());
                    int tube = Integer.parseInt(txtTube.getText().trim());
                    int sockets = Integer.parseInt(txtSockets.getText().trim());

                    int beds = Integer.parseInt(txtBeds.getText().trim());
                    int tables = Integer.parseInt(txtTables.getText().trim());
                    int chairs = Integer.parseInt(txtChairs.getText().trim());
                    int wardrobes = Integer.parseInt(txtWardrobes.getText().trim());
                    int bookshelves = Integer.parseInt(txtBookshelves.getText().trim());

                    hallDAO.updateRoomElectrical(r.getRoomId(), ac, fans, led, tube, sockets);
                    hallDAO.updateRoomFurniture(r.getRoomId(), beds, tables, chairs, wardrobes, bookshelves);
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            }
            return false;
        });

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            loadAllData();
            showFeedback("Room " + r.getRoomNumber() + " equipment & furniture updated successfully!");
        }
    }

    private void showUpdateMaintenanceDialog(MaintenanceRecord rec) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Update Maintenance Ticket — " + rec.getRecordCode());
        dialog.setHeaderText("Update status and actual repair cost for " + rec.getRecordCode());

        DialogPane dp = dialog.getDialogPane();
        dp.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dp.setStyle("-fx-background-color: #0f1923;");

        GridPane g = new GridPane();
        g.setHgap(15); g.setVgap(10);
        g.setPadding(new Insets(20));

        ComboBox<String> cmbStatus = new ComboBox<>(FXCollections.observableArrayList("PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED"));
        cmbStatus.getSelectionModel().select(rec.getStatus());

        TextField txtCost = new TextField(String.valueOf(rec.getActualCost()));
        TextField txtRemarks = new TextField(rec.getRemarks() != null ? rec.getRemarks() : "");

        g.add(new Label("Status:"), 0, 0); g.add(cmbStatus, 1, 0);
        g.add(new Label("Actual Cost (Tk):"), 0, 1); g.add(txtCost, 1, 1);
        g.add(new Label("Remarks:"), 0, 2); g.add(txtRemarks, 1, 2);

        dp.setContent(g);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    String st = cmbStatus.getSelectionModel().getSelectedItem();
                    double cost = Double.parseDouble(txtCost.getText().trim());
                    String rem = txtRemarks.getText().trim();
                    return hallDAO.updateMaintenanceStatus(rec.getId(), st, cost, rem);
                } catch (Exception ex) {
                    return false;
                }
            }
            return false;
        });

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            loadAllData();
            showFeedback("Maintenance ticket " + rec.getRecordCode() + " updated!");
        }
    }

    @FXML
    private void onNewMaintenanceClicked() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Report New Maintenance Work Order");
        dialog.setHeaderText("Submit a maintenance request for " + lblHallNameHeader.getText());

        DialogPane dp = dialog.getDialogPane();
        dp.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dp.setStyle("-fx-background-color: #0f1923;");

        GridPane g = new GridPane();
        g.setHgap(15); g.setVgap(10);
        g.setPadding(new Insets(20));

        TextField txtLoc = new TextField("Room 201");
        ComboBox<String> cmbCat = new ComboBox<>(FXCollections.observableArrayList("Electrical", "Plumbing", "Carpentry", "AC Servicing", "Civil / Masonry", "Cleaning"));
        cmbCat.getSelectionModel().selectFirst();
        TextArea txtDesc = new TextArea();
        txtDesc.setPrefRowCount(3);
        ComboBox<String> cmbPri = new ComboBox<>(FXCollections.observableArrayList("LOW", "MEDIUM", "HIGH", "EMERGENCY"));
        cmbPri.getSelectionModel().select("MEDIUM");

        g.add(new Label("Location / Room:"), 0, 0); g.add(txtLoc, 1, 0);
        g.add(new Label("Category:"), 0, 1); g.add(cmbCat, 1, 1);
        g.add(new Label("Problem Description:"), 0, 2); g.add(txtDesc, 1, 2);
        g.add(new Label("Priority Level:"), 0, 3); g.add(cmbPri, 1, 3);

        dp.setContent(g);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    String desc = txtDesc.getText().trim();
                    if (desc.isEmpty()) return false;

                    MaintenanceRecord r = new MaintenanceRecord();
                    r.setHallId(selectedHallId);
                    r.setLocation(txtLoc.getText().trim());
                    r.setEquipmentCategory(cmbCat.getSelectionModel().getSelectedItem());
                    r.setProblemDescription(desc);
                    r.setReportedBy(currentUser != null ? currentUser.getUserId() : "Student");
                    r.setPriority(cmbPri.getSelectionModel().getSelectedItem());
                    r.setStatus("PENDING");
                    r.setReportDate(LocalDate.now());
                    r.setAssignedPerson("Maintenance Cell");
                    return hallDAO.createMaintenanceRecord(r);
                } catch (Exception ex) {
                    return false;
                }
            }
            return false;
        });

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            loadAllData();
            showFeedback("Maintenance request submitted successfully!");
        }
    }

    @FXML
    private void onAddAssetClicked() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Add New Fixed Asset");
        dialog.setHeaderText("Register a new asset item for " + lblHallNameHeader.getText());

        DialogPane dp = dialog.getDialogPane();
        dp.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dp.setStyle("-fx-background-color: #0f1923;");

        GridPane g = new GridPane();
        g.setHgap(15); g.setVgap(10);
        g.setPadding(new Insets(20));

        TextField txtName = new TextField();
        ComboBox<String> cmbCat = new ComboBox<>(FXCollections.observableArrayList("ELECTRICAL", "FURNITURE", "IT_EQUIPMENT", "SPORTS", "SAFETY", "CLEANING", "OTHER"));
        cmbCat.getSelectionModel().selectFirst();
        TextField txtQty = new TextField("1");
        TextField txtLoc = new TextField("General Storage");
        TextField txtPrice = new TextField("5000");

        g.add(new Label("Asset Name:"), 0, 0); g.add(txtName, 1, 0);
        g.add(new Label("Category:"), 0, 1); g.add(cmbCat, 1, 1);
        g.add(new Label("Quantity:"), 0, 2); g.add(txtQty, 1, 2);
        g.add(new Label("Location:"), 0, 3); g.add(txtLoc, 1, 3);
        g.add(new Label("Purchase Value (Tk):"), 0, 4); g.add(txtPrice, 1, 4);

        dp.setContent(g);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    String name = txtName.getText().trim();
                    if (name.isEmpty()) return false;

                    AssetItem a = new AssetItem();
                    a.setHallId(selectedHallId);
                    a.setAssetCode("AST-" + System.currentTimeMillis() % 100000);
                    a.setAssetName(name);
                    a.setCategory(cmbCat.getSelectionModel().getSelectedItem());
                    a.setQuantity(Integer.parseInt(txtQty.getText().trim()));
                    a.setLocation(txtLoc.getText().trim());
                    a.setPurchasePrice(Double.parseDouble(txtPrice.getText().trim()));
                    a.setPurchaseDate(LocalDate.now());
                    a.setConditionStatus("GOOD");
                    a.setWarrantyInfo("Standard Warranty");
                    a.setAssignedPerson("Estate Officer");
                    return hallDAO.createAssetItem(a);
                } catch (Exception ex) {
                    return false;
                }
            }
            return false;
        });

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            loadAllData();
            showFeedback("Asset added to inventory!");
        }
    }

    @FXML
    private void onHallSelected() {
        int idx = cmbSelectHall.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            selectedHallId = idx + 1;
            loadAllData();
        }
    }

    @FXML
    private void onFloorFilterChanged() {
        loadRoomData();
    }

    @FXML
    private void onRoomSearchChanged() {
        loadRoomData();
    }

    @FXML
    private void onMaintenanceFilterChanged() {
        loadMaintenanceData();
    }

    @FXML
    private void onAssetCategoryChanged() {
        loadAssetData();
    }

    @FXML
    private void onAssetSearchChanged() {
        loadAssetData();
    }

    @FXML
    private void onRefreshClicked() {
        loadAllData();
        showFeedback("Hall infrastructure data refreshed.");
    }

    private void showFeedback(String msg) {
        lblFeedback.setText(msg);
        lblFeedback.setVisible(true);
        lblFeedback.setManaged(true);
    }

    private Stage getStage() {
        if (sidebar != null && sidebar.getScene() != null && sidebar.getScene().getWindow() instanceof Stage) {
            return (Stage) sidebar.getScene().getWindow();
        }
        return null;
    }

    @FXML
    private void onDashboardClicked() {
        try {
            Stage stage = getStage();
            if (stage != null) {
                if (isProvost) {
                    SceneManager.switchTo(stage, SceneManager.PROVOST_DASHBOARD_FXML, "Provost Dashboard");
                } else {
                    SceneManager.switchTo(stage, SceneManager.STUDENT_DASHBOARD_FXML, "Student Dashboard");
                }
            }
        } catch (IOException e) {
            System.err.println("[HallInfoController] Nav error: " + e.getMessage());
        }
    }

    @FXML
    private void onMyProfileClicked() {
        try {
            Stage stage = getStage();
            if (stage != null) {
                if (isProvost) {
                    SceneManager.switchTo(stage, SceneManager.PROVOST_PROFILE_FXML, "Provost Profile");
                } else {
                    SceneManager.switchTo(stage, SceneManager.STUDENT_PROFILE_FXML, "My Profile");
                }
            }
        } catch (IOException e) {
            System.err.println("[HallInfoController] Nav error: " + e.getMessage());
        }
    }

    @FXML
    private void onBillingOrRequestsClicked() {
        try {
            Stage stage = getStage();
            if (stage != null) {
                if (isProvost) {
                    SceneManager.switchTo(stage, SceneManager.PROVOST_PAYMENT_REQUESTS_FXML, "Payment Requests");
                } else {
                    SceneManager.switchTo(stage, SceneManager.STUDENT_BILLING_FXML, "Bill & Due");
                }
            }
        } catch (IOException e) {
            System.err.println("[HallInfoController] Nav error: " + e.getMessage());
        }
    }

    @FXML
    private void onNoticeBoardClicked() {
        try {
            Stage stage = getStage();
            if (stage != null) {
                SceneManager.switchTo(stage, SceneManager.NOTICE_BOARD_FXML, "Notice Board");
            }
        } catch (IOException e) {
            System.err.println("[HallInfoController] Nav error: " + e.getMessage());
        }
    }

    @FXML
    private void onHallChangeClicked() {
        try {
            Stage stage = getStage();
            if (stage != null) {
                SceneManager.switchTo(stage, SceneManager.STUDENT_HALL_CHANGE_FXML, "Apply for New Hall");
            }
        } catch (IOException e) {
            System.err.println("[HallInfoController] Nav error: " + e.getMessage());
        }
    }

    @FXML
    private void onRoomChangeClicked() {
        try {
            Stage stage = getStage();
            if (stage != null) {
                SceneManager.switchTo(stage, SceneManager.STUDENT_ROOM_CHANGE_FXML, "Room/Seat Change");
            }
        } catch (IOException e) {
            System.err.println("[HallInfoController] Nav error: " + e.getMessage());
        }
    }

    @FXML
    private void onComplaintsClicked() {
        try {
            Stage stage = getStage();
            if (stage != null) {
                if (isProvost) {
                    SceneManager.switchTo(stage, SceneManager.PROVOST_COMPLAINTS_FXML, "Complaints");
                } else {
                    SceneManager.switchTo(stage, SceneManager.STUDENT_COMPLAINTS_FXML, "Complaints");
                }
            }
        } catch (IOException e) {
            System.err.println("[HallInfoController] Nav error: " + e.getMessage());
        }
    }

    @FXML
    private void onHallLeaveClicked() {
        try {
            Stage stage = getStage();
            if (stage != null) {
                SceneManager.switchTo(stage, SceneManager.STUDENT_HALL_LEAVE_FXML, "Hall Leave");
            }
        } catch (IOException e) {
            System.err.println("[HallInfoController] Nav error: " + e.getMessage());
        }
    }

    @FXML
    private void onHallChangeRequestsClicked() {
        try {
            Stage stage = getStage();
            if (stage != null) {
                SceneManager.switchTo(stage, SceneManager.PROVOST_HALL_CHANGE_REQUESTS_FXML, "Hall Change Requests");
            }
        } catch (IOException e) {
            System.err.println("[HallInfoController] Nav error: " + e.getMessage());
        }
    }

    @FXML
    private void onRoomChangeRequestsClicked() {
        try {
            Stage stage = getStage();
            if (stage != null) {
                SceneManager.switchTo(stage, SceneManager.PROVOST_ROOM_CHANGE_REQUESTS_FXML, "Room/Seat Change Requests");
            }
        } catch (IOException e) {
            System.err.println("[HallInfoController] Nav error: " + e.getMessage());
        }
    }

    @FXML
    private void onHallLeaveRequestsClicked() {
        try {
            Stage stage = getStage();
            if (stage != null) {
                SceneManager.switchTo(stage, SceneManager.PROVOST_HALL_LEAVE_REQUESTS_FXML, "Hall Leave Requests");
            }
        } catch (IOException e) {
            System.err.println("[HallInfoController] Nav error: " + e.getMessage());
        }
    }

    private void selectTab(int index) {
        Button[] tabButtons = {
            tabBtnOverview, tabBtnBuilding, tabBtnFloors, tabBtnRooms, tabBtnElectrical,
            tabBtnFurniture, tabBtnAc, tabBtnLighting, tabBtnWashrooms, tabBtnWater,
            tabBtnDining, tabBtnNetwork, tabBtnCctv, tabBtnFire, tabBtnMaintenance,
            tabBtnCleaning, tabBtnContacts, tabBtnUtilities, tabBtnAssets
        };
        VBox[] panes = {
            paneOverview, paneBuilding, paneFloors, paneRooms, paneElectrical,
            paneFurniture, paneAc, paneLighting, paneWashrooms, paneWater,
            paneDining, paneNetwork, paneCctv, paneFire, paneMaintenance,
            paneCleaning, paneContacts, paneUtilities, paneAssets
        };

        for (int i = 0; i < tabButtons.length; i++) {
            boolean active = (i == index);
            if (tabButtons[i] != null) {
                tabButtons[i].getStyleClass().removeAll("tab-pill", "tab-pill-active");
                tabButtons[i].getStyleClass().add(active ? "tab-pill-active" : "tab-pill");
            }
            if (panes[i] != null) {
                panes[i].setVisible(active);
                panes[i].setManaged(active);
            }
        }

        if (scrollTabBar != null && index >= 0 && index < tabButtons.length) {
            double hval = (double) index / (tabButtons.length - 1);
            scrollTabBar.setHvalue(hval);
        }
    }

    @FXML
    private void onTabScrollLeft() {
        if (scrollTabBar != null) {
            scrollTabBar.setHvalue(Math.max(0.0, scrollTabBar.getHvalue() - 0.20));
        }
    }

    @FXML
    private void onTabScrollRight() {
        if (scrollTabBar != null) {
            scrollTabBar.setHvalue(Math.min(1.0, scrollTabBar.getHvalue() + 0.20));
        }
    }

    @FXML private void onTabOverviewClicked()    { selectTab(0); }
    @FXML private void onTabBuildingClicked()    { selectTab(1); }
    @FXML private void onTabFloorsClicked()      { selectTab(2); }
    @FXML private void onTabRoomsClicked()       { selectTab(3); }
    @FXML private void onTabElectricalClicked()  { selectTab(4); }
    @FXML private void onTabFurnitureClicked()   { selectTab(5); }
    @FXML private void onTabAcClicked()          { selectTab(6); }
    @FXML private void onTabLightingClicked()    { selectTab(7); }
    @FXML private void onTabWashroomsClicked()   { selectTab(8); }
    @FXML private void onTabWaterClicked()       { selectTab(9); }
    @FXML private void onTabDiningClicked()      { selectTab(10); }
    @FXML private void onTabNetworkClicked()     { selectTab(11); }
    @FXML private void onTabCctvClicked()        { selectTab(12); }
    @FXML private void onTabFireClicked()        { selectTab(13); }
    @FXML private void onTabMaintenanceClicked() { selectTab(14); }
    @FXML private void onTabCleaningClicked()    { selectTab(15); }
    @FXML private void onTabContactsClicked()    { selectTab(16); }
    @FXML private void onTabUtilitiesClicked()   { selectTab(17); }
    @FXML private void onTabAssetsClicked()      { selectTab(18); }

    @FXML
    private void onBackClicked() {
        try {
            Stage stage = getStage();
            if (stage != null) {
                SceneManager.goBack(stage);
            }
        } catch (Exception e) {
            System.err.println("[HallInfoController] Back navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void onLogoutClicked() {
        SessionManager.getInstance().logout();
        try {
            Stage stage = getStage();
            if (stage != null) {
                SceneManager.switchTo(stage, SceneManager.LOGIN_FXML, "Login");
            }
        } catch (IOException e) {
            System.err.println("[HallInfoController] Logout error: " + e.getMessage());
        }
    }
}

