package com.hallmanagement.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Bill {
    private int id;
    private String studentId;
    private int hallId;
    private String hallName;
    private int roomNumber;
    private int seatNumber;
    private String billingPeriod;
    private double hallCharge;
    private double electricityCharge;
    private double waterCharge;
    private double maintenanceCharge;
    private double otherCharge;
    private double totalAmount;
    private double paidAmount;
    private double dueAmount;
    private String status;
    private LocalDateTime createdAt;

    public Bill() {}

    public Bill(int id, String studentId, int hallId, String hallName, int roomNumber, int seatNumber,
                String billingPeriod, double hallCharge, double electricityCharge, double waterCharge,
                double maintenanceCharge, double otherCharge, double totalAmount, double paidAmount,
                double dueAmount, String status, LocalDateTime createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.hallId = hallId;
        this.hallName = hallName;
        this.roomNumber = roomNumber;
        this.seatNumber = seatNumber;
        this.billingPeriod = billingPeriod;
        this.hallCharge = hallCharge;
        this.electricityCharge = electricityCharge;
        this.waterCharge = waterCharge;
        this.maintenanceCharge = maintenanceCharge;
        this.otherCharge = otherCharge;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.dueAmount = dueAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public int getHallId() {
        return hallId;
    }

    public void setHallId(int hallId) {
        this.hallId = hallId;
    }

    public String getHallName() {
        return hallName;
    }

    public void setHallName(String hallName) {
        this.hallName = hallName;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getBillingPeriod() {
        return billingPeriod;
    }

    public void setBillingPeriod(String billingPeriod) {
        this.billingPeriod = billingPeriod;
    }

    public double getHallCharge() {
        return hallCharge;
    }

    public void setHallCharge(double hallCharge) {
        this.hallCharge = hallCharge;
    }

    public double getElectricityCharge() {
        return electricityCharge;
    }

    public void setElectricityCharge(double electricityCharge) {
        this.electricityCharge = electricityCharge;
    }

    public double getWaterCharge() {
        return waterCharge;
    }

    public void setWaterCharge(double waterCharge) {
        this.waterCharge = waterCharge;
    }

    public double getMaintenanceCharge() {
        return maintenanceCharge;
    }

    public void setMaintenanceCharge(double maintenanceCharge) {
        this.maintenanceCharge = maintenanceCharge;
    }

    public double getOtherCharge() {
        return otherCharge;
    }

    public void setOtherCharge(double otherCharge) {
        this.otherCharge = otherCharge;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public double getDueAmount() {
        return dueAmount;
    }

    public void setDueAmount(double dueAmount) {
        this.dueAmount = dueAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFormattedTotal() {
        return String.format("৳%.2f", totalAmount);
    }

    public String getFormattedPaid() {
        return String.format("৳%.2f", paidAmount);
    }

    public String getFormattedDue() {
        return String.format("৳%.2f", dueAmount);
    }

    public String getFormattedDate() {
        if (createdAt == null) return "N/A";
        return createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }
}
