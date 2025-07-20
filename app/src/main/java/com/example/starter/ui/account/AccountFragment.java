package com.example.starter.ui.account;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.starter.LoginActivity;
import com.example.starter.databinding.FragmentAccountBinding;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private AccountViewModel accountViewModel;
    private SharedPreferences prefs;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        // Initialize SharedPreferences
        prefs = requireActivity().getSharedPreferences("StarterPrefs", Context.MODE_PRIVATE);
        
        // Initialize ViewModel
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        
        // Check authentication status
        checkAuthenticationStatus();
        
        // Setup UI
        setupObservers();
        setupButtons();
        
        return root;
    }
    
    private void checkAuthenticationStatus() {
        String authToken = prefs.getString("authToken", "");
        String username = prefs.getString("username", "");
        
        if (authToken.isEmpty()) {
            showLoginPrompt();
        } else {
            showAccountInfo();
            // Note: AccountViewModel API needs to be updated for proper NetworkManager usage
            displayUserInfo(username);
        }
    }
    
    private void showLoginPrompt() {
        // Basic implementation for now - these UI elements need to be added to fragment_account.xml
        // binding.loginPromptCard.setVisibility(View.VISIBLE);
        // binding.accountContentLayout.setVisibility(View.GONE);
    }
    
    private void showAccountInfo() {
        // Basic implementation for now - these UI elements need to be added to fragment_account.xml
        // binding.loginPromptCard.setVisibility(View.GONE);
        // binding.accountContentLayout.setVisibility(View.VISIBLE);
    }
    
    private void displayUserInfo(String username) {
        // Basic implementation for now - these UI elements need to be added to fragment_account.xml
        // binding.textUsername.setText(username);
        // binding.textUserInitial.setText(username.isEmpty() ? "U" : username.substring(0, 1).toUpperCase());
    }
    
    private void setupObservers() {
        // TODO: Implement when AccountViewModel is fixed
    }
    
    private void setupButtons() {
        // TODO: Implement when UI elements are added to fragment_account.xml
    }
    
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // TODO: Implement when AccountViewModel is fixed
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void clearUserData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
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