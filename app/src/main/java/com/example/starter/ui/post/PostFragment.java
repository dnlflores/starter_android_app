package com.example.starter.ui.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.example.starter.R;
import com.example.starter.databinding.FragmentPostBinding;
import com.example.starter.ui.auth.AuthAwareFragment;
import com.example.starter.ui.auth.AuthSplashFragment;

public class PostFragment extends AuthAwareFragment {

    private FragmentPostBinding binding;
    private PostViewModel postViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);
        binding = FragmentPostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected void showAuthenticatedContent() {
        if (binding != null) {
            final TextView textView = binding.textPost;
            postViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        }
    }
    
    @Override
    protected AuthSplashFragment.TabType getTabType() {
        return AuthSplashFragment.TabType.POST;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 