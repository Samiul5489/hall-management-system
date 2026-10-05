package com.hallmanagement.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AssetItem {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private int id;
    private int hallId;
    private String assetCode;
    private String assetName;
    private String category;
    private int quantity;
    private String location;
    private int roomNumber;
    private int floorNumber;
    private LocalDate purchaseDate;
    private double purchasePrice;
    private String conditionStatus;
    private String warrantyInfo;
    private String assignedPerson;
    private String remarks;

    public AssetItem() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHallId() { return hallId; }
    public void setHallId(int hallId) { this.hallId = hallId; }

    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    public String getLocationDisplay() {
        if (roomNumber > 0) return "Room " + roomNumber;
        if (location != null && !location.isEmpty()) return location;
        if (floorNumber > 0) return "Floor " + floorNumber;
        return "General Hall Storage";
    }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getPurchaseDateDisplay() {
        return purchaseDate != null ? purchaseDate.format(DATE_FMT) : "—";
    }

    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    public String getConditionStatus() { return conditionStatus != null ? conditionStatus : "GOOD"; }
    public void setConditionStatus(String conditionStatus) { this.conditionStatus = conditionStatus; }

    public String getWarrantyInfo() { return warrantyInfo; }
    public void setWarrantyInfo(String warrantyInfo) { this.warrantyInfo = warrantyInfo; }

    public String getAssignedPerson() { return assignedPerson; }
    public void setAssignedPerson(String assignedPerson) { this.assignedPerson = assignedPerson; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
