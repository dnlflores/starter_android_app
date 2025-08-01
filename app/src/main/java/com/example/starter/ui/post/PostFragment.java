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
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
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
        
        // Back button in address search
        binding.btnBackSearch.setOnClickListener(v -> {
            System.out.println("Back button clicked!");
            collapseAddressSearch();
        });
        
        // Add photo button click
        binding.btnAddPhoto.setOnClickListener(v -> {
            showPhotoSelectionDialog();
        });
        
        // Close button in header
        binding.btnClose.setOnClickListener(v -> {
            // Navigate back or close the fragment
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
        
        // Publish button
        binding.btnPublish.setOnClickListener(v -> {
            publishListing();
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
                // Camera permission granted, open camera immediately
                Toast.makeText(requireContext(), "Camera permission granted", Toast.LENGTH_SHORT).show();
                openCamera();
            } else {
                // Camera permission denied
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                    // User denied but didn't check "Don't ask again"
                    Toast.makeText(requireContext(), "Camera permission is required to take photos", Toast.LENGTH_LONG).show();
                } else {
                    // User denied and checked "Don't ask again" - show settings dialog
                    showPermissionSettingsDialog("Camera", "camera");
                }
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Storage permission granted, open gallery immediately
                Toast.makeText(requireContext(), "Gallery access granted", Toast.LENGTH_SHORT).show();
                openGallery();
            } else {
                // Storage permission denied
                String permissionName = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU 
                    ? Manifest.permission.READ_MEDIA_IMAGES 
                    : Manifest.permission.READ_EXTERNAL_STORAGE;
                
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permissionName)) {
                    // User denied but didn't check "Don't ask again"
                    Toast.makeText(requireContext(), "Photo access permission is required to select images", Toast.LENGTH_LONG).show();
                } else {
                    // User denied and checked "Don't ask again" - show settings dialog
                    showPermissionSettingsDialog("Photo Access", "storage");
                }
            }
        }
    }
    
    private void showPermissionSettingsDialog(String permissionType, String permissionKey) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle(permissionType + " Permission Required");
        builder.setMessage(permissionType + " permission is required for this feature. Please enable it in Settings > Apps > " + 
                          getString(R.string.app_name) + " > Permissions.");
        builder.setPositiveButton("Open Settings", (dialog, which) -> {
            // Open app settings
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.fromParts("package", requireContext().getPackageName(), null));
            startActivity(intent);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }
    
    // Photo selection methods
    private void showPhotoSelectionDialog() {
        // Always show camera option - let the system handle if camera is actually available
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
    
    private boolean isCameraAvailable() {
        try {
            // Check multiple camera intents to be more comprehensive
            Intent[] cameraIntents = {
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                new Intent("android.media.action.IMAGE_CAPTURE"),
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE),
                new Intent(Intent.ACTION_CAMERA_BUTTON)
            };
            
            PackageManager packageManager = requireActivity().getPackageManager();
            
            for (Intent intent : cameraIntents) {
                if (intent.resolveActivity(packageManager) != null) {
                    return true;
                }
            }
            
            // Also check if device has camera hardware
            return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) ||
                   packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
            
        } catch (Exception e) {
            // If there's any error, assume camera might be available and let the user try
            return true;
        }
    }
    
    private void checkCameraPermissionAndOpen() {
        // Check if camera permission is already granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, open camera directly
            openCamera();
        } else {
            // Check if we should show rationale for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                // Show explanation to user before requesting permission
                showCameraPermissionRationale();
            } else {
                // Request permission directly
                ActivityCompat.requestPermissions(requireActivity(), 
                        new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        }
    }
    
    private void showCameraPermissionRationale() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Camera Permission Required");
        builder.setMessage("This app needs camera access to take photos for your tool listings. Please grant camera permission to continue.");
        builder.setPositiveButton("Grant Permission", (dialog, which) -> {
            ActivityCompat.requestPermissions(requireActivity(), 
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            Toast.makeText(requireContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }
    
    private void checkStoragePermissionAndOpen() {
        // For Android 13+ (API 33+), we need READ_MEDIA_IMAGES permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED) {
                // Permission already granted, open gallery directly
                openGallery();
            } else {
                // Check if we should show rationale for permission
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)) {
                    showStoragePermissionRationale(Manifest.permission.READ_MEDIA_IMAGES);
                } else {
                    // Request permission directly
                    ActivityCompat.requestPermissions(requireActivity(), 
                            new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
                }
            }
        } else {
            // For older Android versions, check READ_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED) {
                // Permission already granted, open gallery directly
                openGallery();
            } else {
                // Check if we should show rationale for permission
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showStoragePermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE);
                } else {
                    // Request permission directly
                    ActivityCompat.requestPermissions(requireActivity(), 
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
                }
            }
        }
    }
    
    private void showStoragePermissionRationale(String permission) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Gallery Access Required");
        builder.setMessage("This app needs access to your photos to select images for your tool listings. Please grant photo access permission to continue.");
        builder.setPositiveButton("Grant Permission", (dialog, which) -> {
            ActivityCompat.requestPermissions(requireActivity(), 
                    new String[]{permission}, REQUEST_STORAGE_PERMISSION);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            Toast.makeText(requireContext(), "Photo access permission is required to select images", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }
    
    private void openCamera() {
        try {
            // Try multiple camera intents in order of preference
            Intent[] cameraIntents = {
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                new Intent("android.media.action.IMAGE_CAPTURE"),
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE)
            };
            
            PackageManager packageManager = requireActivity().getPackageManager();
            boolean cameraFound = false;
            
            for (Intent intent : cameraIntents) {
                if (intent.resolveActivity(packageManager) != null) {
                    try {
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                        cameraFound = true;
                        break;
                    } catch (Exception e) {
                        // If this intent fails, try the next one
                        System.out.println("Camera intent failed: " + intent.getAction() + " - " + e.getMessage());
                        continue;
                    }
                }
            }
            
            if (!cameraFound) {
                // If no camera app is found, try a more generic approach
                try {
                    Intent genericCameraIntent = new Intent(Intent.ACTION_CAMERA_BUTTON);
                    if (genericCameraIntent.resolveActivity(packageManager) != null) {
                        startActivityForResult(genericCameraIntent, REQUEST_IMAGE_CAPTURE);
                        cameraFound = true;
                    }
                } catch (Exception e) {
                    System.out.println("Generic camera intent also failed: " + e.getMessage());
                }
                
                if (!cameraFound) {
                    // Last resort: try to open any camera app
                    try {
                        Intent anyCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(anyCameraIntent, REQUEST_IMAGE_CAPTURE);
                        cameraFound = true;
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Unable to open camera. Please use gallery option instead.", Toast.LENGTH_LONG).show();
                        System.out.println("All camera attempts failed: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error opening camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void openGallery() {
        try {
            Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageIntent.setType("image/*");
            
            // Check if there's a gallery app available
            if (pickImageIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivityForResult(pickImageIntent, REQUEST_IMAGE_PICK);
            } else {
                // Try alternative gallery intents
                Intent[] galleryIntents = {
                    new Intent(Intent.ACTION_GET_CONTENT),
                    new Intent(Intent.ACTION_OPEN_DOCUMENT)
                };
                
                boolean galleryFound = false;
                for (Intent intent : galleryIntents) {
                    intent.setType("image/*");
                    if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                        startActivityForResult(intent, REQUEST_IMAGE_PICK);
                        galleryFound = true;
                        break;
                    }
                }
                
                if (!galleryFound) {
                    Toast.makeText(requireContext(), "No gallery app found. Please install a gallery app.", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error opening gallery: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
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
            binding.photoText.setText("Photo Selected ✓");
            binding.photoText.setTextColor(requireContext().getResources().getColor(android.R.color.holo_green_light, null));
            binding.btnAddPhoto.setBackgroundResource(R.drawable.photo_button_with_image_background);
            
            // Update icon tint to green
            binding.photoIcon.setColorFilter(requireContext().getResources().getColor(android.R.color.holo_green_light, null), PorterDuff.Mode.SRC_IN);
        }
    }
    
    private void resetPhotoButton() {
        binding.photoText.setText(getString(R.string.add_photo));
        binding.photoText.setTextColor(requireContext().getResources().getColor(android.R.color.white, null));
        binding.btnAddPhoto.setBackgroundResource(R.drawable.photo_button_background);
        
        // Reset icon tint to white
        binding.photoIcon.setColorFilter(requireContext().getResources().getColor(android.R.color.white, null), PorterDuff.Mode.SRC_IN);
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
    
    private void publishListing() {
        // Get form data
        String toolName = binding.etToolName.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String address = binding.etAddressCollapsed.getText().toString().trim();
        String priceText = binding.etPrice.getText().toString().trim();
        
        // Validate form
        if (toolName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a tool name", Toast.LENGTH_SHORT).show();
            binding.etToolName.requestFocus();
            return;
        }
        
        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a description", Toast.LENGTH_SHORT).show();
            binding.etDescription.requestFocus();
            return;
        }
        
        if (address.isEmpty()) {
            Toast.makeText(requireContext(), "Please select an address", Toast.LENGTH_SHORT).show();
            binding.collapsedAddressInput.requestFocus();
            return;
        }
        
        if (priceText.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a price", Toast.LENGTH_SHORT).show();
            binding.etPrice.requestFocus();
            return;
        }
        
        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Please enter a valid price", Toast.LENGTH_SHORT).show();
            binding.etPrice.requestFocus();
            return;
        }
        
        // Show loading state
        binding.btnPublish.setEnabled(false);
        binding.btnPublish.setText("Publishing...");
        
        // TODO: Implement actual API call to create listing
        // For now, just show success message
        Toast.makeText(requireContext(), "Listing published successfully!", Toast.LENGTH_LONG).show();
        
        // Reset form
        binding.etToolName.setText("");
        binding.etDescription.setText("");
        binding.etAddressCollapsed.setText("");
        binding.etPrice.setText("");
        resetPhotoButton();
        
        // Reset button state
        binding.btnPublish.setEnabled(true);
        binding.btnPublish.setText(getString(R.string.publish_listing));
        
        // Navigate back
        if (getActivity() != null) {
            getActivity().onBackPressed();
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