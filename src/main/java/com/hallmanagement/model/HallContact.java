package com.hallmanagement.model;

public class HallContact {
    private int id;
    private int hallId;
    private String category;
    private String designation;
    private String name;
    private String phone;
    private String email;
    private String availability;

    public HallContact() {}

    public HallContact(int id, int hallId, String category, String designation, String name, String phone, String email, String availability) {
        this.id = id;
        this.hallId = hallId;
        this.category = category;
        this.designation = designation;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.availability = availability;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHallId() { return hallId; }
    public void setHallId(int hallId) { this.hallId = hallId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvailability() { return availability != null ? availability : "Office Hours"; }
    public void setAvailability(String availability) { this.availability = availability; }
}
