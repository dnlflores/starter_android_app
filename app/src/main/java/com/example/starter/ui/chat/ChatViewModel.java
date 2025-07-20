package com.example.starter.ui.chat;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.starter.models.Chat;
import com.example.starter.models.ChatAPIMessage;
import com.example.starter.models.ChatMessage;
import com.example.starter.network.NetworkManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatViewModel extends AndroidViewModel {

    private final NetworkManager networkManager;
    
    private MutableLiveData<List<Chat>> chats = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ChatViewModel(Application application) {
        super(application);
        this.networkManager = NetworkManager.getInstance(application);
    }

    // Getters for LiveData
    public LiveData<List<Chat>> getChats() { return chats; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadChats() {
        isLoading.setValue(true);
        
        networkManager.fetchChats(new NetworkManager.NetworkCallback<List<ChatAPIMessage>>() {
            @Override
            public void onSuccess(List<ChatAPIMessage> result) {
                isLoading.setValue(false);
                
                // Group messages into conversations
                List<Chat> chatList = groupMessagesIntoChats(result);
                chats.setValue(chatList);
                errorMessage.setValue(null);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to load chats: " + error);
                chats.setValue(new ArrayList<>());
            }
        });
    }

    private List<Chat> groupMessagesIntoChats(List<ChatAPIMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }

        int currentUserId = networkManager.getUserId();
        Map<String, Chat> chatMap = new HashMap<>();
        
        for (ChatAPIMessage message : messages) {
            // Create a unique chat ID based on tool only
            String chatId;
            if (message.getToolId() != null) {
                chatId = "tool_" + message.getToolId(); // Group by tool
            } else {
                // For general conversations without a tool, group by other participant
                int otherUserId = (message.getSenderId() == currentUserId) 
                    ? message.getRecipientId() 
                    : message.getSenderId();
                chatId = "user_" + otherUserId;
            }
            
            Chat chat = chatMap.get(chatId);
            if (chat == null) {
                // Create new chat conversation
                chat = new Chat();
                chat.setId(chatId);
                
                if (message.getToolId() != null) {
                    // Tool-based conversation
                    chat.setToolId(message.getToolId());
                    chat.setToolName("Tool " + message.getToolId()); // Will be updated when we fetch tool details
                    chat.setOtherUserId(0); // Not applicable for tool conversations
                    chat.setOtherUsername("Multiple users"); // Tool conversations can have multiple participants
                } else {
                    // User-to-user conversation
                    int otherUserId = (message.getSenderId() == currentUserId) 
                        ? message.getRecipientId() 
                        : message.getSenderId();
                    chat.setOtherUserId(otherUserId);
                    chat.setOtherUsername("User " + otherUserId);
                    chat.setToolId(null);
                    chat.setToolName(null);
                }
                chatMap.put(chatId, chat);
            }
            
            // Convert ChatAPIMessage to ChatMessage and add to conversation
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setId(message.getId());
            chatMessage.setText(message.getMessage());
            chatMessage.setSenderId(message.getSenderId());
            chatMessage.setTimestamp(message.getCreatedAt() != null ? parseDate(message.getCreatedAt()) : new Date());
            chatMessage.setToolId(message.getToolId());
            chatMessage.setEdited(message.isEdited());
            
            chat.appendMessage(chatMessage);
            
            // Update chat metadata based on latest message
            if (chat.getLatestMessage() == null || 
                chatMessage.getTimestamp().after(chat.getLatestMessage().getTimestamp())) {
                chat.setLastMessage(message.getMessage());
                chat.setTimestamp(chatMessage.getTimestamp());
            }
        }
        
        // Convert map values to list and sort by latest message timestamp
        List<Chat> chatList = new ArrayList<>(chatMap.values());
        Collections.sort(chatList, new Comparator<Chat>() {
            @Override
            public int compare(Chat c1, Chat c2) {
                ChatMessage m1 = c1.getLatestMessage();
                ChatMessage m2 = c2.getLatestMessage();
                if (m1 == null && m2 == null) return 0;
                if (m1 == null) return 1;
                if (m2 == null) return -1;
                return m2.getTimestamp().compareTo(m1.getTimestamp()); // Newest first
            }
        });
        
        return chatList;
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

    public void sendQuickMessage(int recipientId, String messageText) {
        networkManager.sendMessage(recipientId, messageText, null, null, 
                new NetworkManager.NetworkCallback<ChatAPIMessage>() {
            @Override
            public void onSuccess(ChatAPIMessage result) {
                // Refresh chats to show the new message
                loadChats();
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue("Failed to send message: " + error);
            }
        });
    }
} 