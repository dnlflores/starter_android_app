package com.example.starter.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.starter.databinding.FragmentAccountBinding;
import com.example.starter.model.User;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private AccountViewModel accountViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupObservers();
        
        return root;
    }

    private void setupObservers() {
        accountViewModel.getUser().observe(getViewLifecycleOwner(), this::displayUser);
        
        accountViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        accountViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            binding.errorText.setVisibility(error != null ? View.VISIBLE : View.GONE);
            if (error != null) {
                binding.errorText.setText(error);
                // Add click listener to retry
                binding.errorText.setOnClickListener(v -> accountViewModel.retry(1));
            }
        });
    }

    private void displayUser(User user) {
        if (user == null) return;
        
        binding.textAccount.setText("Welcome, " + user.getUsername() + "!");
        binding.userIdText.setText("User ID: " + user.getId());
        binding.usernameText.setText("Username: " + user.getUsername());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 