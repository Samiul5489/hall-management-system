package com.hallmanagement.model;

import java.util.ArrayList;
import java.util.List;

public class RoomFullProfile {
    private int roomId;
    private int hallId;
    private String hallName;
    private int roomNumber;
    private int floorNumber;
    private String roomType;
    private int totalSeats;
    private int occupiedSeats;
    private int availableSeats;
    private double roomSizeSqft;
    private int windowCount;
    private int doorCount;
    private boolean hasBalcony;
    private boolean hasAttachedBath;
    private String conditionStatus;

    private int acCount;
    private int ceilingFanCount;
    private int wallFanCount;
    private int exhaustFanCount;
    private int tubeLightCount;
    private int ledLightCount;
    private int bulbCount;
    private int emergencyLightCount;
    private int nightLightCount;
    private int switchCount;
    private int socketCount;
    private int powerOutletCount;
    private String otherElectrical;

    private int bedCount;
    private int tableCount;
    private int chairCount;
    private int wardrobeCount;
    private int bookshelfCount;
    private int mattressCount;
    private int mirrorCount;
    private int curtainCount;
    private int dustbinCount;
    private int goodConditionCount;
    private int damagedCount;
    private int underRepairCount;
    private int missingCount;
    private String furnitureRemarks;

    private int pendingMaintenanceCount;
    private String latestMaintenanceIssue;

    private List<SeatInfo> occupants = new ArrayList<>();

    public RoomFullProfile() {}

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public int getHallId() { return hallId; }
    public void setHallId(int hallId) { this.hallId = hallId; }

