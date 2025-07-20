package com.example.starter.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Chat {
    private String id;
    private int otherUserId;
    private String otherUsername;
    private Integer toolId;
    private String toolName;
    private List<ChatMessage> messages;
    private String lastMessage;
    private Date timestamp;

    public Chat() {
        this.messages = new ArrayList<>();
    }

    public Chat(String id, int otherUserId, String otherUsername, Integer toolId, String toolName) {
        this.id = id;
        this.otherUserId = otherUserId;
        this.otherUsername = otherUsername;
        this.toolId = toolId;
        this.toolName = toolName;
        this.messages = new ArrayList<>();
    }

    public Chat(String id, int otherUserId, String otherUsername, Integer toolId, String toolName, List<ChatMessage> messages) {
        this.id = id;
        this.otherUserId = otherUserId;
        this.otherUsername = otherUsername;
        this.toolId = toolId;
        this.toolName = toolName;
        this.messages = messages != null ? messages : new ArrayList<>();
    }

    // Generate a unique identifier for tool-specific conversations
    public static String generateId(int otherUserId, Integer toolId) {
        if (toolId != null) {
            return otherUserId + "_" + toolId;
        } else {
            return String.valueOf(otherUserId);
        }
    }

    // Display name for the chat - prioritizes tool name over username
    public String getDisplayName() {
        return toolName != null ? toolName : otherUsername;
    }

    // Secondary information for the chat list
    public String getDisplaySubtitle() {
        if (toolName != null) {
            return "Tool conversation"; // For tool-based conversations
        } else {
            return "General chat";
        }
    }

    // Title for chat detail view
    public String getChatTitle() {
        if (toolName != null) {
            return toolName; // Just show the tool name
        } else {
            return otherUsername;
        }
    }

    // Get the latest message (messages are sorted with newest first)
    public ChatMessage getLatestMessage() {
        if (messages != null && !messages.isEmpty()) {
            return messages.get(0);
        }
        return null;
    }

    // Add a message to the beginning (newest first)
    public void addMessage(ChatMessage message) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(0, message);
    }

    // Add a message to the end (oldest first)
    public void appendMessage(ChatMessage message) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(message);
    }

    // Getters
    public String getId() { return id; }
    public int getOtherUserId() { return otherUserId; }
    public String getOtherUsername() { return otherUsername; }
    public Integer getToolId() { return toolId; }
    public String getToolName() { return toolName; }
    public List<ChatMessage> getMessages() { return messages; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setId(int id) { this.id = String.valueOf(id); } // Overload for int compatibility
    public void setOtherUserId(int otherUserId) { this.otherUserId = otherUserId; }
    public void setOtherUsername(String otherUsername) { this.otherUsername = otherUsername; }
    public void setOtherParticipantName(String name) { this.otherUsername = name; } // Alias for compatibility
    public void setToolId(Integer toolId) { this.toolId = toolId; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chat chat = (Chat) o;
        return Objects.equals(id, chat.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Chat{" +
                "id='" + id + '\'' +
                ", otherUserId=" + otherUserId +
                ", otherUsername='" + otherUsername + '\'' +
                ", toolId=" + toolId +
                ", toolName='" + toolName + '\'' +
                ", messageCount=" + (messages != null ? messages.size() : 0) +
                '}';
    }
} 