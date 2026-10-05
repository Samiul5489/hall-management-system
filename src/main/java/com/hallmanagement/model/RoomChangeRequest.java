package com.hallmanagement.model;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RoomChangeRequest {

    private int id;
    private String requestCode;
    private String studentId;
    private String studentName;
    private String studentRoll;
    private String studentDept;

    private int currentHallId;
    private String currentHallName;
    private int currentRoom;
    private int currentSeat;

    private int requestedHallId;
    private String requestedHallName;
    private int requestedRoom;
    private int requestedSeat;

    private String reason;
    private LocalDate requestDate;
    private String status;

    private String processedBy;
    private String processedByName;
    private LocalDateTime processedDate;
    private String rejectionReason;
    private LocalDateTime studentViewedAt;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    private String serial;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public RoomChangeRequest() {}

    public RoomChangeRequest(int id, String studentId, int currentHallId, int currentRoom, int currentSeat,
                             int requestedHallId, int requestedRoom, int requestedSeat,
                             String reason, LocalDate requestDate, String status) {
        this.id = id;
        this.requestCode = String.format("RCR-%03d", id);
        this.studentId = studentId;
        this.currentHallId = currentHallId;
        this.currentRoom = currentRoom;
        this.currentSeat = currentSeat;
        this.requestedHallId = requestedHallId;
        this.requestedRoom = requestedRoom;
        this.requestedSeat = requestedSeat;
        this.reason = reason;
        this.requestDate = requestDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        this.requestCode = String.format("RCR-%03d", id);
    }

    public String getRequestCode() {
        return requestCode != null ? requestCode : String.format("RCR-%03d", id);
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentRoll() {
        return studentRoll != null ? studentRoll : studentId;
    }

    public void setStudentRoll(String studentRoll) {
        this.studentRoll = studentRoll;
    }

    public String getStudentDept() {
        return studentDept;
    }

    public void setStudentDept(String studentDept) {
        this.studentDept = studentDept;
    }

    public int getCurrentHallId() {
        return currentHallId;
    }

    public void setCurrentHallId(int currentHallId) {
        this.currentHallId = currentHallId;
    }

    public String getCurrentHallName() {
        return currentHallName;
    }

    public void setCurrentHallName(String currentHallName) {
        this.currentHallName = currentHallName;
    }

    public int getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(int currentRoom) {
        this.currentRoom = currentRoom;
    }

    public int getCurrentSeat() {
        return currentSeat;
    }

    public void setCurrentSeat(int currentSeat) {
        this.currentSeat = currentSeat;
    }

    public int getRequestedHallId() {
        return requestedHallId;
    }

    public void setRequestedHallId(int requestedHallId) {
        this.requestedHallId = requestedHallId;
    }

    public String getRequestedHallName() {
        return requestedHallName;
    }

    public void setRequestedHallName(String requestedHallName) {
        this.requestedHallName = requestedHallName;
    }

    public int getRequestedRoom() {
        return requestedRoom;
    }

    public void setRequestedRoom(int requestedRoom) {
        this.requestedRoom = requestedRoom;
    }

    public int getRequestedSeat() {
        return requestedSeat;
    }

    public void setRequestedSeat(int requestedSeat) {
        this.requestedSeat = requestedSeat;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    public String getProcessedByName() {
        return processedByName;
    }

    public void setProcessedByName(String processedByName) {
        this.processedByName = processedByName;
    }

    public LocalDateTime getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(LocalDateTime processedDate) {
        this.processedDate = processedDate;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getStudentViewedAt() {
        return studentViewedAt;
    }

    public void setStudentViewedAt(LocalDateTime studentViewedAt) {
        this.studentViewedAt = studentViewedAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }

    public boolean isApproved() {
        return "APPROVED".equalsIgnoreCase(status);
    }

    public boolean isRejected() {
        return "REJECTED".equalsIgnoreCase(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(status);
    }

    public String getFormattedRequestDate() {
        return requestDate != null ? requestDate.format(DATE_FORMATTER) : "-";
    }

    public String getFormattedProcessedDate() {
        return processedDate != null ? processedDate.format(DATE_TIME_FORMATTER) : "-";
    }

    public String getCurrentResidenceDisplay() {
        if (currentHallName == null || currentRoom == 0) return "NONE";
        return currentHallName + " — Room " + currentRoom + " (Seat " + String.format("%02d", currentSeat) + ")";
    }

    public String getRequestedResidenceDisplay() {
        String hall = requestedHallName != null ? requestedHallName : (currentHallName != null ? currentHallName : "Current Hall");
        return hall + " — Room " + requestedRoom + " (Seat " + String.format("%02d", requestedSeat) + ")";
    }
}
