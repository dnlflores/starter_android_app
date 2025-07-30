package com.example.starter.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.starter.R;
import com.example.starter.databinding.FragmentHomeBinding;
import com.example.starter.model.Tool;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ToolsAdapter toolsAdapter;
    private boolean isListView = true;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set up RecyclerView
        setupRecyclerView();

        // Set up button click listeners
        setupButtonListeners();

        // Observe data from ViewModel
        homeViewModel.getTools().observe(getViewLifecycleOwner(), tools -> {
            toolsAdapter.setTools(tools);
            binding.recyclerViewTools.setVisibility(tools != null && !tools.isEmpty() ? View.VISIBLE : View.GONE);
        });

        homeViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        homeViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            binding.errorText.setVisibility(error != null ? View.VISIBLE : View.GONE);
            if (error != null) {
                binding.errorText.setText(error);
                // Add click listener to retry
                binding.errorText.setOnClickListener(v -> homeViewModel.retry());
            }
        });

        return root;
    }

    private void setupRecyclerView() {
        toolsAdapter = new ToolsAdapter();
        toolsAdapter.setOnToolClickListener(this::onToolClick);
        binding.recyclerViewTools.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewTools.setAdapter(toolsAdapter);
    }

    private void setupButtonListeners() {
        // Filters button
        binding.buttonFilters.setOnClickListener(v -> {
            // TODO: Implement filters functionality
            // For now, just show a toast or navigate to filters screen
        });

        // List/Map toggle
        binding.buttonList.setOnClickListener(v -> {
            if (!isListView) {
                setListViewActive();
            }
        });

        binding.buttonMap.setOnClickListener(v -> {
            if (isListView) {
                setMapViewActive();
            }
        });
    }

    private void setListViewActive() {
        isListView = true;
        binding.buttonList.setBackgroundResource(R.drawable.button_toggle_active_background);
        binding.buttonList.setTextColor(getResources().getColor(android.R.color.black));
        binding.buttonMap.setBackgroundResource(android.R.color.transparent);
        binding.buttonMap.setTextColor(getResources().getColor(android.R.color.white));
        // TODO: Switch to list view
    }

    private void setMapViewActive() {
        isListView = false;
        binding.buttonMap.setBackgroundResource(R.drawable.button_toggle_active_background);
        binding.buttonMap.setTextColor(getResources().getColor(android.R.color.black));
        binding.buttonList.setBackgroundResource(android.R.color.transparent);
        binding.buttonList.setTextColor(getResources().getColor(android.R.color.white));
        // TODO: Switch to map view
    }
    
    private void onToolClick(Tool tool) {
        // Navigate to tool detail fragment
        Bundle bundle = new Bundle();
        bundle.putInt("tool_id", tool.getId());
        Navigation.findNavController(requireView()).navigate(R.id.action_home_to_tool_detail, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}