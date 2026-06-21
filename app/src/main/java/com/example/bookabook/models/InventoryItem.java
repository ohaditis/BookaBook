package com.example.bookabook.models;

import com.example.bookabook.models.enums.InventoryStatus;

public class InventoryItem {
    private String isbn;
    private String storeId;
    private Double storePrice;
    private InventoryStatus status;
    private Integer stockCount;
    private String lastUpdatedByStoreUserId;
    private long lastUpdated;

    public InventoryItem() {
    }

    public InventoryItem(String isbn, String storeId, Double storePrice,
                         InventoryStatus status, Integer stockCount,
                         String lastUpdatedByStoreUserId, long lastUpdated) {
        this.isbn = isbn;
        this.storeId = storeId;
        this.storePrice = storePrice;
        this.status = status;
        this.stockCount = stockCount;
        this.lastUpdatedByStoreUserId = lastUpdatedByStoreUserId;
        this.lastUpdated = lastUpdated;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public Double getStorePrice() {
        return storePrice;
    }

    public void setStorePrice(Double storePrice) {
        this.storePrice = storePrice;
    }

    public InventoryStatus getStatus() {
        return status;
    }

    public void setStatus(InventoryStatus status) {
        this.status = status;
    }

    public Integer getStockCount() {
        return stockCount;
    }

    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }

    public String getLastUpdatedByStoreUserId() {
        return lastUpdatedByStoreUserId;
    }

    public void setLastUpdatedByStoreUserId(String lastUpdatedByStoreUserId) {
        this.lastUpdatedByStoreUserId = lastUpdatedByStoreUserId;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}