    public String getHallName() { return hallName; }
    public void setHallName(String hallName) { this.hallName = hallName; }

    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    public String getRoomType() { return roomType != null ? roomType : "STANDARD"; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public int getOccupiedSeats() { return occupiedSeats; }
    public void setOccupiedSeats(int occupiedSeats) { this.occupiedSeats = occupiedSeats; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public double getRoomSizeSqft() { return roomSizeSqft; }
    public void setRoomSizeSqft(double roomSizeSqft) { this.roomSizeSqft = roomSizeSqft; }

    public int getWindowCount() { return windowCount; }
    public void setWindowCount(int windowCount) { this.windowCount = windowCount; }

    public int getDoorCount() { return doorCount; }
    public void setDoorCount(int doorCount) { this.doorCount = doorCount; }

    public boolean isHasBalcony() { return hasBalcony; }
    public void setHasBalcony(boolean hasBalcony) { this.hasBalcony = hasBalcony; }

    public boolean isHasAttachedBath() { return hasAttachedBath; }
    public void setHasAttachedBath(boolean hasAttachedBath) { this.hasAttachedBath = hasAttachedBath; }

    public String getConditionStatus() { return conditionStatus != null ? conditionStatus : "GOOD"; }
    public void setConditionStatus(String conditionStatus) { this.conditionStatus = conditionStatus; }

    public int getAcCount() { return acCount; }
    public void setAcCount(int acCount) { this.acCount = acCount; }

    public int getCeilingFanCount() { return ceilingFanCount; }
    public void setCeilingFanCount(int ceilingFanCount) { this.ceilingFanCount = ceilingFanCount; }

    public int getWallFanCount() { return wallFanCount; }
    public void setWallFanCount(int wallFanCount) { this.wallFanCount = wallFanCount; }

    public int getExhaustFanCount() { return exhaustFanCount; }
    public void setExhaustFanCount(int exhaustFanCount) { this.exhaustFanCount = exhaustFanCount; }

    public int getTotalFans() { return ceilingFanCount + wallFanCount + exhaustFanCount; }

    public int getTubeLightCount() { return tubeLightCount; }
    public void setTubeLightCount(int tubeLightCount) { this.tubeLightCount = tubeLightCount; }

    public int getLedLightCount() { return ledLightCount; }
    public void setLedLightCount(int ledLightCount) { this.ledLightCount = ledLightCount; }

    public int getBulbCount() { return bulbCount; }
    public void setBulbCount(int bulbCount) { this.bulbCount = bulbCount; }

    public int getEmergencyLightCount() { return emergencyLightCount; }
    public void setEmergencyLightCount(int emergencyLightCount) { this.emergencyLightCount = emergencyLightCount; }

    public int getTotalLights() { return tubeLightCount + ledLightCount + bulbCount + emergencyLightCount; }

    public int getNightLightCount() { return nightLightCount; }
    public void setNightLightCount(int nightLightCount) { this.nightLightCount = nightLightCount; }

    public int getSwitchCount() { return switchCount; }
    public void setSwitchCount(int switchCount) { this.switchCount = switchCount; }

    public int getSocketCount() { return socketCount; }
    public void setSocketCount(int socketCount) { this.socketCount = socketCount; }

    public int getPowerOutletCount() { return powerOutletCount; }
    public void setPowerOutletCount(int powerOutletCount) { this.powerOutletCount = powerOutletCount; }

    public String getOtherElectrical() { return otherElectrical; }
    public void setOtherElectrical(String otherElectrical) { this.otherElectrical = otherElectrical; }

    public int getBedCount() { return bedCount; }
    public void setBedCount(int bedCount) { this.bedCount = bedCount; }

    public int getTableCount() { return tableCount; }
    public void setTableCount(int tableCount) { this.tableCount = tableCount; }

    public int getChairCount() { return chairCount; }
    public void setChairCount(int chairCount) { this.chairCount = chairCount; }

    public int getWardrobeCount() { return wardrobeCount; }
    public void setWardrobeCount(int wardrobeCount) { this.wardrobeCount = wardrobeCount; }

    public int getBookshelfCount() { return bookshelfCount; }
    public void setBookshelfCount(int bookshelfCount) { this.bookshelfCount = bookshelfCount; }

    public int getMattressCount() { return mattressCount; }
    public void setMattressCount(int mattressCount) { this.mattressCount = mattressCount; }

    public int getMirrorCount() { return mirrorCount; }
    public void setMirrorCount(int mirrorCount) { this.mirrorCount = mirrorCount; }

    public int getCurtainCount() { return curtainCount; }
    public void setCurtainCount(int curtainCount) { this.curtainCount = curtainCount; }

    public int getDustbinCount() { return dustbinCount; }
    public void setDustbinCount(int dustbinCount) { this.dustbinCount = dustbinCount; }

    public int getGoodConditionCount() { return goodConditionCount; }
    public void setGoodConditionCount(int goodConditionCount) { this.goodConditionCount = goodConditionCount; }

    public int getDamagedCount() { return damagedCount; }
    public void setDamagedCount(int damagedCount) { this.damagedCount = damagedCount; }

    public int getUnderRepairCount() { return underRepairCount; }
    public void setUnderRepairCount(int underRepairCount) { this.underRepairCount = underRepairCount; }

    public int getMissingCount() { return missingCount; }
    public void setMissingCount(int missingCount) { this.missingCount = missingCount; }

    public String getFurnitureRemarks() { return furnitureRemarks; }
    public void setFurnitureRemarks(String furnitureRemarks) { this.furnitureRemarks = furnitureRemarks; }

    public int getPendingMaintenanceCount() { return pendingMaintenanceCount; }
    public void setPendingMaintenanceCount(int pendingMaintenanceCount) { this.pendingMaintenanceCount = pendingMaintenanceCount; }

    public String getLatestMaintenanceIssue() { return latestMaintenanceIssue; }
    public void setLatestMaintenanceIssue(String latestMaintenanceIssue) { this.latestMaintenanceIssue = latestMaintenanceIssue; }

    public List<SeatInfo> getOccupants() { return occupants; }
    public void setOccupants(List<SeatInfo> occupants) { this.occupants = occupants; }

    public String getStatus() {
        if (availableSeats == 0) return "FULL";
        if (occupiedSeats == 0) return "VACANT";
        return "AVAILABLE (" + availableSeats + " Free)";
    }

    public String getBalconyDisplay() { return hasBalcony ? "Yes" : "No"; }
    public String getAttachedBathDisplay() { return hasAttachedBath ? "Yes" : "No"; }
}
