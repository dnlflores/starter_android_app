package com.example.starter.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.starter.LoginActivity;
import com.example.starter.R;
import com.example.starter.databinding.FragmentChatBinding;
import com.example.starter.models.Chat;
import com.example.starter.ui.chat.ChatAdapter;
import com.example.starter.ui.chat.ChatDetailActivity;
import com.example.starter.ui.chat.ChatViewModel;

public class ChatFragment extends Fragment implements ChatAdapter.OnChatClickListener {

    private FragmentChatBinding binding;
    private ChatViewModel chatViewModel;
    private ChatAdapter chatAdapter;
    private SharedPreferences prefs;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        
        binding = FragmentChatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        // Initialize SharedPreferences
        prefs = requireActivity().getSharedPreferences("StarterPrefs", Context.MODE_PRIVATE);
        
        // Initialize ViewModel
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        
        // Check authentication status
        checkAuthenticationStatus();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup observers
        setupObservers();
        
        // Setup refresh functionality
        setupSwipeRefresh();
        
        // Setup login button
        binding.loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });
        
        // Setup signup button
        binding.signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.putExtra("showSignUp", true);
            startActivity(intent);
        });
        
        // Setup refresh button in empty state
        binding.refreshButton.setOnClickListener(v -> {
            chatViewModel.loadChats();
        });
        
        return root;
    }
    
    private void checkAuthenticationStatus() {
        String authToken = prefs.getString("authToken", "");
        String username = prefs.getString("username", "");
        
        if (authToken.isEmpty()) {
            showLoginPrompt();
        } else {
            showChatList();
            // No longer need to set auth token and username - NetworkManager handles this internally
            chatViewModel.loadChats();
        }
    }
    
    private void showLoginPrompt() {
        binding.loginPromptCard.setVisibility(View.VISIBLE);
        binding.chatContentLayout.setVisibility(View.GONE);
    }
    
    private void showChatList() {
        binding.loginPromptCard.setVisibility(View.GONE);
        binding.chatContentLayout.setVisibility(View.VISIBLE);
    }
    
    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(this);
        binding.recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewChats.setAdapter(chatAdapter);
    }
    
    private void setupObservers() {
        chatViewModel.getChats().observe(getViewLifecycleOwner(), chats -> {
            chatAdapter.setChats(chats);
            
            if (chats.isEmpty()) {
                binding.emptyStateLayout.setVisibility(View.VISIBLE);
                binding.recyclerViewChats.setVisibility(View.GONE);
            } else {
                binding.emptyStateLayout.setVisibility(View.GONE);
                binding.recyclerViewChats.setVisibility(View.VISIBLE);
            }
        });
        
        chatViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefreshLayout.setRefreshing(isLoading);
        });
        
        // Connection status observation removed as it's no longer available
        /*chatViewModel.getConnectionStatus().observe(getViewLifecycleOwner(), isConnected -> {
            if (isConnected) {
                binding.connectionStatusLayout.setVisibility(View.GONE);
            } else {
                binding.connectionStatusLayout.setVisibility(View.VISIBLE);
            }
        });*/
    }
    
    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            chatViewModel.loadChats();
        });
        
        // Set refresh colors to match theme
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.gradient_start,
            R.color.gradient_end
        );
    }
    
    @Override
    public void onChatClick(Chat chat) {
        Intent intent = new Intent(getActivity(), ChatDetailActivity.class);
        intent.putExtra("chatId", chat.getId());
        intent.putExtra("chatTitle", chat.getChatTitle());
        intent.putExtra("otherUserId", chat.getOtherUserId());
        intent.putExtra("toolId", chat.getToolId());
        startActivity(intent);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        checkAuthenticationStatus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}