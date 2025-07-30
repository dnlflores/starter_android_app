package com.example.starter.model;

import com.google.gson.annotations.SerializedName;

public class Tool {
    private int id;
    private String name;
    private double price;
    private String description;
    
    @SerializedName("owner_id")
    private int ownerId;
    
    @SerializedName("owner_username")
    private String ownerUsername;
    
    @SerializedName("image_url")
    private String imageUrl;
    
    private double latitude;
    private double longitude;

    // Default constructor for Gson
    public Tool() {}

    // Constructor
    public Tool(int id, String name, double price, String description, int ownerId, String ownerUsername, String imageUrl, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Tool{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", ownerId=" + ownerId +
                ", ownerUsername='" + ownerUsername + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}