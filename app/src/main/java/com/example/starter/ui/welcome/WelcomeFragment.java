package com.example.starter.ui.welcome;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.starter.R;
import com.example.starter.models.Tool;
import com.example.starter.network.NetworkManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WelcomeFragment extends Fragment implements ToolsAdapter.OnToolClickListener {
    
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    
    // UI Components
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private androidx.cardview.widget.CardView filterButton;
    private ChipGroup viewModeChips;
    private Chip listChip;
    private Chip mapChip;
    private TextView headerSubtitle;
    
    // Data and Logic
    private WelcomeViewModel viewModel;
    private NetworkManager networkManager;
    private ToolsAdapter toolsAdapter;
    private List<Tool> allTools = new ArrayList<>();
    private List<Tool> filteredTools = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    private Location userLocation;
    
    // Filter states
    private double minPrice = 10.0;
    private double maxPrice = 10000.0;
    private DistanceRange selectedDistanceRange = DistanceRange.ALL;
    private int selectedViewMode = 0; // 0 = list, 1 = map
    
    public enum DistanceRange {
        TEN(10, "10 miles"),
        TWENTY_FIVE(25, "25 miles"),
        FIFTY(50, "50 miles"),
        HUNDRED(100, "100 miles"),
        ALL(-1, "All");
        
        public final int miles;
        public final String title;
        
        DistanceRange(int miles, String title) {
            this.miles = miles;
            this.title = title;
        }
        
        public double getMaxDistanceMeters() {
            return miles > 0 ? miles * 1609.34 : Double.MAX_VALUE; // Convert miles to meters
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupLocationServices();
        setupClickListeners();
        
        loadTools();
    }

    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        recyclerView = view.findViewById(R.id.recycler_tools);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyTextView = view.findViewById(R.id.empty_text);
        filterButton = view.findViewById(R.id.btn_filter);
        viewModeChips = view.findViewById(R.id.chip_group_view_mode);
        listChip = view.findViewById(R.id.chip_list);
        mapChip = view.findViewById(R.id.chip_map);
        headerSubtitle = view.findViewById(R.id.header_subtitle);
        
        // Update header subtitle for filter status
        updateHeaderSubtitle();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(WelcomeViewModel.class);
        networkManager = NetworkManager.getInstance(requireContext());
    }

    private void setupRecyclerView() {
        toolsAdapter = new ToolsAdapter(this);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(toolsAdapter);
    }

    private void setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        requestLocationPermission();
    }

    private void setupClickListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::loadTools);
        
        filterButton.setOnClickListener(v -> showFilterDialog());
        
        viewModeChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // Handle tool category filtering
            if (checkedIds.contains(R.id.chip_all)) {
                // Show all tools - no additional filtering needed
                applyFiltersAndSort();
            } else if (checkedIds.contains(R.id.chip_power_tools)) {
                // Filter for power tools
                filterToolsByCategory("power");
            } else if (checkedIds.contains(R.id.chip_hand_tools)) {
                // Filter for hand tools
                filterToolsByCategory("hand");
            } else if (checkedIds.contains(R.id.chip_outdoor)) {
                // Filter for outdoor tools
                filterToolsByCategory("outdoor");
            }
            
            // Handle view mode changes
            if (checkedIds.contains(R.id.chip_list)) {
                selectedViewMode = 0;
                showListView();
            } else if (checkedIds.contains(R.id.chip_map)) {
                selectedViewMode = 1;
                showMapView();
            }
        });
        
        // Set default selection to "All Tools" category
        if (viewModeChips.findViewById(R.id.chip_all) != null) {
            viewModeChips.check(R.id.chip_all);
        }
    }

    private void filterToolsByCategory(String category) {
        List<Tool> categoryTools = new ArrayList<>();
        for (Tool tool : allTools) {
            String toolName = tool.getName().toLowerCase();
            String toolDescription = tool.getDescription().toLowerCase();
            
            switch (category) {
                case "power":
                    if (toolName.contains("drill") || toolName.contains("saw") || toolName.contains("grinder") ||
                        toolName.contains("power") || toolDescription.contains("electric") || 
                        toolDescription.contains("power")) {
                        categoryTools.add(tool);
                    }
                    break;
                case "hand":
                    if (toolName.contains("hammer") || toolName.contains("wrench") || toolName.contains("screwdriver") ||
                        toolName.contains("hand") || toolDescription.contains("manual") || 
                        toolDescription.contains("hand")) {
                        categoryTools.add(tool);
                    }
                    break;
                case "outdoor":
                    if (toolName.contains("mower") || toolName.contains("trimmer") || toolName.contains("chainsaw") ||
                        toolName.contains("garden") || toolDescription.contains("outdoor") || 
                        toolDescription.contains("yard")) {
                        categoryTools.add(tool);
                    }
                    break;
                default:
                    categoryTools.addAll(allTools);
                    break;
            }
        }
        
        filteredTools = categoryTools;
        applyFiltersAndSort();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(getContext(), "Location permission denied. Distance filtering unavailable.", 
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            userLocation = location;
                            applyFiltersAndSort();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to get location", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadTools() {
        setLoadingState(true);
        
        networkManager.fetchTools(new NetworkManager.NetworkCallback<List<Tool>>() {
            @Override
            public void onSuccess(List<Tool> tools) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        allTools.clear();
                        allTools.addAll(tools);
                        applyFiltersAndSort();
                        setLoadingState(false);
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        setLoadingState(false);
                        Toast.makeText(getContext(), "Failed to load tools: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void applyFiltersAndSort() {
        List<Tool> filtered = filterTools(allTools);
        List<Tool> sorted = sortToolsByDistance(filtered);
        
        filteredTools.clear();
        filteredTools.addAll(sorted);
        
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                toolsAdapter.updateTools(filteredTools);
                updateEmptyState();
                updateHeaderSubtitle();
            });
        }
    }

    private List<Tool> filterTools(List<Tool> tools) {
        List<Tool> filtered = new ArrayList<>();
        
        for (Tool tool : tools) {
            // Price filter
            try {
                double price = Double.parseDouble(tool.getPrice());
                if (price < minPrice || price > maxPrice) {
                    continue;
                }
            } catch (NumberFormatException e) {
                continue; // Skip tools with invalid price
            }
            
            // Distance filter
            if (selectedDistanceRange != DistanceRange.ALL && userLocation != null) {
                Double distance = calculateDistance(userLocation, tool);
                if (distance == null || distance > selectedDistanceRange.getMaxDistanceMeters()) {
                    continue;
                }
            }
            
            filtered.add(tool);
        }
        
        return filtered;
    }

    private List<Tool> sortToolsByDistance(List<Tool> tools) {
        if (userLocation == null) {
            return tools;
        }
        
        List<Tool> sorted = new ArrayList<>(tools);
        Collections.sort(sorted, new Comparator<Tool>() {
            @Override
            public int compare(Tool t1, Tool t2) {
                Double d1 = calculateDistance(userLocation, t1);
                Double d2 = calculateDistance(userLocation, t2);
                
                if (d1 == null && d2 == null) return 0;
                if (d1 == null) return 1;
                if (d2 == null) return -1;
                
                return Double.compare(d1, d2);
            }
        });
        
        return sorted;
    }

    private Double calculateDistance(Location userLocation, Tool tool) {
        if (tool.getLatitude() == null || tool.getLongitude() == null) {
            return null;
        }
        
        Location toolLocation = new Location("");
        toolLocation.setLatitude(tool.getLatitude());
        toolLocation.setLongitude(tool.getLongitude());
        
        return (double) userLocation.distanceTo(toolLocation);
    }

    private void showFilterDialog() {
        FilterDialogFragment dialog = FilterDialogFragment.newInstance(
                minPrice, maxPrice, selectedDistanceRange);
        dialog.setFilterListener(new FilterDialogFragment.FilterListener() {
            @Override
            public void onFiltersApplied(double minPrice, double maxPrice, DistanceRange distanceRange) {
                WelcomeFragment.this.minPrice = minPrice;
                WelcomeFragment.this.maxPrice = maxPrice;
                WelcomeFragment.this.selectedDistanceRange = distanceRange;
                applyFiltersAndSort();
            }

            @Override
            public void onFiltersReset() {
                clearFilters();
            }
        });
        dialog.show(getParentFragmentManager(), "filter_dialog");
    }

    private void clearFilters() {
        minPrice = 10.0;
        maxPrice = 10000.0;
        selectedDistanceRange = DistanceRange.ALL;
        applyFiltersAndSort();
    }

    private void showListView() {
        // Show the RecyclerView and hide any map view
        recyclerView.setVisibility(View.VISIBLE);
        // TODO: Hide map view when implemented
        Toast.makeText(getContext(), "Showing tools in list view", Toast.LENGTH_SHORT).show();
    }

    private void showMapView() {
        // TODO: Implement map view functionality
        // For now, show a message that map view will be implemented
        Toast.makeText(getContext(), "Map view - Coming Soon! Will show all tools on map", Toast.LENGTH_LONG).show();
        
        // Keep list view visible for now
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void setLoadingState(boolean loading) {
        swipeRefreshLayout.setRefreshing(loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        boolean isEmpty = filteredTools.isEmpty();
        emptyTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        
        if (isEmpty) {
            if (allTools.isEmpty()) {
                emptyTextView.setText("No tools available\nCheck back later for new listings");
            } else {
                emptyTextView.setText("No tools match your filters\nTry adjusting your filter settings");
            }
        }
    }

    private void updateHeaderSubtitle() {
        if (hasActiveFilters()) {
            headerSubtitle.setText("Filters applied");
            headerSubtitle.setVisibility(View.VISIBLE);
        } else {
            headerSubtitle.setVisibility(View.GONE);
        }
    }

    private boolean hasActiveFilters() {
        return minPrice > 10 || maxPrice < 10000 || selectedDistanceRange != DistanceRange.ALL;
    }

    @Override
    public void onToolClick(Tool tool) {
        // Navigate to ToolDetailFragment
        // TODO: Implement navigation to tool detail
        Toast.makeText(getContext(), "Tool clicked: " + tool.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
        progressBar = null;
        emptyTextView = null;
        filterButton = null;
        viewModeChips = null;
        listChip = null;
        mapChip = null;
        headerSubtitle = null;
        swipeRefreshLayout = null;
    }
} 