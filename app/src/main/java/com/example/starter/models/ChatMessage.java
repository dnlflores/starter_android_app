package com.example.starter.models;

import java.util.Date;
import java.util.Objects;

public class ChatMessage {
    private int id;
    private int senderId;
    private String text;
    private Date date;
    private Integer toolId;
    private boolean isEdited;
    private Date updatedAt;

    public ChatMessage() {
        // Default constructor
    }

    public ChatMessage(int id, int senderId, String text, Date date, Integer toolId, boolean isEdited, Date updatedAt) {
        this.id = id;
        this.senderId = senderId;
        this.text = text;
        this.date = date;
        this.toolId = toolId;
        this.isEdited = isEdited;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getId() { return id; }
    public int getSenderId() { return senderId; }
    public String getText() { return text; }
    public String getMessage() { return text; } // Alias for getText() for compatibility
    public Date getDate() { return date; }
    public Date getTimestamp() { return date; } // Alias for getDate() for compatibility
    public Integer getToolId() { return toolId; }
    public boolean isEdited() { return isEdited; }
    public Date getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
    public void setText(String text) { this.text = text; }
    public void setMessage(String text) { this.text = text; } // Alias for setText() for compatibility
    public void setDate(Date date) { this.date = date; }
    public void setTimestamp(Date date) { this.date = date; } // Alias for setDate() for compatibility
    public void setToolId(Integer toolId) { this.toolId = toolId; }
    public void setEdited(boolean edited) { isEdited = edited; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMessage that = (ChatMessage) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", text='" + text + '\'' +
                ", date=" + date +
                ", toolId=" + toolId +
                ", isEdited=" + isEdited +
                '}';
    }
} 