package com.hallmanagement.model;

public class User {

    private final int    id;
    private final String userId;
    private String name;
    private String phone;
    private final String role;
    private final String status;

    public User(int id, String userId, String name, String phone, String role, String status) {
        this.id     = id;
        this.userId = userId;
        this.name   = name;
        this.phone  = phone;
        this.role   = role;
        this.status = status;
    }

    public int    getId()     { return id;     }
    public String getUserId() { return userId; }
    public String getName()   { return name;   }
    public void setName(String name) { this.name = name; }
    public String getPhone()  { return phone;  }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRole()   { return role;   }
    public String getStatus() { return status; }

    public boolean isStudent() { return "STUDENT".equalsIgnoreCase(role); }
    public boolean isProvost() { return "PROVOST".equalsIgnoreCase(role); }
    public boolean isActive()  { return "ACTIVE".equalsIgnoreCase(status); }

    @Override
    public String toString() {
        return "User{id=" + id + ", userId='" + userId + "', role='" + role +
               "', status='" + status + "'}";
    }
}
