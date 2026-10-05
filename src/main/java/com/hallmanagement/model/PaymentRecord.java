package com.hallmanagement.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentRecord {
    private int id;
    private String paymentCode;
    private int paymentRequestId;
    private String studentId;
    private String studentName;
    private String studentRoll;
    private String provostId;
    private String provostName;
    private int billId;
    private String billingPeriod;
    private String hallName;
    private int roomNumber;
    private int seatNumber;
    private double amount;
    private LocalDateTime paymentDate;
    private String status;

    public PaymentRecord() {}

    public PaymentRecord(int id, String paymentCode, int paymentRequestId, String studentId,
                         String studentName, String studentRoll, String provostId, String provostName,
                         int billId, String billingPeriod, String hallName, int roomNumber,
                         int seatNumber, double amount, LocalDateTime paymentDate, String status) {
        this.id = id;
        this.paymentCode = paymentCode;
        this.paymentRequestId = paymentRequestId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentRoll = studentRoll;
        this.provostId = provostId;
        this.provostName = provostName;
        this.billId = billId;
        this.billingPeriod = billingPeriod;
        this.hallName = hallName;
        this.roomNumber = roomNumber;
        this.seatNumber = seatNumber;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPaymentCode() {
        return paymentCode;
    }

    public void setPaymentCode(String paymentCode) {
        this.paymentCode = paymentCode;
    }

    public int getPaymentRequestId() {
        return paymentRequestId;
    }

    public void setPaymentRequestId(int paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFormattedAmount() {
        return String.format("৳%.2f", amount);
    }

    public String getFormattedDate() {
        if (paymentDate == null) return "N/A";
        return paymentDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }

    public String getResidenceDisplay() {
        return (hallName != null ? hallName : "Hall") + " | Rm " + roomNumber + " | St " + String.format("%02d", seatNumber);
    }
}
