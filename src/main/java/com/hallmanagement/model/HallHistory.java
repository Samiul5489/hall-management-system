package com.hallmanagement.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HallHistory {
    private String hallName;
    private int roomNumber;
    private int seatNumber;
    private LocalDate startDate;
    private LocalDate endDate;

    public HallHistory() {}

    public HallHistory(String hallName, int roomNumber, int seatNumber, LocalDate startDate, LocalDate endDate) {
        this.hallName = hallName;
        this.roomNumber = roomNumber;
        this.seatNumber = seatNumber;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStartDateDisplay() {
        return startDate != null ? startDate.toString() : "";
    }

    public String getEndDateDisplay() {
        return endDate != null ? endDate.toString() : "Current";
    }
}
