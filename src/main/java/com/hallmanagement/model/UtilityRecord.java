package com.hallmanagement.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UtilityRecord {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private int id;
    private int hallId;
    private String billingMonth;
    private double electricityBill;
    private double waterBill;
    private double internetBill;
    private double gasBill;
    private double maintenanceCost;
    private double cleaningCost;
    private double totalExpense;
    private String paymentStatus;
    private LocalDate paymentDate;
    private String remarks;

    public UtilityRecord() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHallId() { return hallId; }
    public void setHallId(int hallId) { this.hallId = hallId; }

    public String getBillingMonth() { return billingMonth; }
    public void setBillingMonth(String billingMonth) { this.billingMonth = billingMonth; }

    public double getElectricityBill() { return electricityBill; }
    public void setElectricityBill(double electricityBill) { this.electricityBill = electricityBill; }

    public double getWaterBill() { return waterBill; }
    public void setWaterBill(double waterBill) { this.waterBill = waterBill; }

    public double getInternetBill() { return internetBill; }
    public void setInternetBill(double internetBill) { this.internetBill = internetBill; }

    public double getGasBill() { return gasBill; }
    public void setGasBill(double gasBill) { this.gasBill = gasBill; }

    public double getMaintenanceCost() { return maintenanceCost; }
    public void setMaintenanceCost(double maintenanceCost) { this.maintenanceCost = maintenanceCost; }

    public double getCleaningCost() { return cleaningCost; }
    public void setCleaningCost(double cleaningCost) { this.cleaningCost = cleaningCost; }

    public double getTotalExpense() { return totalExpense; }
    public void setTotalExpense(double totalExpense) { this.totalExpense = totalExpense; }

    public String getPaymentStatus() { return paymentStatus != null ? paymentStatus : "PAID"; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getPaymentDateDisplay() {
        return paymentDate != null ? paymentDate.format(DATE_FMT) : "—";
    }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
