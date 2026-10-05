package com.hallmanagement.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Complaint {
    private int id;
    private String studentId;
    private String studentName;
    private String studentRoll;
    private String studentDepartment;
    private String hallName;
    private int roomNumber;
    private int seatNumber;

    private String provostId;
    private String provostName;
    private String provostHallName;

    private String title;
    private String message;
    private LocalDateTime submittedAt;
    private String status;
    private LocalDateTime processedAt;

    private String responseMessage;
    private LocalDateTime respondedAt;
    private LocalDateTime studentViewedAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public Complaint() {}

    public Complaint(int id, String studentId, String provostId, String title, String message,
                     LocalDateTime submittedAt, String status, LocalDateTime processedAt) {
        this.id = id;
        this.studentId = studentId;
        this.provostId = provostId;
        this.title = title;
        this.message = message;
        this.submittedAt = submittedAt;
        this.status = status;
        this.processedAt = processedAt;
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
        return studentRoll;
    }

    public void setStudentRoll(String studentRoll) {
        this.studentRoll = studentRoll;
    }

    public String getStudentDepartment() {
        return studentDepartment;
    }

    public void setStudentDepartment(String studentDepartment) {
        this.studentDepartment = studentDepartment;
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

    public String getProvostId() {
        return provostId;
    }

    public void setProvostId(String provostId) {
        this.provostId = provostId;
    }

    public String getProvostName() {
        return provostName;
    }

    public void setProvostName(String provostName) {
        this.provostName = provostName;
    }

    public String getProvostHallName() {
        return provostHallName;
    }

    public void setProvostHallName(String provostHallName) {
        this.provostHallName = provostHallName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public String getSubmittedAtFormatted() {
        return submittedAt != null ? submittedAt.format(DATE_ONLY_FORMATTER) : "N/A";
    }

    public String getSubmittedAtFullFormatted() {
        return submittedAt != null ? submittedAt.format(FORMATTER) : "N/A";
    }

    public String getRespondedAtFormatted() {
        return respondedAt != null ? respondedAt.format(FORMATTER) : "N/A";
    }

    public String getResidenceDisplay() {
        if (hallName == null || hallName.isEmpty()) return "Not Assigned";
        return hallName + " (Room " + roomNumber + ", Seat " + seatNumber + ")";
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

    public LocalDateTime getStudentViewedAt() {
        return studentViewedAt;
    }

    public void setStudentViewedAt(LocalDateTime studentViewedAt) {
        this.studentViewedAt = studentViewedAt;
    }

    public boolean isReviewedByStudent() {
        if (studentViewedAt == null) return false;
        if (processedAt != null) {
            return !studentViewedAt.isBefore(processedAt);
        }
        return true;
    }

    public boolean isProcessed() {
        return isApproved() || isRejected();
    }
}
