package com.example.starter.ui.post;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.starter.R;
import com.example.starter.databinding.FragmentPostBinding;
import com.example.starter.ui.auth.AuthAwareFragment;
import com.example.starter.ui.auth.AuthSplashFragment;

import java.io.IOException;

public class PostFragment extends AuthAwareFragment {

    private FragmentPostBinding binding;
    private PostViewModel postViewModel;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);
        binding = FragmentPostBinding.inflate(inflater, container, false);
        
        setupActivityResultLaunchers();
        setupClickListeners();
        
        return binding.getRoot();
    }

    private void setupActivityResultLaunchers() {
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                        if (photo != null) {
                            postViewModel.setSelectedImage(photo);
                            updatePhotoButton();
                        }
                    }
                }
            }
        );

        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        if (selectedImage != null) {
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                    requireActivity().getContentResolver(), selectedImage);
                                postViewModel.setSelectedImage(bitmap);
                                updatePhotoButton();
                            } catch (IOException e) {
                                Toast.makeText(requireContext(), getString(R.string.error_loading_image), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        );

        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    showImageSourceDialog();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.camera_permission_required), Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void setupClickListeners() {
        binding.btnClose.setOnClickListener(v -> {
            // Navigate back or close the fragment
            requireActivity().onBackPressed();
        });

        binding.btnAddPhoto.setOnClickListener(v -> {
            checkCameraPermission();
        });

        binding.btnPublish.setOnClickListener(v -> {
            publishListing();
        });
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            showImageSourceDialog();
        }
    }

    private void showImageSourceDialog() {
        String[] options = {getString(R.string.camera), getString(R.string.gallery)};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.select_image_source));
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else {
                openGallery();
            }
        });
        builder.show();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private void updatePhotoButton() {
        if (postViewModel.getSelectedImage().getValue() != null) {
            binding.btnAddPhoto.setText(getString(R.string.photo_added));
        } else {
            binding.btnAddPhoto.setText(getString(R.string.add_photo));
        }
    }

    private void publishListing() {
        String toolName = binding.etToolName.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String priceText = binding.etPrice.getText().toString().trim();

        if (toolName.isEmpty()) {
            binding.etToolName.setError(getString(R.string.tool_name_required));
            return;
        }

        if (description.isEmpty()) {
            binding.etDescription.setError(getString(R.string.description_required));
            return;
        }

        if (address.isEmpty()) {
            binding.etAddress.setError(getString(R.string.address_required));
            return;
        }

        double price = 0.0;
        if (!priceText.isEmpty()) {
            try {
                price = Double.parseDouble(priceText);
            } catch (NumberFormatException e) {
                binding.etPrice.setError(getString(R.string.invalid_price_format));
                return;
            }
        }

        // Create the tool object and send to ViewModel
        postViewModel.createTool(toolName, description, address, price);
        
        Toast.makeText(requireContext(), getString(R.string.listing_published_successfully), Toast.LENGTH_SHORT).show();
        
        // Clear the form
        clearForm();
    }

    private void clearForm() {
        binding.etToolName.setText("");
        binding.etDescription.setText("");
        binding.etAddress.setText("");
        binding.etPrice.setText("");
        postViewModel.setSelectedImage(null);
        updatePhotoButton();
    }

    @Override
    protected void showAuthenticatedContent() {
        if (binding != null) {
            // Form is already visible, no additional setup needed
        }
    }
    
    @Override
    protected AuthSplashFragment.TabType getTabType() {
        return AuthSplashFragment.TabType.POST;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 