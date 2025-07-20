package com.example.starter.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.starter.R;
import com.example.starter.models.ChatMessage;
import com.example.starter.network.NetworkManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    private List<ChatMessage> messages = new ArrayList<>();
    private NetworkManager networkManager;
    private OnEditMessageListener editMessageListener;

    public interface OnEditMessageListener {
        void onEditMessage(int messageId, String newText);
    }

    public ChatMessageAdapter(String currentUserId, OnEditMessageListener editMessageListener) {
        this.editMessageListener = editMessageListener;
        // We'll get NetworkManager instance to determine current user ID
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message_bubble, parent, false);
        
        // Initialize NetworkManager if not already done
        if (networkManager == null) {
            networkManager = NetworkManager.getInstance(parent.getContext());
        }
        
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.bind(message, networkManager.getUserId());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addMessage(ChatMessage message) {
        if (message != null) {
            this.messages.add(message);
            notifyItemInserted(messages.size() - 1);
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout sentMessageLayout;
        private LinearLayout receivedMessageLayout;
        private TextView sentMessageText;
        private TextView receivedMessageText;
        private TextView sentTimestamp;
        private TextView receivedTimestamp;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sentMessageLayout = itemView.findViewById(R.id.layout_sent_message);
            receivedMessageLayout = itemView.findViewById(R.id.layout_received_message);
            sentMessageText = itemView.findViewById(R.id.text_sent_message);
            receivedMessageText = itemView.findViewById(R.id.text_received_message);
            sentTimestamp = itemView.findViewById(R.id.text_sent_timestamp);
            receivedTimestamp = itemView.findViewById(R.id.text_received_timestamp);
        }

        void bind(ChatMessage message, int currentUserId) {
            boolean isSentMessage = message.getSenderId() == currentUserId;
            
            if (isSentMessage) {
                // Show sent message layout
                sentMessageLayout.setVisibility(View.VISIBLE);
                receivedMessageLayout.setVisibility(View.GONE);
                
                sentMessageText.setText(message.getText());
                sentTimestamp.setText(formatTimestamp(message.getTimestamp()));
            } else {
                // Show received message layout
                sentMessageLayout.setVisibility(View.GONE);
                receivedMessageLayout.setVisibility(View.VISIBLE);
                
                receivedMessageText.setText(message.getText());
                receivedTimestamp.setText(formatTimestamp(message.getTimestamp()));
            }
        }
        
        private String formatTimestamp(Date date) {
            if (date == null) return "";
            
            Calendar calendar = Calendar.getInstance();
            Calendar messageCalendar = Calendar.getInstance();
            messageCalendar.setTime(date);
            
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            
            // Check if today
            if (isSameDay(calendar, messageCalendar)) {
                return timeFormat.format(date);
            }
            
            // Check if yesterday
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            if (isSameDay(calendar, messageCalendar)) {
                return "Yesterday " + timeFormat.format(date);
            }
            
            // Different day
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
            return dateFormat.format(date);
        }
        
        private boolean isSameDay(Calendar cal1, Calendar cal2) {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                   cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        }
    }
} 