package com.example.starter.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.starter.R;
import com.example.starter.databinding.FragmentHomeBinding;
import com.example.starter.model.Tool;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private FragmentHomeBinding binding;
    private ToolsAdapter toolsAdapter;
    private boolean isListView = true;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

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

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Set up map
        setupMap();

        // Observe data from ViewModel
        homeViewModel.getTools().observe(getViewLifecycleOwner(), tools -> {
            Log.d("HomeFragment", "Tools data observed in onCreateView: " + (tools != null ? tools.size() : "null") + " tools");
            if (tools != null && !tools.isEmpty()) {
                Log.d("HomeFragment", "First tool: " + tools.get(0).getName() + 
                        " at " + tools.get(0).getLatitude() + ", " + tools.get(0).getLongitude());
            }
            toolsAdapter.setTools(tools);
            // Only show/hide the list container if we're in list view mode
            if (isListView) {
                binding.listContainer.setVisibility(tools != null && !tools.isEmpty() ? View.VISIBLE : View.GONE);
            }
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
        
        // Show list view, hide map view
        binding.listContainer.setVisibility(View.VISIBLE);
        binding.mapContainer.setVisibility(View.GONE);
    }

    private void setMapViewActive() {
        Log.d("HomeFragment", "Switching to map view");
        isListView = false;
        binding.buttonMap.setBackgroundResource(R.drawable.button_toggle_active_background);
        binding.buttonMap.setTextColor(getResources().getColor(android.R.color.black));
        binding.buttonList.setBackgroundResource(android.R.color.transparent);
        binding.buttonList.setTextColor(getResources().getColor(android.R.color.white));
        
        // Hide list view, show map view
        binding.listContainer.setVisibility(View.GONE);
        binding.mapContainer.setVisibility(View.VISIBLE);
        
        Log.d("HomeFragment", "Map container visibility: " + binding.mapContainer.getVisibility());
        
        // Request location permission if needed
        if (checkLocationPermission()) {
            enableMyLocation();
        } else {
            requestLocationPermission();
        }
    }
    
    private void onToolClick(Tool tool) {
        try {
            Log.d("HomeFragment", "Navigating to tool detail for tool ID: " + tool.getId());
            // Navigate to tool detail fragment
            Bundle bundle = new Bundle();
            bundle.putInt("tool_id", tool.getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_home_to_tool_detail, bundle);
        } catch (Exception e) {
            Log.e("HomeFragment", "Error navigating to tool detail", e);
            Toast.makeText(requireContext(), "Error opening tool details", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupMap() {
        Log.d("HomeFragment", "Setting up map...");
        binding.mapView.onCreate(null);
        binding.mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        Log.d("HomeFragment", "Map is ready!");
        googleMap = map;
        
        // Set map properties
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false); // Disable default toolbar
        
        // Adjust UI settings to avoid bottom navigation overlap
        googleMap.getUiSettings().setZoomControlsEnabled(false); // Disable zoom controls (they overlap with bottom nav)
        
        // Set padding to avoid bottom navigation - use precise bottom nav height
        int bottomNavHeight = (int) (56 * getResources().getDisplayMetrics().density); // Standard Material Design bottom nav height
        googleMap.setPadding(0, 0, 0, bottomNavHeight);
        
        // Set initial camera position to Austin area (where the seed data is located)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(30.2672, -97.7431), 11));
        
        Log.d("HomeFragment", "Map camera moved to Austin area");
        
        // Add a test marker to verify map is working
        LatLng testLocation = new LatLng(30.2672, -97.7431);
        MarkerOptions testMarker = new MarkerOptions()
                .position(testLocation)
                .title("Test Marker")
                .snippet("Map is working!");
        googleMap.addMarker(testMarker);
        Log.d("HomeFragment", "Added test marker at Austin");
        
        // Add markers for tools when data is available
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.getTools().observe(getViewLifecycleOwner(), tools -> {
            Log.d("HomeFragment", "Tools data received: " + (tools != null ? tools.size() : "null") + " tools");
            if (tools != null && googleMap != null) {
                addToolMarkers(tools);
            }
        });
    }

    private void addToolMarkers(java.util.List<Tool> tools) {
        Log.d("HomeFragment", "Adding markers for " + tools.size() + " tools");
        
        if (googleMap == null) {
            Log.e("HomeFragment", "GoogleMap is null, cannot add markers");
            return;
        }
        
        googleMap.clear();
        
        // Use actual tool coordinates from the database
        for (Tool tool : tools) {
            Log.d("HomeFragment", "Adding marker for tool: " + tool.getName() + 
                    " at location: " + tool.getLatitude() + ", " + tool.getLongitude());
            
            LatLng location = new LatLng(tool.getLatitude(), tool.getLongitude());
            
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(location)
                    .title(tool.getName())
                    .snippet("$" + tool.getPrice() + " • Owner: " + tool.getOwnerUsername());
            
            googleMap.addMarker(markerOptions);
        }
        
        // Move camera to show all markers with appropriate bounds
        if (!tools.isEmpty()) {
            Log.d("HomeFragment", "Moving camera to show all markers");
            // Calculate bounds to include all markers
            com.google.android.gms.maps.model.LatLngBounds.Builder boundsBuilder = new com.google.android.gms.maps.model.LatLngBounds.Builder();
            for (Tool tool : tools) {
                boundsBuilder.include(new LatLng(tool.getLatitude(), tool.getLongitude()));
            }
            com.google.android.gms.maps.model.LatLngBounds bounds = boundsBuilder.build();
            
            // Add padding to the bounds for better view
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } else {
            Log.d("HomeFragment", "No tools to show on map");
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void enableMyLocation() {
        if (googleMap != null && checkLocationPermission()) {
            googleMap.setMyLocationEnabled(true);
            
            // Get current location and move camera
            if (ActivityCompat.checkSelfPermission(requireContext(), 
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(requireActivity(), location -> {
                            if (location != null) {
                                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 11));
                            }
                        });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(requireContext(), "Location permission is required for map functionality", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        binding.mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapView.onLowMemory();
    }
}