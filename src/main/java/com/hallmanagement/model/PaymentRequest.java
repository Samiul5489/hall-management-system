package com.hallmanagement.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentRequest {
    private int id;
    private String requestCode;
    private String studentId;
    private String studentName;
    private String studentRoll;
    private String studentDept;
    private int hallId;
    private String hallName;
    private int roomNumber;
    private int seatNumber;
    private String provostId;
    private String provostName;
    private int billId;
    private String billingPeriod;
    private double totalBill;
    private double previouslyPaid;
    private double currentDue;
    private double amount;
    private LocalDateTime requestDate;
    private String status;
    private String rejectionReason;

    public PaymentRequest() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentRoll() {
        return studentRoll;
    }

    public void setStudentRoll(String studentRoll) {
        this.studentRoll = studentRoll;
    }

    public String getStudentDept() {
        return studentDept;
    }

    public void setStudentDept(String studentDept) {
        this.studentDept = studentDept;
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

    public String getProvostId() {
        return provostId;
    }

    public void setProvostId(String provostId) {
        this.provostId = provostId;
    }

    public String getProvostName() {
        return provostName;
    }

    public void setProvostName(String provostName) {
        this.provostName = provostName;
    }

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public String getBillingPeriod() {
        return billingPeriod;
    }

    public void setBillingPeriod(String billingPeriod) {
        this.billingPeriod = billingPeriod;
    }

    public double getTotalBill() {
        return totalBill;
    }

    public void setTotalBill(double totalBill) {
        this.totalBill = totalBill;
    }

    public double getPreviouslyPaid() {
        return previouslyPaid;
    }

    public void setPreviouslyPaid(double previouslyPaid) {
        this.previouslyPaid = previouslyPaid;
    }

    public double getCurrentDue() {
        return currentDue;
    }

    public void setCurrentDue(double currentDue) {
        this.currentDue = currentDue;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getFormattedAmount() {
        return String.format("৳%.2f", amount);
    }

    public String getFormattedTotalBill() {
        return String.format("৳%.2f", totalBill);
    }

    public String getFormattedPrevPaid() {
        return String.format("৳%.2f", previouslyPaid);
    }

    public String getFormattedCurrentDue() {
        return String.format("৳%.2f", currentDue);
    }

    public String getFormattedDate() {
        if (requestDate == null) return "N/A";
        return requestDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    public String getResidenceDisplay() {
        return (hallName != null ? hallName : "Hall") + " | Room " + roomNumber + " | Seat " + String.format("%02d", seatNumber);
    }
}
