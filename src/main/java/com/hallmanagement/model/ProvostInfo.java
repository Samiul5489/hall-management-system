package com.hallmanagement.model;

public class ProvostInfo {
    private String userId;
    private String name;
    private String phone;
    private int hallId;
    private String hallName;
    private Integer age;
    private String email;
    private String designation;
    private String officeRoom;

    public ProvostInfo() {}

    public ProvostInfo(String userId, String name, String phone, int hallId, String hallName) {
        this(userId, name, phone, hallId, hallName, 45, userId + "@ruet.ac.bd", "Professor & Provost", "Room 101, Administrative Block");
    }

    public ProvostInfo(String userId, String name, String phone, int hallId, String hallName,
                       Integer age, String email, String designation, String officeRoom) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.hallId = hallId;
        this.hallName = hallName;
        this.age = age != null ? age : 45;
        this.email = (email != null && !email.isEmpty()) ? email : (userId + "@ruet.ac.bd");
        this.designation = (designation != null && !designation.isEmpty()) ? designation : "Professor & Provost";
        this.officeRoom = (officeRoom != null && !officeRoom.isEmpty()) ? officeRoom : "Provost Office, Ground Floor";
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getOfficeRoom() {
        return officeRoom;
    }

    public void setOfficeRoom(String officeRoom) {
        this.officeRoom = officeRoom;
    }

    @Override
    public String toString() {
        return hallName + " — " + name + " (" + userId + ")";
    }
}
