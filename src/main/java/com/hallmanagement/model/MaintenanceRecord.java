package com.hallmanagement.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MaintenanceRecord {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private int id;
    private int hallId;
    private String recordCode;
    private String location;
    private int roomNumber;
    private String equipmentCategory;
    private String problemDescription;
    private String reportedBy;
    private LocalDate reportDate;
    private String assignedPerson;
    private String priority;
    private String status;
    private double estimatedCost;
    private double actualCost;
    private LocalDate completionDate;
    private String remarks;

    public MaintenanceRecord() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHallId() { return hallId; }
    public void setHallId(int hallId) { this.hallId = hallId; }

    public String getRecordCode() { return recordCode; }
    public void setRecordCode(String recordCode) { this.recordCode = recordCode; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public String getLocationDisplay() {
        if (roomNumber > 0) return "Room " + roomNumber;
        return location != null && !location.isEmpty() ? location : "General Facility";
    }

    public String getEquipmentCategory() { return equipmentCategory; }
    public void setEquipmentCategory(String equipmentCategory) { this.equipmentCategory = equipmentCategory; }

    public String getProblemDescription() { return problemDescription; }
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }

    public String getReportDateDisplay() {
        return reportDate != null ? reportDate.format(DATE_FMT) : "—";
    }

    public String getAssignedPerson() { return assignedPerson; }
    public void setAssignedPerson(String assignedPerson) { this.assignedPerson = assignedPerson; }

    public String getPriority() { return priority != null ? priority : "MEDIUM"; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status != null ? status : "PENDING"; }
    public void setStatus(String status) { this.status = status; }

    public double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(double estimatedCost) { this.estimatedCost = estimatedCost; }

    public double getActualCost() { return actualCost; }
    public void setActualCost(double actualCost) { this.actualCost = actualCost; }

    public LocalDate getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }

    public String getCompletionDateDisplay() {
        return completionDate != null ? completionDate.format(DATE_FMT) : "—";
    }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
