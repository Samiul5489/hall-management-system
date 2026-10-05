package com.hallmanagement.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class WaterSystemInfo {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private int id;
    private int hallId;
    private String sourceType;
    private String deepTubeWell;
    private int pumpCount;
    private int tankCount;
    private int totalTankCapacityLiters;
    private int filtersCount;
    private int purifiersCount;
    private String supplySchedule;
    private int drinkingWaterPoints;
    private int tapCount;
    private String pumpCondition;
    private String tankCondition;
    private LocalDate lastCleaningDate;
    private LocalDate nextCleaningDate;

    public WaterSystemInfo() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHallId() { return hallId; }
    public void setHallId(int hallId) { this.hallId = hallId; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getDeepTubeWell() { return deepTubeWell; }
    public void setDeepTubeWell(String deepTubeWell) { this.deepTubeWell = deepTubeWell; }

    public int getPumpCount() { return pumpCount; }
    public void setPumpCount(int pumpCount) { this.pumpCount = pumpCount; }

    public int getTankCount() { return tankCount; }
    public void setTankCount(int tankCount) { this.tankCount = tankCount; }

    public int getTotalTankCapacityLiters() { return totalTankCapacityLiters; }
    public void setTotalTankCapacityLiters(int totalTankCapacityLiters) { this.totalTankCapacityLiters = totalTankCapacityLiters; }

    public int getFiltersCount() { return filtersCount; }
    public void setFiltersCount(int filtersCount) { this.filtersCount = filtersCount; }

    public int getPurifiersCount() { return purifiersCount; }
    public void setPurifiersCount(int purifiersCount) { this.purifiersCount = purifiersCount; }

    public String getSupplySchedule() { return supplySchedule; }
    public void setSupplySchedule(String supplySchedule) { this.supplySchedule = supplySchedule; }

    public int getDrinkingWaterPoints() { return drinkingWaterPoints; }
    public void setDrinkingWaterPoints(int drinkingWaterPoints) { this.drinkingWaterPoints = drinkingWaterPoints; }

    public int getTapCount() { return tapCount; }
    public void setTapCount(int tapCount) { this.tapCount = tapCount; }

    public String getPumpCondition() { return pumpCondition != null ? pumpCondition : "WORKING"; }
    public void setPumpCondition(String pumpCondition) { this.pumpCondition = pumpCondition; }

    public String getTankCondition() { return tankCondition != null ? tankCondition : "GOOD"; }
    public void setTankCondition(String tankCondition) { this.tankCondition = tankCondition; }

    public LocalDate getLastCleaningDate() { return lastCleaningDate; }
    public void setLastCleaningDate(LocalDate lastCleaningDate) { this.lastCleaningDate = lastCleaningDate; }

    public String getLastCleaningDateDisplay() {
        return lastCleaningDate != null ? lastCleaningDate.format(DATE_FMT) : "—";
    }

    public LocalDate getNextCleaningDate() { return nextCleaningDate; }
    public void setNextCleaningDate(LocalDate nextCleaningDate) { this.nextCleaningDate = nextCleaningDate; }

    public String getNextCleaningDateDisplay() {
        return nextCleaningDate != null ? nextCleaningDate.format(DATE_FMT) : "—";
    }
}
