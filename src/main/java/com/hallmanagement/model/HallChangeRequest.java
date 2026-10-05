package com.hallmanagement.model;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HallChangeRequest {
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

    private Date requestDate;
    private String status;
    private String processedBy;
    private String processedByName;
    private Timestamp processedDate;
    private String rejectionReason;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    private String serial;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public HallChangeRequest() {
    }

    public HallChangeRequest(int id, String requestCode, String studentId, String studentName, String studentRoll,
                             String studentDept, int currentHallId, String currentHallName, int currentRoom,
                             int currentSeat, int requestedHallId, String requestedHallName, int requestedRoom,
                             int requestedSeat, Date requestDate, String status, String processedBy,
                             String processedByName, Timestamp processedDate, String rejectionReason,
                             Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.requestCode = requestCode;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentRoll = studentRoll != null ? studentRoll : studentId;
        this.studentDept = studentDept;
        this.currentHallId = currentHallId;
        this.currentHallName = currentHallName;
        this.currentRoom = currentRoom;
        this.currentSeat = currentSeat;
        this.requestedHallId = requestedHallId;
        this.requestedHallName = requestedHallName;
        this.requestedRoom = requestedRoom;
        this.requestedSeat = requestedSeat;
        this.requestDate = requestDate;
        this.status = status;
        this.processedBy = processedBy;
        this.processedByName = processedByName;
        this.processedDate = processedDate;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRequestCode() {
        return requestCode;
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
        return studentRoll;
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

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
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

    public Timestamp getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(Timestamp processedDate) {
        this.processedDate = processedDate;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
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

    public String getFormattedRequestDate() {
        if (requestDate == null) return "N/A";
        try {
            LocalDate ld = requestDate.toLocalDate();
            return ld.format(DATE_FORMATTER);
        } catch (Exception e) {
            return requestDate.toString();
        }
    }

    public String getCurrentResidenceDisplay() {
        return currentHallName + " (Room " + currentRoom + ", Seat " + String.format("%02d", currentSeat) + ")";
    }

    public String getRequestedResidenceDisplay() {
        return requestedHallName + " (Room " + requestedRoom + ", Seat " + String.format("%02d", requestedSeat) + ")";
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
}
