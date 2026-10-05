package com.hallmanagement.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EquipmentItem {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private int id;
    private int hallId;
    private String itemCode;
    private String category;
    private String equipmentType;
    private String locationType;
    private int roomNumber;
    private int floorNumber;
    private String specificLocation;
    private String brandModel;
    private String capacityRating;
    private LocalDate installDate;
    private String conditionStatus;
    private LocalDate lastServiceDate;
    private LocalDate nextServiceDate;
    private String remarks;

    public EquipmentItem() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHallId() { return hallId; }
    public void setHallId(int hallId) { this.hallId = hallId; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getEquipmentType() { return equipmentType; }
    public void setEquipmentType(String equipmentType) { this.equipmentType = equipmentType; }

    public String getLocationType() { return locationType; }
    public void setLocationType(String locationType) { this.locationType = locationType; }

    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    public String getSpecificLocation() { return specificLocation; }
    public void setSpecificLocation(String specificLocation) { this.specificLocation = specificLocation; }

    public String getLocationDisplay() {
        if (roomNumber > 0) {
            return "Room " + roomNumber + (floorNumber > 0 ? " (Floor " + floorNumber + ")" : "");
        }
        if (specificLocation != null && !specificLocation.isEmpty()) {
            return specificLocation + (floorNumber > 0 ? " (Floor " + floorNumber + ")" : "");
        }
        if (floorNumber > 0) {
            return "Floor " + floorNumber;
        }
        return "General Hall Area";
    }

    public String getBrandModel() { return brandModel; }
    public void setBrandModel(String brandModel) { this.brandModel = brandModel; }

    public String getCapacityRating() { return capacityRating; }
    public void setCapacityRating(String capacityRating) { this.capacityRating = capacityRating; }

    public LocalDate getInstallDate() { return installDate; }
    public void setInstallDate(LocalDate installDate) { this.installDate = installDate; }

    public String getInstallDateDisplay() {
        return installDate != null ? installDate.format(DATE_FMT) : "—";
    }

    public String getConditionStatus() { return conditionStatus != null ? conditionStatus : "WORKING"; }
    public void setConditionStatus(String conditionStatus) { this.conditionStatus = conditionStatus; }

    public LocalDate getLastServiceDate() { return lastServiceDate; }
    public void setLastServiceDate(LocalDate lastServiceDate) { this.lastServiceDate = lastServiceDate; }

    public String getLastServiceDateDisplay() {
        return lastServiceDate != null ? lastServiceDate.format(DATE_FMT) : "—";
    }

    public LocalDate getNextServiceDate() { return nextServiceDate; }
    public void setNextServiceDate(LocalDate nextServiceDate) { this.nextServiceDate = nextServiceDate; }

    public String getNextServiceDateDisplay() {
        return nextServiceDate != null ? nextServiceDate.format(DATE_FMT) : "—";
    }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
