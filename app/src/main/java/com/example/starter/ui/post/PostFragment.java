package com.example.starter.ui.post;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
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
    
    // Photo selection constants
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 1002;
    private static final int REQUEST_STORAGE_PERMISSION = 1003;
    
    // Photo variables
    private Uri selectedImageUri;
    private Bitmap selectedImageBitmap;
    
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
            System.out.println("Collapsed address input clicked!");
            if (!isSearchExpanded) {
                System.out.println("Expanding address search...");
                expandAddressSearch();
            }
        });
        
        // Also add click listener to the EditText itself as backup
        binding.etAddressCollapsed.setOnClickListener(v -> {
            System.out.println("EditText address clicked!");
            if (!isSearchExpanded) {
                System.out.println("Expanding address search from EditText...");
                expandAddressSearch();
            }
        });
        
        // Add photo button click
        binding.btnAddPhoto.setOnClickListener(v -> {
            showPhotoSelectionDialog();
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
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    // Photo selection methods
    private void showPhotoSelectionDialog() {
        String[] options;
        if (selectedImageBitmap != null) {
            options = new String[]{"Take Photo", "Choose from Gallery", "Remove Photo", "Cancel"};
        } else {
            options = new String[]{"Take Photo", "Choose from Gallery", "Cancel"};
        }
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Add Photo");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Take Photo
                    checkCameraPermissionAndOpen();
                    break;
                case 1: // Choose from Gallery
                    checkStoragePermissionAndOpen();
                    break;
                case 2: // Remove Photo or Cancel
                    if (selectedImageBitmap != null) {
                        resetPhotoButton();
                        Toast.makeText(requireContext(), "Photo removed", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                    break;
                case 3: // Cancel (only when photo is selected)
                    if (selectedImageBitmap != null) {
                        dialog.dismiss();
                    }
                    break;
            }
        });
        builder.show();
    }
    
    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), 
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }
    
    private void checkStoragePermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), 
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        } else {
            openGallery();
        }
    }
    
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(requireContext(), "Camera app not available", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openGallery() {
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageIntent.setType("image/*");
        startActivityForResult(pickImageIntent, REQUEST_IMAGE_PICK);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                // Handle camera photo
                Bundle extras = data.getExtras();
                if (extras != null) {
                    selectedImageBitmap = (Bitmap) extras.get("data");
                    if (selectedImageBitmap != null) {
                        // Update button to show selected image
                        updatePhotoButtonWithImage();
                        Toast.makeText(requireContext(), "Photo captured successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                // Handle gallery photo
                selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        selectedImageBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri);
                        updatePhotoButtonWithImage();
                        Toast.makeText(requireContext(), "Photo selected successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    private void updatePhotoButtonWithImage() {
        if (selectedImageBitmap != null) {
            // Update button appearance to show photo was selected
            binding.btnAddPhoto.setText("Photo Selected ✓");
            binding.btnAddPhoto.setBackgroundResource(R.drawable.photo_button_with_image_background);
            binding.btnAddPhoto.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            
            // Change text color to green to indicate success
            binding.btnAddPhoto.setTextColor(requireContext().getResources().getColor(android.R.color.holo_green_light, null));
        }
    }
    
    private void resetPhotoButton() {
        binding.btnAddPhoto.setText(getString(R.string.add_photo));
        binding.btnAddPhoto.setBackgroundResource(R.drawable.photo_button_background);
        binding.btnAddPhoto.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_photo, 0, 0, 0);
        binding.btnAddPhoto.setTextColor(requireContext().getResources().getColor(android.R.color.white, null));
        selectedImageBitmap = null;
        selectedImageUri = null;
    }
    
    // Getter methods for the selected photo data
    public Bitmap getSelectedImageBitmap() {
        return selectedImageBitmap;
    }
    
    public Uri getSelectedImageUri() {
        return selectedImageUri;
    }
    
    public boolean hasPhotoSelected() {
        return selectedImageBitmap != null;
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