package com.hallmanagement.model;

public class FloorInfo {
    private int floorNumber;
    private String floorName;
    private int totalRooms;
    private int totalSeats;
    private int occupiedSeats;
    private int vacantSeats;
    private int acCount;
    private int fansCount;
    private int lightsCount;
    private int windowsCount;
    private int doorsCount;
    private int washroomsCount;
    private int toiletsCount;
    private int waterTaps;
    private int emergencyExits;

    public FloorInfo() {}

    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    public String getFloorName() { return floorName; }
    public void setFloorName(String floorName) { this.floorName = floorName; }

    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public int getOccupiedSeats() { return occupiedSeats; }
    public void setOccupiedSeats(int occupiedSeats) { this.occupiedSeats = occupiedSeats; }

    public int getVacantSeats() { return vacantSeats; }
    public void setVacantSeats(int vacantSeats) { this.vacantSeats = vacantSeats; }

    public int getAcCount() { return acCount; }
    public void setAcCount(int acCount) { this.acCount = acCount; }

    public int getFansCount() { return fansCount; }
    public void setFansCount(int fansCount) { this.fansCount = fansCount; }

    public int getLightsCount() { return lightsCount; }
    public void setLightsCount(int lightsCount) { this.lightsCount = lightsCount; }

    public int getWindowsCount() { return windowsCount; }
    public void setWindowsCount(int windowsCount) { this.windowsCount = windowsCount; }

    public int getDoorsCount() { return doorsCount; }
    public void setDoorsCount(int doorsCount) { this.doorsCount = doorsCount; }

    public int getWashroomsCount() { return washroomsCount; }
    public void setWashroomsCount(int washroomsCount) { this.washroomsCount = washroomsCount; }

    public int getToiletsCount() { return toiletsCount; }
    public void setToiletsCount(int toiletsCount) { this.toiletsCount = toiletsCount; }

    public int getWaterTaps() { return waterTaps; }
    public void setWaterTaps(int waterTaps) { this.waterTaps = waterTaps; }

    public int getEmergencyExits() { return emergencyExits; }
    public void setEmergencyExits(int emergencyExits) { this.emergencyExits = emergencyExits; }

    public String getOccupancyRateDisplay() {
        if (totalSeats > 0) {
            return String.format("%.1f%%", ((double) occupiedSeats / totalSeats) * 100.0);
        }
        return "0.0%";
    }
}
