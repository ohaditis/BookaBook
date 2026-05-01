package com.example.bookabook.models;

public class StoreUser {
    private String uid;
    private String loginEmail;
    private String phone;
    private String storeId;
    private boolean active;

    public StoreUser() {
    }

    public StoreUser(String uid, String loginEmail, String phone, boolean active) {
        this.uid = uid;
        this.loginEmail = loginEmail;
        this.phone = phone;
        this.active = active;
        this.storeId = null;
    }

    public StoreUser(String uid, String loginEmail, String phone, String storeId, boolean active) {
        this.uid = uid;
        this.phone = phone;
        this.loginEmail = loginEmail;
        this.storeId = storeId;
        this.active = active;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLoginEmail() {
        return loginEmail;
    }

    public void setLoginEmail(String loginEmail) {
        this.loginEmail = loginEmail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
