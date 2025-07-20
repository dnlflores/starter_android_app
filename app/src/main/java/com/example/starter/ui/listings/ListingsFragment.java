package com.example.starter.ui.listings;

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

import com.example.starter.LoginActivity;
import com.example.starter.databinding.FragmentListingsBinding;
import com.example.starter.ui.welcome.ToolsAdapter;

public class ListingsFragment extends Fragment {

    private FragmentListingsBinding binding;
    private ListingsViewModel listingsViewModel;
    private ToolsAdapter toolsAdapter;
    private SharedPreferences prefs;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        
        binding = FragmentListingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        // Initialize SharedPreferences
        prefs = requireActivity().getSharedPreferences("StarterPrefs", Context.MODE_PRIVATE);
        
        // Initialize ViewModel
        listingsViewModel = new ViewModelProvider(this).get(ListingsViewModel.class);
        
        // Check authentication
        checkAuthenticationStatus();
        
        // Setup UI
        setupRecyclerView();
        setupObservers();
        setupButtons();
        
        return root;
    }
    
    private void checkAuthenticationStatus() {
        String authToken = prefs.getString("authToken", "");
        
        if (authToken.isEmpty()) {
            showLoginPrompt();
        } else {
            showListings();
            // No longer need to set auth token - NetworkManager handles this internally
            listingsViewModel.loadUserTools();
        }
    }
    
    private void showLoginPrompt() {
        binding.loginPromptCard.setVisibility(View.VISIBLE);
        binding.listingsContentLayout.setVisibility(View.GONE);
    }
    
    private void showListings() {
        binding.loginPromptCard.setVisibility(View.GONE);
        binding.listingsContentLayout.setVisibility(View.VISIBLE);
    }
    
    private void setupRecyclerView() {
        toolsAdapter = new ToolsAdapter(tool -> {
            // Handle tool click - could navigate to tool detail or edit
            // For now, just show a toast
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(), 
                    "Clicked: " + tool.getName(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        binding.recyclerViewTools.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewTools.setAdapter(toolsAdapter);
    }
    
    private void setupObservers() {
        listingsViewModel.getTools().observe(getViewLifecycleOwner(), tools -> {
            toolsAdapter.setTools(tools);
            
            if (tools.isEmpty()) {
                binding.emptyStateLayout.setVisibility(View.VISIBLE);
                binding.recyclerViewTools.setVisibility(View.GONE);
            } else {
                binding.emptyStateLayout.setVisibility(View.GONE);
                binding.recyclerViewTools.setVisibility(View.VISIBLE);
            }
        });
        
        listingsViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefreshLayout.setRefreshing(isLoading);
        });
    }
    
    private void setupButtons() {
        binding.loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });
        
        binding.signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.putExtra("showSignUp", true);
            startActivity(intent);
        });
        
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            listingsViewModel.loadUserTools();
        });
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