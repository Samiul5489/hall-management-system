package com.hallmanagement.model;

public class HallSummaryStats {
    private int hallId;
    private String hallName;
    private String hallType;
    private int totalFloors;
    private int totalRooms;
    private int totalSeats;
    private int occupiedSeats;
    private int vacantSeats;
    private int totalStudents;
    private int residentialStudents;
    private int nonResidentialStudents;
    private int totalStaff;
    private int totalAc;
    private int totalFans;
    private int totalLights;
    private int totalWashrooms;
    private int totalCctv;
    private int totalFireSafety;
    private int pendingMaintenance;
    private double occupancyRate;

    public HallSummaryStats() {}

    public int getHallId() { return hallId; }
    public void setHallId(int hallId) { this.hallId = hallId; }

    public String getHallName() { return hallName; }
    public void setHallName(String hallName) { this.hallName = hallName; }

    public String getHallType() { return hallType; }
    public void setHallType(String hallType) { this.hallType = hallType; }

    public int getTotalFloors() { return totalFloors; }
    public void setTotalFloors(int totalFloors) { this.totalFloors = totalFloors; }

    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public int getOccupiedSeats() { return occupiedSeats; }
    public void setOccupiedSeats(int occupiedSeats) { this.occupiedSeats = occupiedSeats; }

    public int getVacantSeats() { return vacantSeats; }
    public void setVacantSeats(int vacantSeats) { this.vacantSeats = vacantSeats; }

    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

    public int getResidentialStudents() { return residentialStudents; }
    public void setResidentialStudents(int residentialStudents) { this.residentialStudents = residentialStudents; }

    public int getNonResidentialStudents() { return nonResidentialStudents; }
    public void setNonResidentialStudents(int nonResidentialStudents) { this.nonResidentialStudents = nonResidentialStudents; }

    public int getTotalStaff() { return totalStaff; }
    public void setTotalStaff(int totalStaff) { this.totalStaff = totalStaff; }

    public int getTotalAc() { return totalAc; }
    public void setTotalAc(int totalAc) { this.totalAc = totalAc; }

    public int getTotalFans() { return totalFans; }
    public void setTotalFans(int totalFans) { this.totalFans = totalFans; }

    public int getTotalLights() { return totalLights; }
    public void setTotalLights(int totalLights) { this.totalLights = totalLights; }

    public int getTotalWashrooms() { return totalWashrooms; }
    public void setTotalWashrooms(int totalWashrooms) { this.totalWashrooms = totalWashrooms; }

    public int getTotalCctv() { return totalCctv; }
    public void setTotalCctv(int totalCctv) { this.totalCctv = totalCctv; }

    public int getTotalFireSafety() { return totalFireSafety; }
    public void setTotalFireSafety(int totalFireSafety) { this.totalFireSafety = totalFireSafety; }

    public int getPendingMaintenance() { return pendingMaintenance; }
    public void setPendingMaintenance(int pendingMaintenance) { this.pendingMaintenance = pendingMaintenance; }

    public double getOccupancyRate() {
        if (totalSeats > 0) {
            return ((double) occupiedSeats / totalSeats) * 100.0;
        }
        return 0.0;
    }

    public String getOccupancyPercentageFormatted() {
        return String.format("%.1f%%", getOccupancyRate());
    }
}
