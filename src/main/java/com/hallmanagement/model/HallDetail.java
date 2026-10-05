package com.hallmanagement.model;

public class HallDetail {
    private int hallId;
    private String hallCode;
    private String hallName;
    private String hallType;
    private int establishedYear;
    private String address;
    private String description;
    private int totalFloors;
    private int totalStaff;
    private int nonResidentialStudents;
    private String provostName;
    private String provostPhone;
    private String assistantProvosts;
    private String officeContact;
    private String emergencyContact;
    private String email;

    public HallDetail() {}

    public int getHallId() { return hallId; }
    public void setHallId(int hallId) { this.hallId = hallId; }

    public String getHallCode() { return hallCode; }
    public void setHallCode(String hallCode) { this.hallCode = hallCode; }

    public String getHallName() { return hallName; }
    public void setHallName(String hallName) { this.hallName = hallName; }

    public String getHallType() { return hallType; }
    public void setHallType(String hallType) { this.hallType = hallType; }

    public int getEstablishedYear() { return establishedYear; }
    public void setEstablishedYear(int establishedYear) { this.establishedYear = establishedYear; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTotalFloors() { return totalFloors; }
    public void setTotalFloors(int totalFloors) { this.totalFloors = totalFloors; }

    public int getTotalStaff() { return totalStaff; }
    public void setTotalStaff(int totalStaff) { this.totalStaff = totalStaff; }

    public int getNonResidentialStudents() { return nonResidentialStudents; }
    public void setNonResidentialStudents(int nonResidentialStudents) { this.nonResidentialStudents = nonResidentialStudents; }

    public String getProvostName() { return provostName; }
    public void setProvostName(String provostName) { this.provostName = provostName; }

    public String getProvostPhone() { return provostPhone; }
    public void setProvostPhone(String provostPhone) { this.provostPhone = provostPhone; }

    public String getAssistantProvosts() { return assistantProvosts; }
    public void setAssistantProvosts(String assistantProvosts) { this.assistantProvosts = assistantProvosts; }

    public String getOfficeContact() { return officeContact; }
    public void setOfficeContact(String officeContact) { this.officeContact = officeContact; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
