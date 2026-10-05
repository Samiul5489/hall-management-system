package com.hallmanagement.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class WashroomInfo {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private int id;
    private int hallId;
    private int floorNumber;
    private String locationName;
    private int toiletsCount;
    private int urinalsCount;
    private int showersCount;
    private int waterTaps;
    private int basinsCount;
    private int mirrorsCount;
    private int exhaustFans;
    private int lightsCount;
    private int waterHeaters;
    private int waterFilters;
    private String conditionStatus;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;

    public WashroomInfo() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHallId() { return hallId; }
    public void setHallId(int hallId) { this.hallId = hallId; }

    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public int getToiletsCount() { return toiletsCount; }
    public void setToiletsCount(int toiletsCount) { this.toiletsCount = toiletsCount; }

    public int getUrinalsCount() { return urinalsCount; }
    public void setUrinalsCount(int urinalsCount) { this.urinalsCount = urinalsCount; }

    public int getShowersCount() { return showersCount; }
    public void setShowersCount(int showersCount) { this.showersCount = showersCount; }

    public int getWaterTaps() { return waterTaps; }
    public void setWaterTaps(int waterTaps) { this.waterTaps = waterTaps; }

    public int getBasinsCount() { return basinsCount; }
    public void setBasinsCount(int basinsCount) { this.basinsCount = basinsCount; }

    public int getMirrorsCount() { return mirrorsCount; }
    public void setMirrorsCount(int mirrorsCount) { this.mirrorsCount = mirrorsCount; }

    public int getExhaustFans() { return exhaustFans; }
    public void setExhaustFans(int exhaustFans) { this.exhaustFans = exhaustFans; }

    public int getLightsCount() { return lightsCount; }
    public void setLightsCount(int lightsCount) { this.lightsCount = lightsCount; }

    public int getWaterHeaters() { return waterHeaters; }
    public void setWaterHeaters(int waterHeaters) { this.waterHeaters = waterHeaters; }

    public int getWaterFilters() { return waterFilters; }
    public void setWaterFilters(int waterFilters) { this.waterFilters = waterFilters; }

    public String getConditionStatus() { return conditionStatus != null ? conditionStatus : "GOOD"; }
    public void setConditionStatus(String conditionStatus) { this.conditionStatus = conditionStatus; }

    public LocalDate getLastMaintenanceDate() { return lastMaintenanceDate; }
    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) { this.lastMaintenanceDate = lastMaintenanceDate; }

    public String getLastMaintenanceDateDisplay() {
        return lastMaintenanceDate != null ? lastMaintenanceDate.format(DATE_FMT) : "—";
    }

    public LocalDate getNextMaintenanceDate() { return nextMaintenanceDate; }
    public void setNextMaintenanceDate(LocalDate nextMaintenanceDate) { this.nextMaintenanceDate = nextMaintenanceDate; }

    public String getNextMaintenanceDateDisplay() {
        return nextMaintenanceDate != null ? nextMaintenanceDate.format(DATE_FMT) : "—";
    }
}
