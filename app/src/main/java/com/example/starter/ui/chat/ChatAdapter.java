package com.example.starter.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.starter.R;
import com.example.starter.models.Chat;
import com.example.starter.models.ChatMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    
    private List<Chat> chats = new ArrayList<>();
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public ChatAdapter(OnChatClickListener listener) {
        this.listener = listener;
    }

    public void setChats(List<Chat> chats) {
        this.chats = chats;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_row, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chats.get(position);
        holder.bind(chat, listener);
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView messageTextView;
        private TextView timestampTextView;
        private TextView subtitleTextView;
        private ImageView avatarImageView;
        private View cardView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_chat_name);
            messageTextView = itemView.findViewById(R.id.text_last_message);
            timestampTextView = itemView.findViewById(R.id.text_timestamp);
            subtitleTextView = itemView.findViewById(R.id.text_subtitle);
            avatarImageView = itemView.findViewById(R.id.image_avatar);
            cardView = itemView.findViewById(R.id.chat_card);
        }

        public void bind(Chat chat, OnChatClickListener listener) {
            nameTextView.setText(chat.getDisplayName());
            subtitleTextView.setText(chat.getDisplaySubtitle());
            
            // Get last message
            String lastMessageText = "No messages yet";
            String timestamp = "";
            
            ChatMessage lastMessage = chat.getLatestMessage();
            if (lastMessage != null) {
                lastMessageText = lastMessage.getText();
                timestamp = formatTimestamp(lastMessage.getTimestamp());
            }
            
            messageTextView.setText(lastMessageText);
            timestampTextView.setText(timestamp);
            
            // Set avatar placeholder (could be enhanced with actual user avatars)
            avatarImageView.setImageResource(R.drawable.ic_person);
            
            // Set click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChatClick(chat);
                }
            });
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
                return "Yesterday";
            }
            
            // Check if this year
            calendar = Calendar.getInstance();
            if (calendar.get(Calendar.YEAR) == messageCalendar.get(Calendar.YEAR)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                return dateFormat.format(date);
            }
            
            // Different year
            SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yy", Locale.getDefault());
            return dateFormat.format(date);
        }
        
        private boolean isSameDay(Calendar cal1, Calendar cal2) {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                   cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        }
    }
} 