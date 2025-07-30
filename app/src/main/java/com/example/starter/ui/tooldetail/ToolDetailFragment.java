package com.example.starter.ui.tooldetail;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.starter.R;
import com.example.starter.model.Tool;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class ToolDetailFragment extends Fragment implements OnMapReadyCallback {
    
    private static final String ARG_TOOL_ID = "tool_id";
    
    private ToolDetailViewModel viewModel;
    private ImageView toolImage;
    private TextView toolName;
    private TextView toolPrice;
    private TextView bottomPrice;
    private TextView toolDescription;
    private TextView toolOwner;
    private LinearLayout ownerSection;
    private ProgressBar progressBar;
    private TextView errorText;
    private View mapOverlay;
    
    private GoogleMap map;
    private Tool currentTool;
    
    private int toolId;

    @SuppressWarnings("unused")
    public static ToolDetailFragment newInstance(int toolId) {
        ToolDetailFragment fragment = new ToolDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TOOL_ID, toolId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            toolId = getArguments().getInt(ARG_TOOL_ID, -1);
            if (toolId == -1) {
                // Try to get from navigation arguments
                toolId = getArguments().getInt("tool_id", -1);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tool_detail, container, false);
        
        initViews(root);
        setupViewModel();
        observeViewModel();
        setupMap();
        
        // Load tool details
        if (toolId != -1) {
            viewModel.loadTool(toolId);
        } else {
            Log.e("ToolDetailFragment", "Invalid tool ID: " + toolId);
            errorText.setVisibility(View.VISIBLE);
            errorText.setText(R.string.invalid_tool_id);
        }
        
        return root;
    }

    private void initViews(View root) {
        toolImage = root.findViewById(R.id.tool_image);
        toolName = root.findViewById(R.id.tool_name);
        toolPrice = root.findViewById(R.id.tool_price);
        bottomPrice = root.findViewById(R.id.bottom_price);
        toolDescription = root.findViewById(R.id.tool_description);
        toolOwner = root.findViewById(R.id.tool_owner);
        ownerSection = root.findViewById(R.id.owner_section);
        progressBar = root.findViewById(R.id.progress_bar);
        errorText = root.findViewById(R.id.error_text);
        mapOverlay = root.findViewById(R.id.map_overlay);
        
        // Set up contact button click listener
        root.findViewById(R.id.button_contact).setOnClickListener(v -> {
            if (currentTool != null) {
                // Navigate to chat or show contact options
                Toast.makeText(requireContext(), 
                    "Contacting " + currentTool.getOwnerUsername(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ToolDetailViewModel.class);
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_preview);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        // Set up map overlay click listener
        if (mapOverlay != null) {
            mapOverlay.setOnClickListener(v -> {
                if (currentTool != null) {
                    // Navigate to full map view or show location details
                    Toast.makeText(requireContext(), 
                        "Location: " + currentTool.getLatitude() + ", " + currentTool.getLongitude(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void observeViewModel() {
        viewModel.getTool().observe(getViewLifecycleOwner(), this::displayTool);
        
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), 
            isLoading -> progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));
        
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            errorText.setVisibility(error != null ? View.VISIBLE : View.GONE);
            if (error != null) {
                errorText.setText(error);
                // Add click listener to retry
                errorText.setOnClickListener(v -> viewModel.retry(toolId));
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        
        // Configure map settings
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        
        // If we already have tool data, show the location
        if (currentTool != null) {
            showToolLocation(currentTool);
        }
    }

    private void displayTool(Tool tool) {
        if (tool == null) return;
        
        currentTool = tool;
        
        toolName.setText(tool.getName());
        
        // Format price with "per day" suffix
        String priceText = String.format(Locale.getDefault(), "$%.2f per day", tool.getPrice());
        toolPrice.setText(priceText);
        bottomPrice.setText(priceText);
        
        toolDescription.setText(tool.getDescription());
        toolOwner.setText(tool.getOwnerUsername());
        
        // Set up owner section click listener
        if (ownerSection != null) {
            ownerSection.setOnClickListener(v -> {
                // Navigate to user profile fragment
                Bundle bundle = new Bundle();
                bundle.putInt("user_id", tool.getOwnerId());
                // For now, show a toast. In a real app, you'd navigate to the profile fragment
                Toast.makeText(requireContext(), 
                    getString(R.string.viewing_profile, tool.getOwnerUsername(), tool.getOwnerId()), 
                    Toast.LENGTH_SHORT).show();
            });
        }
        
        // Load tool image
        loadToolImage(tool.getImageUrl());
        
        // Show tool location on map if map is ready
        if (map != null) {
            showToolLocation(tool);
        }
    }
    
    private void showToolLocation(Tool tool) {
        if (map == null || tool == null) return;
        
        try {
            LatLng toolLocation = new LatLng(tool.getLatitude(), tool.getLongitude());
            
            // Clear existing markers
            map.clear();
            
            // Add marker for tool location
            map.addMarker(new MarkerOptions()
                    .position(toolLocation)
                    .title(tool.getName())
                    .snippet("Tool location"));
            
            // Move camera to tool location with zoom
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(toolLocation, 15f));
            
        } catch (Exception e) {
            Log.e("ToolDetailFragment", "Error showing tool location", e);
        }
    }
    
    private void loadToolImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .centerCrop()
                    .into(toolImage);
        } else {
            toolImage.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }
}