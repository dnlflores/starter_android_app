package com.example.starter.ui.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.starter.databinding.ActivityChatDetailBinding;
import com.example.starter.models.ChatMessage;
import com.example.starter.network.NetworkManager;
import com.example.starter.websocket.WebSocketManager;

import java.util.List;

public class ChatDetailActivity extends AppCompatActivity implements WebSocketManager.MessageListener {

    private ActivityChatDetailBinding binding;
    private ChatDetailViewModel viewModel;
    private ChatMessageAdapter messageAdapter;
    private SharedPreferences prefs;
    private NetworkManager networkManager;
    
    private String chatId;
    private String chatTitle;
    private int otherUserId;
    private Integer toolId;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize NetworkManager and SharedPreferences
        networkManager = NetworkManager.getInstance(this);
        prefs = getSharedPreferences("StarterPrefs", Context.MODE_PRIVATE);
        currentUserId = networkManager.getUserId();
        
        // Get intent extras
        chatId = getIntent().getStringExtra("chatId");
        chatTitle = getIntent().getStringExtra("chatTitle");
        otherUserId = getIntent().getIntExtra("otherUserId", -1);
        toolId = getIntent().hasExtra("toolId") ? getIntent().getIntExtra("toolId", -1) : null;
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ChatDetailViewModel.class);
        
        // Setup UI
        setupToolbar();
        setupRecyclerView();
        setupInputField();
        setupObservers();
        
        // Setup WebSocket listener
        WebSocketManager.getInstance().setMessageListener(this);
        
        // Load messages for this specific conversation
        if (chatId != null && (toolId != null || otherUserId > 0)) {
            viewModel.loadMessagesForConversation(otherUserId, toolId);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(chatTitle != null ? chatTitle : "Chat");
        }
        
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        messageAdapter = new ChatMessageAdapter(String.valueOf(currentUserId), this::onEditMessage);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom
        
        binding.recyclerViewMessages.setLayoutManager(layoutManager);
        binding.recyclerViewMessages.setAdapter(messageAdapter);
    }

    private void setupInputField() {
        // Enable/disable send button based on text input
        binding.editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.buttonSend.setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Send message on button click
        binding.buttonSend.setOnClickListener(v -> sendMessage());
        binding.buttonSend.setEnabled(false);
    }

    private void setupObservers() {
        viewModel.getMessages().observe(this, messages -> {
            messageAdapter.setMessages(messages);
            
            // Scroll to bottom when new messages arrive
            if (!messages.isEmpty()) {
                binding.recyclerViewMessages.smoothScrollToPosition(messages.size() - 1);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            // Could show loading indicator here
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = binding.editTextMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            int recipientId = otherUserId;
            
            // For tool conversations (otherUserId = 0), find a recent participant to send to
            if (toolId != null && otherUserId <= 0) {
                // Get recent messages to find other participants
                List<ChatMessage> currentMessages = viewModel.getMessages().getValue();
                if (currentMessages != null && !currentMessages.isEmpty()) {
                    // Find the most recent message from someone else
                    int currentUserId = networkManager.getUserId();
                    for (int i = currentMessages.size() - 1; i >= 0; i--) {
                        ChatMessage msg = currentMessages.get(i);
                        if (msg.getSenderId() != currentUserId) {
                            recipientId = msg.getSenderId();
                            break;
                        }
                    }
                }
                
                // If we still don't have a recipient, default to 1 (first user)
                if (recipientId <= 0) {
                    recipientId = 1;
                }
            }
            
            if (recipientId > 0) {
                viewModel.sendMessage(recipientId, messageText, toolId);
                binding.editTextMessage.setText("");
            }
        }
    }

    private void onEditMessage(int messageId, String newText) {
        viewModel.editMessage(messageId, newText);
    }

    // WebSocket MessageListener implementation
    @Override
    public void onNewMessage(ChatMessage message) {
        runOnUiThread(() -> {
            // Only add message if it belongs to this chat
            if (isMessageForThisChat(message)) {
                viewModel.addMessage(message);
            }
        });
    }

    @Override
    public void onMessageUpdated(ChatMessage message) {
        runOnUiThread(() -> {
            if (isMessageForThisChat(message)) {
                viewModel.updateMessage(message);
            }
        });
    }

    @Override
    public void onConnectionStatusChanged(boolean isConnected) {
        runOnUiThread(() -> {
            // Could update UI to show connection status
            if (!isConnected) {
                Toast.makeText(this, "Connection lost, trying to reconnect...", 
                             Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    private boolean isMessageForThisChat(ChatMessage message) {
        // Check if message belongs to this chat based on participants and tool
        return (message.getSenderId() == currentUserId || message.getSenderId() == otherUserId) &&
               ((toolId == null && message.getToolId() == null) || 
                (toolId != null && toolId.equals(message.getToolId())));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove WebSocket listener to prevent memory leaks
        WebSocketManager.getInstance().setMessageListener(null);
    }
} 