package com.hallmanagement.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HallLeaveRequest {

    private int id;
    private String studentId;
    private String studentName;
    private String studentRoll;
    private String studentDept;

    private int hallId;
    private String hallName;
    private int roomNumber;
    private int seatNumber;

    private String reason;
    private LocalDate requestDate;
    private String status;

    private String processedBy;
    private String processedByName;
    private LocalDateTime processedDate;
    private String declineReason;
    private LocalDateTime studentViewedAt;

    private String serial;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public HallLeaveRequest() {}

    public HallLeaveRequest(int id, String studentId, int hallId, int roomNumber, int seatNumber,
                            String reason, LocalDate requestDate, String status) {
        this.id = id;
        this.studentId = studentId;
        this.hallId = hallId;
        this.roomNumber = roomNumber;
        this.seatNumber = seatNumber;
        this.reason = reason;
        this.requestDate = requestDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getHallId() {
        return hallId;
    }

    public void setHallId(int hallId) {
        this.hallId = hallId;
    }

    public String getHallName() {
        return hallName;
    }

    public void setHallName(String hallName) {
        this.hallName = hallName;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
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

    public String getDeclineReason() {
        return declineReason;
    }

    public void setDeclineReason(String declineReason) {
        this.declineReason = declineReason;
    }

    public LocalDateTime getStudentViewedAt() {
        return studentViewedAt;
    }

    public void setStudentViewedAt(LocalDateTime studentViewedAt) {
        this.studentViewedAt = studentViewedAt;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getFormattedRequestDate() {
        return requestDate != null ? requestDate.format(DATE_FORMATTER) : "N/A";
    }

    public String getFormattedProcessedDate() {
        return processedDate != null ? processedDate.format(DATE_TIME_FORMATTER) : "N/A";
    }

    public String getResidenceDisplay() {
        if (hallName == null || hallName.isEmpty()) return "NONE";
        return hallName + " (Room " + roomNumber + ", Seat " + String.format("%02d", seatNumber) + ")";
    }

    public String getRequestCode() {
        return "HLR-" + String.format("%03d", id);
    }

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }

    public boolean isApproved() {
        return "APPROVED".equalsIgnoreCase(status);
    }

    public boolean isDeclined() {
        return "DECLINED".equalsIgnoreCase(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(status);
    }

    public boolean isProcessed() {
        return isApproved() || isDeclined();
    }
}
