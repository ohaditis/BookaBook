package com.example.bookabook.models;

public class WishlistItem {
    private String isbn;
    private boolean notifyWhenAvailable;

    public WishlistItem() {
    }

    public WishlistItem(String isbn, boolean notifyWhenAvailable) {
        this.isbn = isbn;
        this.notifyWhenAvailable = notifyWhenAvailable;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public boolean isNotifyWhenAvailable() {
        return notifyWhenAvailable;
    }

    public void setNotifyWhenAvailable(boolean notifyWhenAvailable) {
        this.notifyWhenAvailable = notifyWhenAvailable;
    }
}