package com.example.bookabook.models;

public class UserProfile {
    private String uid;
    private String email;
    private String displayName;
    private boolean active;
    private String notificationTime; // "None", "08:00", "20:00"

    public UserProfile() {
    }

    public UserProfile(String email) {
        this.email = email;
        this.displayName = email;
        this.active = true;
        this.notificationTime = "None";
    }

    public UserProfile(String uid, String email, String displayName, boolean active) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.active = active;
        this.notificationTime = "None";
    }

    public UserProfile(String uid, String email, String displayName, boolean active, String notificationTime) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.active = active;
        this.notificationTime = notificationTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(String notificationTime) {
        this.notificationTime = notificationTime;
    }
}