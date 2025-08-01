package com.example.starter.ui.post;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.starter.R;
import com.example.starter.databinding.FragmentPostBinding;
import com.example.starter.model.AddressResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.content.pm.ApplicationInfo;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;

public class PostFragment extends Fragment implements AddressSearchAdapter.OnAddressClickListener {

    private FragmentPostBinding binding;
    private AddressSearchAdapter addressAdapter;
    private ExecutorService executorService;
    private boolean isSearchExpanded = false;
    private LocationManager locationManager;
    private Location currentLocation;
    private Handler mainHandler;
    
    // Google Places API
    private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Places API with the correct method
        try {
            // Get API key from manifest
            String apiKey = getApiKeyFromManifest();
            if (apiKey != null && !apiKey.isEmpty()) {
                Places.initializeWithNewPlacesApiEnabled(requireContext(), apiKey);
                placesClient = Places.createClient(requireContext());
                System.out.println("Places API initialized successfully with key: " + apiKey.substring(0, 10) + "...");
            } else {
                System.out.println("No API key found in manifest");
            }
        } catch (Exception e) {
            System.out.println("Error initializing Places API: " + e.getMessage());
            e.printStackTrace();
        }
        
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    private String getApiKeyFromManifest() {
        try {
            ApplicationInfo appInfo = requireContext().getPackageManager()
                    .getApplicationInfo(requireContext().getPackageName(), PackageManager.GET_META_DATA);
            
            if (appInfo.metaData != null) {
                String apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY");
                System.out.println("Found API key in manifest: " + (apiKey != null ? apiKey.substring(0, 10) + "..." : "null"));
                return apiKey;
            }
        } catch (Exception e) {
            System.out.println("Error getting API key from manifest: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize other components
        locationManager = (LocationManager) requireContext().getSystemService(requireContext().LOCATION_SERVICE);
        sessionToken = AutocompleteSessionToken.newInstance();
        
        setupAddressSearch();
        setupClickListeners();
        getCurrentLocation();
    }

    private void setupAddressSearch() {
        // Setup RecyclerView
        binding.rvAddressResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        addressAdapter = new AddressSearchAdapter(this);
        binding.rvAddressResults.setAdapter(addressAdapter);

        // Setup text watcher for real-time search
        binding.etSearchAddress.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler(Looper.getMainLooper());
            private Runnable searchRunnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Cancel previous search
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }
                
                String query = s.toString().trim();
                
                // Show/hide clear button with animation
                if (query.isEmpty()) {
                    binding.btnClearSearch.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                        binding.btnClearSearch.setVisibility(View.GONE);
                    }).start();
                } else {
                    if (binding.btnClearSearch.getVisibility() == View.GONE) {
                        binding.btnClearSearch.setVisibility(View.VISIBLE);
                        binding.btnClearSearch.setAlpha(0f);
                        binding.btnClearSearch.animate().alpha(1f).setDuration(200).start();
                    }
                }
                
