package com.hallmanagement.util;

import com.hallmanagement.model.User;

public class SessionManager {

    private static volatile SessionManager instance;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    private User currentUser = null;

    public void login(User user) {
        this.currentUser = user;
        System.out.println("[Session] Logged in  → " + user);
    }

    public void logout() {
        System.out.println("[Session] Logged out ← " + currentUser);
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getCurrentUserId()   { return currentUser != null ? currentUser.getUserId() : ""; }
    public String getCurrentUserName() { return currentUser != null ? currentUser.getName()   : ""; }
    public String getCurrentUserRole() { return currentUser != null ? currentUser.getRole()   : ""; }

    public boolean isStudent() {
        return currentUser != null && "STUDENT".equalsIgnoreCase(currentUser.getRole());
    }

    public boolean isProvost() {
        return currentUser != null && "PROVOST".equalsIgnoreCase(currentUser.getRole());
    }
}
