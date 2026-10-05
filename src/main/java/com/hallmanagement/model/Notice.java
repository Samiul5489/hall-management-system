package com.hallmanagement.model;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Notice {
    private int id;
    private String title;
    private String content;
    private Date postedDate;
    private String postedBy;
    private String postedByName;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    private String serial;
    private boolean read = true;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public Notice() {
    }

    public Notice(int id, String title, String content, Date postedDate, String postedBy, String postedByName, Timestamp createdAt, Timestamp updatedAt) {
        this(id, title, content, postedDate, postedBy, postedByName, createdAt, updatedAt, true);
    }

    public Notice(int id, String title, String content, Date postedDate, String postedBy, String postedByName, Timestamp createdAt, Timestamp updatedAt, boolean read) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.postedDate = postedDate;
        this.postedBy = postedBy;
        this.postedByName = postedByName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.read = read;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(Date postedDate) {
        this.postedDate = postedDate;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public String getPostedByName() {
        return postedByName;
    }

    public void setPostedByName(String postedByName) {
        this.postedByName = postedByName;
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

    public String getFormattedDate() {
        if (postedDate == null) return "N/A";
        try {
            LocalDate ld = postedDate.toLocalDate();
            return ld.format(DATE_FORMATTER);
        } catch (Exception e) {
            return postedDate.toString();
        }
    }

    public String getContentPreview() {
        if (content == null || content.trim().isEmpty()) return "";
        String clean = content.replaceAll("\\s+", " ").trim();
        if (clean.length() <= 65) {
            return clean;
        }
        return clean.substring(0, 62) + "...";
    }

    public String getAuthorDisplay() {
        if (postedByName != null && !postedByName.trim().isEmpty()) {
            return postedByName + " (" + postedBy + ")";
        }
        return postedBy != null ? postedBy : "Authority";
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
