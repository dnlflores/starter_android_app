package com.example.starter.websocket;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.starter.models.ChatMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketManager {
    private static final String BASE_URL = "wss://starter-ios-app-backend.onrender.com/ws";
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    
    private static WebSocketManager instance;
    private WebSocket webSocket;
    private OkHttpClient client;
    private Gson gson;
    
    // LiveData for connection status
    private MutableLiveData<Boolean> connectionStatus = new MutableLiveData<>(false);
    private MutableLiveData<ChatMessage> newMessage = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    // Connection management
    private boolean isConnecting = false;
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private String authToken;
    
    // Callback interface for message updates
    public interface MessageListener {
        void onNewMessage(ChatMessage message);
        void onMessageUpdated(ChatMessage message);
        void onConnectionStatusChanged(boolean isConnected);
        void onError(String error);
    }
    
    private MessageListener messageListener;

    private WebSocketManager() {
        client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        gson = new Gson();
    }

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    public LiveData<Boolean> getConnectionStatus() {
        return connectionStatus;
    }

    public LiveData<ChatMessage> getNewMessage() {
        return newMessage;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public void connect(String token) {
        if (isConnecting || (webSocket != null && connectionStatus.getValue() == Boolean.TRUE)) {
            return;
        }

        this.authToken = token;
        isConnecting = true;
        
        Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                isConnecting = false;
                reconnectAttempts = 0;
                connectionStatus.postValue(true);
                
                if (messageListener != null) {
                    messageListener.onConnectionStatusChanged(true);
                }
                
                // Send ping to keep connection alive
                sendPing();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JsonObject jsonObject = JsonParser.parseString(text).getAsJsonObject();
                    
                    if (jsonObject.has("type")) {
                        String type = jsonObject.get("type").getAsString();
                        
                        switch (type) {
                            case "newMessage":
                                if (jsonObject.has("message")) {
                                    ChatMessage message = gson.fromJson(
                                        jsonObject.get("message"), 
                                        ChatMessage.class
                                    );
                                    newMessage.postValue(message);
                                    
                                    if (messageListener != null) {
                                        messageListener.onNewMessage(message);
                                    }
                                }
                                break;
                                
                            case "messageEdited":
                                if (jsonObject.has("message")) {
                                    ChatMessage message = gson.fromJson(
                                        jsonObject.get("message"), 
                                        ChatMessage.class
                                    );
                                    
                                    if (messageListener != null) {
                                        messageListener.onMessageUpdated(message);
                                    }
                                }
                                break;
                                
                            case "pong":
                                // Keep-alive response, no action needed
                                break;
                                
                            default:
                                System.out.println("Unknown message type: " + type);
                        }
                    }
                } catch (Exception e) {
                    String error = "Failed to parse WebSocket message: " + e.getMessage();
                    errorMessage.postValue(error);
                    
                    if (messageListener != null) {
                        messageListener.onError(error);
                    }
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                onMessage(webSocket, bytes.utf8());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(NORMAL_CLOSURE_STATUS, null);
                connectionStatus.postValue(false);
                
                if (messageListener != null) {
                    messageListener.onConnectionStatusChanged(false);
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                connectionStatus.postValue(false);
                isConnecting = false;
                
                if (messageListener != null) {
                    messageListener.onConnectionStatusChanged(false);
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                isConnecting = false;
                connectionStatus.postValue(false);
                
                String error = "WebSocket connection failed: " + t.getMessage();
                errorMessage.postValue(error);
                
                if (messageListener != null) {
                    messageListener.onConnectionStatusChanged(false);
                    messageListener.onError(error);
                }
                
                // Attempt to reconnect
                attemptReconnect();
            }
        });
    }

    private void sendPing() {
        if (webSocket != null && connectionStatus.getValue() == Boolean.TRUE) {
            JsonObject pingMessage = new JsonObject();
            pingMessage.addProperty("type", "ping");
            webSocket.send(pingMessage.toString());
        }
    }

    private void attemptReconnect() {
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS && authToken != null) {
            reconnectAttempts++;
            
            // Exponential backoff: 1s, 2s, 4s, 8s, etc., max 30s
            long delay = Math.min(1000 * (1L << (reconnectAttempts - 1)), 30000);
            
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                () -> connect(authToken), 
                delay
            );
        }
    }

    public void sendMessage(String message) {
        if (webSocket != null && connectionStatus.getValue() == Boolean.TRUE) {
            JsonObject messageObj = new JsonObject();
            messageObj.addProperty("type", "message");
            messageObj.addProperty("text", message);
            
            webSocket.send(messageObj.toString());
        }
    }

    public void editMessage(int messageId, String newText) {
        if (webSocket != null && connectionStatus.getValue() == Boolean.TRUE) {
            JsonObject editObj = new JsonObject();
            editObj.addProperty("type", "editMessage");
            editObj.addProperty("messageId", messageId);
            editObj.addProperty("newText", newText);
            
            webSocket.send(editObj.toString());
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(NORMAL_CLOSURE_STATUS, "Closing connection");
            webSocket = null;
        }
        connectionStatus.postValue(false);
        reconnectAttempts = 0;
        authToken = null;
    }
} 