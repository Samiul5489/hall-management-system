package com.hallmanagement.model;

public class RoomInfo {
    private int id;
    private int hallId;
    private String hallName;
    private int roomNumber;
    private int totalSeats;
    private int occupiedSeats;
    private int availableSeats;

    public RoomInfo(int id, int hallId, String hallName, int roomNumber, int totalSeats, int occupiedSeats, int availableSeats) {
        this.id = id;
        this.hallId = hallId;
        this.hallName = hallName;
        this.roomNumber = roomNumber;
        this.totalSeats = totalSeats;
        this.occupiedSeats = occupiedSeats;
        this.availableSeats = availableSeats;
    }

    public int getId() {
        return id;
    }

    public int getRoomId() {
        return id;
    }

    public int getHallId() {
        return hallId;
    }

    public String getHallName() {
        return hallName;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public String getRoomNumberDisplay() {
        return "Room " + roomNumber;
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

    public boolean isFullyOccupied() {
        return availableSeats <= 0;
    }

    public boolean isFullyAvailable() {
        return occupiedSeats == 0;
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + availableSeats + " seats available)";
    }
}
