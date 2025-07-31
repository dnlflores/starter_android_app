package com.example.starter.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.example.starter.R;
import com.example.starter.databinding.FragmentChatBinding;
import com.example.starter.ui.auth.AuthAwareFragment;
import com.example.starter.ui.auth.AuthSplashFragment;

public class ChatFragment extends AuthAwareFragment {

    private FragmentChatBinding binding;
    private ChatViewModel chatViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected void showAuthenticatedContent() {
        if (binding != null) {
            final TextView textView = binding.textChat;
            chatViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        }
    }
    
    @Override
    protected AuthSplashFragment.TabType getTabType() {
        return AuthSplashFragment.TabType.CHAT;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 