package com.example.starter.ui.chat;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.starter.models.ChatAPIMessage;
import com.example.starter.models.ChatMessage;
import com.example.starter.network.NetworkManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatDetailViewModel extends AndroidViewModel {
    private final NetworkManager networkManager;
    
    private MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ChatDetailViewModel(Application application) {
        super(application);
        this.networkManager = NetworkManager.getInstance(application);
    }

    // Getters for LiveData
    public LiveData<List<ChatMessage>> getMessages() { return messages; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadMessages() {
        isLoading.setValue(true);
        
        // Using fetchChats() as a substitute for loading chat messages
        networkManager.fetchChats(new NetworkManager.NetworkCallback<List<ChatAPIMessage>>() {
            @Override
            public void onSuccess(List<ChatAPIMessage> result) {
                isLoading.setValue(false);
                // Convert ChatAPIMessage to ChatMessage for UI compatibility
                List<ChatMessage> chatMessages = new ArrayList<>();
                if (result != null) {
                    for (ChatAPIMessage apiMessage : result) {
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.setId(apiMessage.getId());
                        chatMessage.setText(apiMessage.getMessage());
                        chatMessage.setSenderId(apiMessage.getSenderId());
                        chatMessage.setTimestamp(apiMessage.getCreatedAt() != null ? parseDate(apiMessage.getCreatedAt()) : new Date());
                        chatMessage.setToolId(apiMessage.getToolId());
                        chatMessage.setEdited(apiMessage.isEdited());
                        chatMessages.add(chatMessage);
                    }
                }
                messages.setValue(chatMessages);
                errorMessage.setValue(null);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to load messages: " + error);
                messages.setValue(new ArrayList<>());
            }
        });
    }

    public void loadMessagesForConversation(int otherUserId, Integer toolId) {
        isLoading.setValue(true);
        
        networkManager.fetchChats(new NetworkManager.NetworkCallback<List<ChatAPIMessage>>() {
            @Override
            public void onSuccess(List<ChatAPIMessage> result) {
                isLoading.setValue(false);
                
                // Filter messages for this specific conversation
                List<ChatMessage> chatMessages = filterMessagesForConversation(result, otherUserId, toolId);
                messages.setValue(chatMessages);
                errorMessage.setValue(null);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to load messages: " + error);
                messages.setValue(new ArrayList<>());
            }
        });
    }

    private List<ChatMessage> filterMessagesForConversation(List<ChatAPIMessage> allMessages, int otherUserId, Integer toolId) {
        List<ChatMessage> conversationMessages = new ArrayList<>();
        int currentUserId = networkManager.getUserId();
        
        if (allMessages != null) {
            for (ChatAPIMessage apiMessage : allMessages) {
                boolean isConversationMessage = false;
                
                if (toolId != null) {
                    // Tool-based conversation: include all messages for this tool
                    if (toolId.equals(apiMessage.getToolId())) {
                        isConversationMessage = true;
                    }
                } else {
                    // User-to-user conversation: include messages between current user and other user
                    if ((apiMessage.getSenderId() == currentUserId && apiMessage.getRecipientId() == otherUserId) ||
                        (apiMessage.getSenderId() == otherUserId && apiMessage.getRecipientId() == currentUserId)) {
                        
                        // Also check that tool ID is null (general conversation)
                        if (apiMessage.getToolId() == null) {
                            isConversationMessage = true;
                        }
                    }
                }
                
                if (isConversationMessage) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setId(apiMessage.getId());
                    chatMessage.setText(apiMessage.getMessage());
                    chatMessage.setSenderId(apiMessage.getSenderId());
                    chatMessage.setTimestamp(apiMessage.getCreatedAt() != null ? parseDate(apiMessage.getCreatedAt()) : new Date());
                    chatMessage.setToolId(apiMessage.getToolId());
                    chatMessage.setEdited(apiMessage.isEdited());
                    conversationMessages.add(chatMessage);
                }
            }
        }
        
        // Sort messages by timestamp (oldest first for chat view)
        Collections.sort(conversationMessages, new Comparator<ChatMessage>() {
            @Override
            public int compare(ChatMessage m1, ChatMessage m2) {
                return m1.getTimestamp().compareTo(m2.getTimestamp());
            }
        });
        
        return conversationMessages;
    }
    
    private Date parseDate(String dateString) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            return format.parse(dateString);
        } catch (Exception e) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                return format.parse(dateString);
            } catch (Exception e2) {
                return new Date(); // Fallback to current time
            }
        }
    }

    public void sendMessage(int recipientId, String messageText, Integer toolId) {
        networkManager.sendMessage(recipientId, messageText, toolId, null, 
                new NetworkManager.NetworkCallback<ChatAPIMessage>() {
            @Override
            public void onSuccess(ChatAPIMessage result) {
                if (result != null) {
                    // Convert to ChatMessage and add to list
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setId(result.getId());
                    chatMessage.setText(result.getMessage());
                    chatMessage.setSenderId(result.getSenderId());
                    chatMessage.setTimestamp(result.getTimestamp());
                    addMessage(chatMessage);
                }
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue("Failed to send message: " + error);
            }
        });
    }

    public void editMessage(int messageId, String newText) {
        networkManager.editMessage(messageId, newText, new NetworkManager.NetworkCallback<ChatAPIMessage>() {
            @Override
            public void onSuccess(ChatAPIMessage result) {
                if (result != null) {
                    // Convert to ChatMessage and update in list
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setId(result.getId());
                    chatMessage.setText(result.getMessage());
                    chatMessage.setSenderId(result.getSenderId());
                    chatMessage.setTimestamp(result.getTimestamp());
                    updateMessage(chatMessage);
                }
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue("Failed to edit message: " + error);
            }
        });
    }

    public void addMessage(ChatMessage message) {
        List<ChatMessage> currentMessages = messages.getValue();
        if (currentMessages != null) {
            List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
            
            // Check if message already exists (avoid duplicates)
            boolean exists = false;
            for (ChatMessage existing : updatedMessages) {
                if (existing.getId() == message.getId()) {
                    exists = true;
                    break;
                }
            }
            
            if (!exists) {
                updatedMessages.add(message);
                messages.setValue(updatedMessages);
            }
        }
    }

    public void updateMessage(ChatMessage updatedMessage) {
        List<ChatMessage> currentMessages = messages.getValue();
        if (currentMessages != null) {
            List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
            
            for (int i = 0; i < updatedMessages.size(); i++) {
                if (updatedMessages.get(i).getId() == updatedMessage.getId()) {
                    updatedMessages.set(i, updatedMessage);
                    break;
                }
            }
            
            messages.setValue(updatedMessages);
        }
    }
} 