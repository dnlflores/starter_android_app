package com.example.starter.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
        setupClickListeners();
        
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

    private void setupClickListeners() {
        // Profile settings
        binding.editProfileRow.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show());
        
        binding.accountInfoRow.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Account Information clicked", Toast.LENGTH_SHORT).show());

        // Preferences
        binding.notificationsRow.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Notifications clicked", Toast.LENGTH_SHORT).show());
        
        binding.privacyRow.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Privacy & Security clicked", Toast.LENGTH_SHORT).show());

        // Support
        binding.helpCenterRow.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Help Center clicked", Toast.LENGTH_SHORT).show());
        
        binding.logoutRow.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Log Out clicked", Toast.LENGTH_SHORT).show());
    }

    private void displayUser(User user) {
        if (user == null) return;
        
        binding.textAccount.setText("Welcome back!");
        binding.usernameText.setText(user.getUsername());
        binding.userIdText.setText("ID: " + user.getId());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 