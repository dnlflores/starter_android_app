package com.example.starter.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.starter.R;
import com.example.starter.model.User;

public class UserProfileFragment extends Fragment {
    
    private static final String ARG_USER_ID = "user_id";
    
    private UserProfileViewModel viewModel;
    private TextView userName;
    private TextView userId;
    private ProgressBar progressBar;
    private TextView errorText;
    
    private int mUserId;

    public static UserProfileFragment newInstance(int userId) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserId = getArguments().getInt(ARG_USER_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user_profile, container, false);
        
        initViews(root);
        setupViewModel();
        observeViewModel();
        
        // Load user details
        viewModel.loadUser(mUserId);
        
        return root;
    }

    private void initViews(View root) {
        userName = root.findViewById(R.id.user_name);
        userId = root.findViewById(R.id.user_id);
        progressBar = root.findViewById(R.id.progress_bar);
        errorText = root.findViewById(R.id.error_text);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
    }

    private void observeViewModel() {
        viewModel.getUser().observe(getViewLifecycleOwner(), this::displayUser);
        
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            errorText.setVisibility(error != null ? View.VISIBLE : View.GONE);
            if (error != null) {
                errorText.setText(error);
                // Add click listener to retry
                errorText.setOnClickListener(v -> viewModel.retry(mUserId));
            }
        });
    }

    private void displayUser(User user) {
        if (user == null) return;
        
        userName.setText(user.getUsername());
        userId.setText("User ID: " + user.getId());
    }
} 