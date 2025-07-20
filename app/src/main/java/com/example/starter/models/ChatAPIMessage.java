package com.example.starter.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class ChatAPIMessage {
    @SerializedName("id")
    private int id;
    
    @SerializedName("sender_id")
    private int senderId;
    
    @SerializedName("recipient_id")
    private int recipientId;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;
    
    @SerializedName("tool_id")
    private Integer toolId;
    
    @SerializedName("is_edited")
    private boolean isEdited;

    public ChatAPIMessage() {
        // Default constructor for Gson
    }

    public ChatAPIMessage(int id, int senderId, int recipientId, String message, String createdAt, String updatedAt, Integer toolId, boolean isEdited) {
        this.id = id;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.message = message;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.toolId = toolId;
        this.isEdited = isEdited;
    }

    // Getters
    public int getId() { return id; }
    public int getSenderId() { return senderId; }
    public int getRecipientId() { return recipientId; }
    public String getMessage() { return message; }
    public String getCreatedAt() { return createdAt; }
    public Date getTimestamp() { return new Date(); } // Alias for compatibility - simplified implementation
    public String getUpdatedAt() { return updatedAt; }
    public Integer getToolId() { return toolId; }
    public boolean isEdited() { return isEdited; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
    public void setRecipientId(int recipientId) { this.recipientId = recipientId; }
    public void setMessage(String message) { this.message = message; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setToolId(Integer toolId) { this.toolId = toolId; }
    public void setEdited(boolean edited) { isEdited = edited; }

    @Override
    public String toString() {
        return "ChatAPIMessage{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", recipientId=" + recipientId +
                ", message='" + message + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", toolId=" + toolId +
                ", isEdited=" + isEdited +
                '}';
    }
} 