                // Start new search after 500ms delay
                searchRunnable = () -> {
                    if (query.length() >= 2) {
                        searchAddressesWithPlacesAPI(query);
                    } else if (query.isEmpty()) {
                        showLoading(false);
                        showNoResults(false);
                        addressAdapter.setAddresses(new ArrayList<>());
                    }
                };
                handler.postDelayed(searchRunnable, 500);
            }
        });

        // Clear search button
        binding.btnClearSearch.setOnClickListener(v -> {
            binding.etSearchAddress.setText("");
            addressAdapter.setAddresses(new ArrayList<>());
            showNoResults(false);
            binding.etSearchAddress.requestFocus();
        });
    }

    private void searchAddressesWithPlacesAPI(String query) {
        if (placesClient == null) {
            System.out.println("PlacesClient is null, cannot search");
            return;
        }

        System.out.println("Searching with Places API for: " + query);

        // Build the autocomplete request
        FindAutocompletePredictionsRequest.Builder requestBuilder = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setSessionToken(sessionToken);

        // Add location bias if we have current location
        if (currentLocation != null) {
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            requestBuilder.setOrigin(currentLatLng);
            
            // Create bounds around current location for better results
            double lat = currentLocation.getLatitude();
            double lng = currentLocation.getLongitude();
            double offset = 0.1; // About 11km radius
            LatLngBounds bounds = LatLngBounds.builder()
                    .include(new LatLng(lat - offset, lng - offset))
                    .include(new LatLng(lat + offset, lng + offset))
                    .build();
            requestBuilder.setLocationBias(com.google.android.libraries.places.api.model.RectangularBounds.newInstance(bounds));
        }

        FindAutocompletePredictionsRequest request = requestBuilder.build();

        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener(response -> {
                    System.out.println("Places API success, found " + response.getAutocompletePredictions().size() + " predictions");
                    
                    List<AddressResult> results = new ArrayList<>();
                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                        String title = prediction.getPrimaryText(null).toString();
                        String subtitle = prediction.getSecondaryText(null).toString();
                        
                        // Get place details to get coordinates
                        List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.ADDRESS);
                        FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(prediction.getPlaceId(), placeFields)
                                .setSessionToken(sessionToken)
                                .build();

                        placesClient.fetchPlace(placeRequest)
                                .addOnSuccessListener(placeResponse -> {
                                    Place place = placeResponse.getPlace();
                                    if (place.getLatLng() != null) {
                                        String distanceText = "";
                                        if (currentLocation != null) {
                                            float[] distance = new float[1];
                                            Location.distanceBetween(
                                                currentLocation.getLatitude(), currentLocation.getLongitude(),
                                                place.getLatLng().latitude, place.getLatLng().longitude, distance
                                            );
                                            distanceText = " • " + formatDistance(distance[0]);
                                        }

                                        AddressResult result = new AddressResult(
                                            title,
                                            subtitle + distanceText,
                                            place.getLatLng().latitude,
                                            place.getLatLng().longitude,
                                            place.getAddress() != null ? place.getAddress() : title + ", " + subtitle
                                        );
                                        results.add(result);

                                        // Update UI when we have results
                                        if (results.size() == response.getAutocompletePredictions().size()) {
                                            final List<AddressResult> finalResults = results;
                                            requireActivity().runOnUiThread(() -> {
                                                addressAdapter.setAddresses(finalResults);
                                                showLoading(false);
                                                showNoResults(false);
                                            });
                                        }
                                    }
                                })
                                .addOnFailureListener(exception -> {
                                    System.out.println("Error fetching place details: " + exception.getMessage());
                                });
                    }

                    // If no results, show no results
                    if (response.getAutocompletePredictions().isEmpty()) {
                        requireActivity().runOnUiThread(() -> {
                            showLoading(false);
                            showNoResults(true);
                        });
                    }
                })
                .addOnFailureListener(exception -> {
                    System.out.println("Places API error: " + exception.getMessage());
                    exception.printStackTrace();
                    requireActivity().runOnUiThread(() -> {
                        showLoading(false);
                        showNoResults(true);
                        Toast.makeText(requireContext(), "Search error: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                    });
                });
    }

    private String formatDistance(float distance) {
        if (distance < 1000) {
            return String.format("%.0f m", distance);
        } else {
            return String.format("%.1f km", distance / 1000);
        }
    }

    private void setupClickListeners() {
        // Collapsed address input click
        binding.collapsedAddressInput.setOnClickListener(v -> {
            if (!isSearchExpanded) {
                expandAddressSearch();
            }
        });

        // Use Current Location button
        binding.btnUseCurrentLocation.setOnClickListener(v -> {
            // Clear the search input
            binding.etSearchAddress.setText("");
            
            if (currentLocation != null) {
                searchNearbyAddresses();
                Toast.makeText(requireContext(), "Showing nearby addresses", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Location not available. Please enable location services.", Toast.LENGTH_LONG).show();
                getCurrentLocation();
            }
        });
    }

    private void expandAddressSearch() {
        isSearchExpanded = true;
        binding.addressSearchContainer.setVisibility(View.VISIBLE);
        binding.addressSearchContainer.setAlpha(0f);
        binding.addressSearchContainer.animate().alpha(1f).setDuration(300).start();
        
        // Focus on search input
        binding.etSearchAddress.requestFocus();
        
        // Show keyboard
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(requireContext().INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(binding.etSearchAddress, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void collapseAddressSearch() {
        isSearchExpanded = false;
        binding.addressSearchContainer.animate().alpha(0f).setDuration(300).withEndAction(() -> {
            binding.addressSearchContainer.setVisibility(View.GONE);
        }).start();
        
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(requireContext().INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.etSearchAddress.getWindowToken(), 0);
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), 
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }

        executorService.execute(() -> {
            try {
                // Try GPS first, then Network provider
                Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (gpsLocation != null) {
                    currentLocation = gpsLocation;
                } else if (networkLocation != null) {
                    currentLocation = networkLocation;
                }

                if (currentLocation != null) {
                    System.out.println("Current location: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
                } else {
                    System.out.println("Could not get current location");
                }
            } catch (Exception e) {
                System.out.println("Error getting location: " + e.getMessage());
            }
        });
    }

    private void searchNearbyAddresses() {
        if (currentLocation == null) {
            Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Search for common address terms near current location
        String[] commonTerms = {"street", "avenue", "road", "drive", "lane", "way"};
        
        showLoading(true);
        showNoResults(false);
        
        // Use the first term to get nearby addresses
        searchAddressesWithPlacesAPI(commonTerms[0]);
    }

    private void showLoading(boolean show) {
        // Implement loading indicator if needed
    }

    private void showNoResults(boolean show) {
        // Implement no results message if needed
    }

    @Override
    public void onAddressClick(AddressResult address) {
        // Handle address selection
        binding.etAddressCollapsed.setText(address.getTitle());
        collapseAddressSearch();
        
        Toast.makeText(requireContext(), "Selected: " + address.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (executorService != null) {
            executorService.shutdown();
        }
        binding = null;
    }
} 