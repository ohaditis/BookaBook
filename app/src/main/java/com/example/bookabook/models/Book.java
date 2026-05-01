package com.example.bookabook.models;

import android.media.Image;

import java.util.List;

public class Book {
    private String isbn;
    private String title;
    private String description;
    private List<String> authors;
    private String publisher;
    private String category;
    private String language;
    private Double defaultPrice;
    private String createdByStoreUserId;
    private boolean approved;
    private String coverImageUrl; // Added field for Firebase Storage link

    public Book() {
    }

    public Book(String isbn, String title, String description, List<String> authors,
                String publisher, String category, String language,
                Double defaultPrice, String createdByStoreUserId, boolean approved, String coverImageUrl) {
        this.isbn = isbn;
        this.title = title;
        this.description = description;
        this.authors = authors;
        this.publisher = publisher;
        this.category = category;
        this.language = language;
        this.defaultPrice = defaultPrice;
        this.createdByStoreUserId = createdByStoreUserId;
        this.approved = approved;
        this.coverImageUrl = coverImageUrl;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Double getDefaultPrice() {
        return defaultPrice;
    }

    public void setDefaultPrice(Double defaultPrice) {
        this.defaultPrice = defaultPrice;
    }

    public String getCreatedByStoreUserId() {
        return createdByStoreUserId;
    }

    public void setCreatedByStoreUserId(String createdByStoreUserId) {
        this.createdByStoreUserId = createdByStoreUserId;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

}