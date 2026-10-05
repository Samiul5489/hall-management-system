package com.hallmanagement.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CleaningSchedule {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private int id;
    private int hallId;
    private String areaType;
    private String frequency;
    private String responsibleStaff;
    private String status;
    private LocalDate lastCleanedDate;
    private LocalDate nextScheduledDate;
    private String remarks;

    public CleaningSchedule() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHallId() { return hallId; }
    public void setHallId(int hallId) { this.hallId = hallId; }

    public String getAreaType() { return areaType; }
    public void setAreaType(String areaType) { this.areaType = areaType; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getResponsibleStaff() { return responsibleStaff; }
    public void setResponsibleStaff(String responsibleStaff) { this.responsibleStaff = responsibleStaff; }

    public String getStatus() { return status != null ? status : "COMPLETED"; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getLastCleanedDate() { return lastCleanedDate; }
    public void setLastCleanedDate(LocalDate lastCleanedDate) { this.lastCleanedDate = lastCleanedDate; }

    public String getLastCleanedDateDisplay() {
        return lastCleanedDate != null ? lastCleanedDate.format(DATE_FMT) : "—";
    }

    public LocalDate getNextScheduledDate() { return nextScheduledDate; }
    public void setNextScheduledDate(LocalDate nextScheduledDate) { this.nextScheduledDate = nextScheduledDate; }

    public String getNextScheduledDateDisplay() {
        return nextScheduledDate != null ? nextScheduledDate.format(DATE_FMT) : "—";
    }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
