package com.example.starter.models;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class Tool {
    @SerializedName("id")
    private int id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("price")
    private String price;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("owner_id")
    private Integer ownerId;
    
    @SerializedName("owner_username")
    private String ownerUsername;
    
    @SerializedName("owner_email")
    private String ownerEmail;
    
    @SerializedName("owner_first_name")
    private String ownerFirstName;
    
    @SerializedName("owner_last_name")
    private String ownerLastName;
    
    @SerializedName("image_url")
    private String imageUrl;
    
    @SerializedName("latitude")
    private Double latitude;
    
    @SerializedName("longitude")
    private Double longitude;

    public Tool() {
        // Default constructor for Gson
    }

    public Tool(int id, String name, String price, String description, Integer ownerId, 
                String ownerUsername, String ownerEmail, String ownerFirstName, 
                String ownerLastName, String imageUrl, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
        this.ownerEmail = ownerEmail;
        this.ownerFirstName = ownerFirstName;
        this.ownerLastName = ownerLastName;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getPrice() { return price; }
    public String getDescription() { return description; }
    public Integer getOwnerId() { return ownerId; }
    public String getOwnerUsername() { return ownerUsername; }
    public String getOwnerEmail() { return ownerEmail; }
    public String getOwnerFirstName() { return ownerFirstName; }
    public String getOwnerLastName() { return ownerLastName; }
    public String getImageUrl() { return imageUrl; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPrice(String price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public void setOwnerId(Integer ownerId) { this.ownerId = ownerId; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
    public void setOwnerFirstName(String ownerFirstName) { this.ownerFirstName = ownerFirstName; }
    public void setOwnerLastName(String ownerLastName) { this.ownerLastName = ownerLastName; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    // Helper method to set latitude from string (handles API inconsistency)
    public void setLatitudeFromString(String latString) {
        if (latString != null && !latString.isEmpty()) {
            try {
                this.latitude = Double.parseDouble(latString);
            } catch (NumberFormatException e) {
                this.latitude = null;
            }
        }
    }

    // Helper method to set longitude from string (handles API inconsistency)
    public void setLongitudeFromString(String lngString) {
        if (lngString != null && !lngString.isEmpty()) {
            try {
                this.longitude = Double.parseDouble(lngString);
            } catch (NumberFormatException e) {
                this.longitude = null;
            }
        }
    }

    // Helper method to get full owner name
    public String getOwnerFullName() {
        if (ownerFirstName != null && ownerLastName != null) {
            return ownerFirstName + " " + ownerLastName;
        } else if (ownerFirstName != null) {
            return ownerFirstName;
        } else if (ownerLastName != null) {
            return ownerLastName;
        } else {
            return ownerUsername;
        }
    }

    // Check if tool has valid location
    public boolean hasValidLocation() {
        return latitude != null && longitude != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tool tool = (Tool) o;
        return id == tool.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Tool{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price='" + price + '\'' +
                ", description='" + description + '\'' +
                ", ownerId=" + ownerId +
                ", ownerUsername='" + ownerUsername + '\'' +
                '}';
    }
} 