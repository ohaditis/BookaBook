package com.example.bookabook.models;

public class Store {
    private String storeId;
    private String storeName;
    private String address;
    private String city;
    private String phone;
    private String description;
    private String ownerUid;
    private String storeImageUrl;
    private long createdAt;
    private Double latitude;
    private Double longitude;

    public Store() {}

    public Store(String storeId, String storeName, String address, String city,
                 String phone, String description, String ownerUid, String storeImageUrl, long createdAt) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.address = address;
        this.city = city;
        this.phone = phone;
        this.description = description;
        this.ownerUid = ownerUid;
        this.storeImageUrl = storeImageUrl;
        this.createdAt = createdAt;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    public String getStoreImageUrl() {
        return storeImageUrl;
    }

    public void setStoreImageUrl(String storeImageUrl) {
        this.storeImageUrl = storeImageUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
