package com.example.starter.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.starter.databinding.FragmentHomeBinding;
import com.example.starter.models.Tool;

public class HomeFragment extends Fragment implements PropertyAdapter.OnPropertyClickListener {

    private FragmentHomeBinding binding;
    private PropertyAdapter propertyAdapter;
    private HomeViewModel homeViewModel;
    private String selectedCategory = "all";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupRecyclerView();
        setupCategorySelection();
        setupFilterButton();
        observeViewModel();

        return root;
    }

    private void setupRecyclerView() {
        propertyAdapter = new PropertyAdapter(this);
        RecyclerView recyclerView = binding.propertiesRecyclerView;
        
        // Use GridLayoutManager with 2 columns like Airbnb
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(propertyAdapter);
    }

    private void setupCategorySelection() {
        // Set up category listeners
        binding.categoryAll.setOnClickListener(v -> selectCategory("all", binding.categoryAll));
        binding.categoryHouses.setOnClickListener(v -> selectCategory("houses", binding.categoryHouses));
        binding.categoryApartments.setOnClickListener(v -> selectCategory("apartments", binding.categoryApartments));
        binding.categoryCabins.setOnClickListener(v -> selectCategory("cabins", binding.categoryCabins));

        // Set initial selection
        selectCategory("all", binding.categoryAll);
    }

    private void selectCategory(String category, LinearLayout selectedLayout) {
        // Reset all indicators
        binding.indicatorAll.setBackgroundColor(0x00000000); // Transparent
        binding.indicatorHouses.setBackgroundColor(0x00000000);
        binding.indicatorApartments.setBackgroundColor(0x00000000);
        binding.indicatorCabins.setBackgroundColor(0x00000000);

        // Set selected indicator
        View indicator = null;
        switch (category) {
            case "all":
                indicator = binding.indicatorAll;
                break;
            case "houses":
                indicator = binding.indicatorHouses;
                break;
            case "apartments":
                indicator = binding.indicatorApartments;
                break;
            case "cabins":
                indicator = binding.indicatorCabins;
                break;
        }

        if (indicator != null) {
            indicator.setBackgroundColor(0xFF222222); // Dark color
        }

        selectedCategory = category;
        homeViewModel.filterProperties(category);
    }

    private void setupFilterButton() {
        binding.filterButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Filters", Toast.LENGTH_SHORT).show();
            // In a real app, this would open a filter dialog
        });
    }

    private void observeViewModel() {
        homeViewModel.getProperties().observe(getViewLifecycleOwner(), properties -> {
            if (properties != null) {
                propertyAdapter.setProperties(properties);
            }
        });
    }

    @Override
    public void onPropertyClick(Tool property) {
        // Handle property card click - could navigate to detail view
        Toast.makeText(getContext(), "Selected: " + property.getName(), Toast.LENGTH_SHORT).show();
        // In a real app, you might navigate to a detail fragment:
        // Navigation.findNavController(requireView()).navigate(
        //     HomeFragmentDirections.actionToPropertyDetail(property.getId())
        // );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}