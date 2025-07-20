package com.example.starter.ui.post;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.starter.LoginActivity;
import com.example.starter.R;
import com.example.starter.databinding.FragmentPostBinding;
import com.example.starter.models.Tool;
import com.example.starter.ui.post.PostViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

public class PostFragment extends Fragment {

    private FragmentPostBinding binding;
    private PostViewModel postViewModel;
    private SharedPreferences prefs;
    
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    
    private Uri selectedImageUri;
    private NumberFormat currencyFormatter;
    
    // Location
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;
    private String selectedAddress = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        
        binding = FragmentPostBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        // Initialize SharedPreferences
        prefs = requireActivity().getSharedPreferences("StarterPrefs", Context.MODE_PRIVATE);
        
        // Initialize ViewModel
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);
        
        // Setup currency formatter
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        
        // Initialize activity result launchers
        initializeActivityLaunchers();
        
        // Check authentication status
        checkAuthenticationStatus();
        
        // Setup UI
        setupFormFields();
        setupObservers();
        setupImagePicker();
        setupLocationPicker();
        setupButtons();
        
        return root;
    }
    
    private void checkAuthenticationStatus() {
        String authToken = prefs.getString("authToken", "");
        
        if (authToken.isEmpty()) {
            showLoginPrompt();
        } else {
            showPostForm();
            // No longer need to set auth token - NetworkManager handles this internally
        }
    }
    
    private void showLoginPrompt() {
        binding.loginPromptCard.setVisibility(View.VISIBLE);
        binding.postFormScrollView.setVisibility(View.GONE);
    }
    
    private void showPostForm() {
        binding.loginPromptCard.setVisibility(View.GONE);
        binding.postFormScrollView.setVisibility(View.VISIBLE);
    }
    
    private void initializeActivityLaunchers() {
        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    displaySelectedImage();
                }
            }
        );
        
        // Camera launcher
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            // Convert bitmap to URI (you might want to save to temp file)
                            binding.imagePreview.setImageBitmap(imageBitmap);
                            binding.imagePreview.setVisibility(View.VISIBLE);
                            binding.imageUploadCard.setVisibility(View.GONE);
                        }
                    }
                }
            }
        );
        
        // Camera permission launcher
        cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(getContext(), "Camera permission required", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    private void setupFormFields() {
        // Price field formatting
        binding.editTextPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Name field validation
        binding.editTextName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Description field validation
        binding.editTextDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void setupObservers() {
        postViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.buttonSavePost.setEnabled(!isLoading);
        });
        
        postViewModel.getSuccessMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                clearForm();
                // Optionally switch to home tab to see the new listing
            }
        });
        
        postViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setupImagePicker() {
        binding.imageUploadCard.setOnClickListener(v -> showImageSourceDialog());
        
        binding.buttonChangeImage.setOnClickListener(v -> showImageSourceDialog());
        
        binding.buttonRemoveImage.setOnClickListener(v -> {
            selectedImageUri = null;
            binding.imagePreview.setVisibility(View.GONE);
            binding.imageUploadCard.setVisibility(View.VISIBLE);
            validateForm();
        });
    }
    
    private void setupLocationPicker() {
        binding.locationCard.setOnClickListener(v -> {
            // For now, show a simple input dialog for address
            // In a full implementation, you'd integrate with Google Places API
            showAddressInputDialog();
        });
    }
    
    private void setupButtons() {
        // Login buttons
        binding.loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });
        
        binding.signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.putExtra("showSignUp", true);
            startActivity(intent);
        });
        
        // Save post button
        binding.buttonSavePost.setOnClickListener(v -> savePost());
        
        // Cancel button
        binding.buttonCancel.setOnClickListener(v -> clearForm());
    }
    
    private void showImageSourceDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_image_source, null);
        
        dialogView.findViewById(R.id.button_gallery).setOnClickListener(v -> {
            openGallery();
            dialog.dismiss();
        });
        
        dialogView.findViewById(R.id.button_camera).setOnClickListener(v -> {
            checkCameraPermissionAndOpen();
            dialog.dismiss();
        });
        
        dialog.setContentView(dialogView);
        dialog.show();
    }
    
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
    
    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }
    
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }
    
    private void displaySelectedImage() {
        if (selectedImageUri != null) {
            Glide.with(this)
                    .load(selectedImageUri)
                    .centerCrop()
                    .into(binding.imagePreview);
            
            binding.imagePreview.setVisibility(View.VISIBLE);
            binding.imageUploadCard.setVisibility(View.GONE);
            validateForm();
        }
    }
    
    private void showAddressInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_address_input, null);
        
        // Get views from dialog
        android.widget.EditText editTextAddress = dialogView.findViewById(R.id.edit_text_address);
        editTextAddress.setText(selectedAddress);
        
        builder.setView(dialogView)
                .setTitle("Enter Location")
                .setPositiveButton("Set Location", (dialog, which) -> {
                    String address = editTextAddress.getText().toString().trim();
                    if (!address.isEmpty()) {
                        setLocation(address, 0.0, 0.0); // In real app, geocode the address
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void setLocation(String address, double latitude, double longitude) {
        this.selectedAddress = address;
        this.selectedLatitude = latitude;
        this.selectedLongitude = longitude;
        
        binding.textSelectedLocation.setText(address);
        binding.textLocationPrompt.setVisibility(View.GONE);
        binding.textSelectedLocation.setVisibility(View.VISIBLE);
        
        validateForm();
    }
    
    private void validateForm() {
        String name = binding.editTextName.getText().toString().trim();
        String priceText = binding.editTextPrice.getText().toString().trim();
        String description = binding.editTextDescription.getText().toString().trim();
        
        boolean isValid = !name.isEmpty() && 
                         !priceText.isEmpty() &&
                         !description.isEmpty() &&
                         selectedImageUri != null &&
                         !selectedAddress.isEmpty();
        
        binding.buttonSavePost.setEnabled(isValid);
    }
    
    private void savePost() {
        String name = binding.editTextName.getText().toString().trim();
        String priceText = binding.editTextPrice.getText().toString().trim();
        String description = binding.editTextDescription.getText().toString().trim();
        
        try {
            double price = Double.parseDouble(priceText);
            
            Tool tool = new Tool();
            tool.setName(name);
            tool.setPrice(String.valueOf(price));
            tool.setDescription(description);
            tool.setLatitude(selectedLatitude);
            tool.setLongitude(selectedLongitude);
            
            // Extract individual parameters for the new createTool API
            // TODO: Convert selectedImageUri to Bitmap if needed
            postViewModel.createTool(name, priceText, description, 
                    selectedLatitude, selectedLongitude, null);
            
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter a valid price", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void clearForm() {
        binding.editTextName.setText("");
        binding.editTextPrice.setText("");
        binding.editTextDescription.setText("");
        selectedImageUri = null;
        selectedAddress = "";
        selectedLatitude = 0.0;
        selectedLongitude = 0.0;
        
        binding.imagePreview.setVisibility(View.GONE);
        binding.imageUploadCard.setVisibility(View.VISIBLE);
        binding.textSelectedLocation.setVisibility(View.GONE);
        binding.textLocationPrompt.setVisibility(View.VISIBLE);
        
        validateForm();
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