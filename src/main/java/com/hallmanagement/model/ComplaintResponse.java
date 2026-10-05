package com.hallmanagement.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ComplaintResponse {
    private int id;
    private int complaintId;
    private String provostId;
    private String responseMessage;
    private LocalDateTime respondedAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public ComplaintResponse() {}

    public ComplaintResponse(int id, int complaintId, String provostId, String responseMessage, LocalDateTime respondedAt) {
        this.id = id;
        this.complaintId = complaintId;
        this.provostId = provostId;
        this.responseMessage = responseMessage;
        this.respondedAt = respondedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(int complaintId) {
        this.complaintId = complaintId;
    }

    public String getProvostId() {
        return provostId;
    }

    public void setProvostId(String provostId) {
        this.provostId = provostId;
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

    public String getRespondedAtFormatted() {
        return respondedAt != null ? respondedAt.format(FORMATTER) : "N/A";
    }
}
