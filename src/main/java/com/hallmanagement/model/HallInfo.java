package com.hallmanagement.model;

public class HallInfo {
    private int id;
    private String hallName;
    private String hallType;
    private int totalRooms;
    private int totalSeats;
    private int occupiedSeats;
    private int availableSeats;
    private String provostUserId;
    private String provostName;
    private String provostPhone;

    public HallInfo(int id, String hallName, String hallType, int totalRooms, int totalSeats, int occupiedSeats, int availableSeats) {
        this.id = id;
        this.hallName = hallName;
        this.hallType = hallType;
        this.totalRooms = totalRooms;
        this.totalSeats = totalSeats;
        this.occupiedSeats = occupiedSeats;
        this.availableSeats = availableSeats;
    }

    public HallInfo(int id, String hallName, String hallType, int totalRooms, int totalSeats, int occupiedSeats, int availableSeats, String provostUserId, String provostName, String provostPhone) {
        this.id = id;
        this.hallName = hallName;
        this.hallType = hallType;
        this.totalRooms = totalRooms;
        this.totalSeats = totalSeats;
        this.occupiedSeats = occupiedSeats;
        this.availableSeats = availableSeats;
        this.provostUserId = provostUserId;
        this.provostName = provostName;
        this.provostPhone = provostPhone;
    }

    public int getId() {
        return id;
    }

    public String getHallName() {
        return hallName;
    }

    public String getHallType() {
        return hallType;
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getOccupiedSeats() {
        return occupiedSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public String getProvostUserId() {
        return provostUserId;
    }

    public void setProvostUserId(String provostUserId) {
        this.provostUserId = provostUserId;
    }

    public String getProvostName() {
        return provostName;
    }

    public void setProvostName(String provostName) {
        this.provostName = provostName;
    }

    public String getProvostPhone() {
        return provostPhone;
    }

    public void setProvostPhone(String provostPhone) {
        this.provostPhone = provostPhone;
    }

    public String getProvostDisplay() {
        if (provostName == null || provostName.isEmpty()) return "Not Assigned";
        if (provostPhone != null && !provostPhone.isEmpty()) {
            return provostName + " (" + provostPhone + ")";
        }
        return provostName;
    }

    public double getOccupancyRate() {
        return totalSeats > 0 ? ((double) occupiedSeats / totalSeats) * 100.0 : 0.0;
    }

    @Override
    public String toString() {
        return hallName;
    }
}
