package com.hallmanagement.model;

public class CommonAreaInfo {
    private int id;
    private int hallId;
    private String areaName;
    private int capacity;
    private int acCount;
    private int fanCount;
    private int lightCount;
    private int tableCount;
    private int chairCount;
    private String conditionStatus;
    private String responsibleStaff;
    private String remarks;

    public CommonAreaInfo() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHallId() { return hallId; }
    public void setHallId(int hallId) { this.hallId = hallId; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getAcCount() { return acCount; }
    public void setAcCount(int acCount) { this.acCount = acCount; }

    public int getFanCount() { return fanCount; }
    public void setFanCount(int fanCount) { this.fanCount = fanCount; }

    public int getLightCount() { return lightCount; }
    public void setLightCount(int lightCount) { this.lightCount = lightCount; }

    public int getTableCount() { return tableCount; }
    public void setTableCount(int tableCount) { this.tableCount = tableCount; }

    public int getChairCount() { return chairCount; }
    public void setChairCount(int chairCount) { this.chairCount = chairCount; }

    public String getConditionStatus() { return conditionStatus != null ? conditionStatus : "GOOD"; }
    public void setConditionStatus(String conditionStatus) { this.conditionStatus = conditionStatus; }

    public String getResponsibleStaff() { return responsibleStaff; }
    public void setResponsibleStaff(String responsibleStaff) { this.responsibleStaff = responsibleStaff; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
