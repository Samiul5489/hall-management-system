package com.hallmanagement.model;

public class Hall {
    private int id;
    private String hallName;
    private String hallType;

    public Hall(int id, String hallName, String hallType) {
        this.id = id;
        this.hallName = hallName;
        this.hallType = hallType;
    }

    public int getId() { return id; }
    public String getHallName() { return hallName; }
    public String getHallType() { return hallType; }

    @Override
    public String toString() {
        return hallName;
    }
}
