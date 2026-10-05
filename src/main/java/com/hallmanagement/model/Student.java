package com.hallmanagement.model;

public class Student {
    private int dbId;
    private String userId;
    private String studentId;
    private String name;
    private String phone;
    private String email;
    private String department;
    private String year;
    private Integer currentHallId;
    private String currentHall;
    private Integer currentRoom;
    private Integer currentSeat;

    public Student(int dbId, String userId, String name, String phone, String department, String year, String currentHall, Integer currentRoom, Integer currentSeat) {
        this.dbId = dbId;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.department = department;
        this.year = year;
        this.currentHall = currentHall;
        this.currentRoom = currentRoom;
        this.currentSeat = currentSeat;
    }

    public Student(int dbId, String userId, String name, String phone, String department, String year, Integer currentHallId, String currentHall, Integer currentRoom, Integer currentSeat) {
        this.dbId = dbId;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.department = department;
        this.year = year;
        this.currentHallId = currentHallId;
        this.currentHall = currentHall;
        this.currentRoom = currentRoom;
        this.currentSeat = currentSeat;
    }

    public Student(int dbId, String userId, String name, String phone, String email, String department, String year, Integer currentHallId, String currentHall, Integer currentRoom, Integer currentSeat) {
        this.dbId = dbId;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.department = department;
        this.year = year;
        this.currentHallId = currentHallId;
        this.currentHall = currentHall;
        this.currentRoom = currentRoom;
        this.currentSeat = currentSeat;
    }

    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRollNumber() {
        return userId;
    }

    public String getStudentId() {
        return studentId != null ? studentId : userId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getYear() {
        return year;
    }

    public String getAcademicYear() {
        return year != null ? year : "1";
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Integer getCurrentHallId() {
        return currentHallId;
    }

    public void setCurrentHallId(Integer currentHallId) {
        this.currentHallId = currentHallId;
    }

    public String getCurrentHall() {
        return currentHall;
    }

    public void setCurrentHall(String currentHall) {
        this.currentHall = currentHall;
    }

    public Integer getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Integer currentRoom) {
        this.currentRoom = currentRoom;
    }

    public Integer getCurrentSeat() {
        return currentSeat;
    }

    public void setCurrentSeat(Integer currentSeat) {
        this.currentSeat = currentSeat;
    }

    public String getResidenceDisplay() {
        if (currentHall == null || currentHall.isEmpty()) return "Not Assigned";
        return currentHall + " (Room " + currentRoom + ", Seat " + currentSeat + ")";
    }

    @Override
    public String toString() {
        return "Student{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
