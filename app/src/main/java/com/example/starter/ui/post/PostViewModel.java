package com.example.starter.ui.post;

import android.graphics.Bitmap;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.starter.model.Tool;

public class PostViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final MutableLiveData<Bitmap> selectedImage;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;

    public PostViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Create New Listing");
        
        selectedImage = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<Bitmap> getSelectedImage() {
        return selectedImage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setSelectedImage(Bitmap image) {
        selectedImage.setValue(image);
    }

    public void createTool(String name, String description, String address, double price) {
        isLoading.setValue(true);
        
        // Create a new Tool object
        Tool tool = new Tool();
        tool.setName(name);
        tool.setDescription(description);
        tool.setPrice(price);
        // Note: owner_id will be set when we have user authentication
        
        // TODO: Send tool to backend API
        // For now, we'll just simulate success
        
        // Simulate API call delay
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulate network delay
                
                // On main thread, update UI
                isLoading.postValue(false);
                
                // TODO: Handle success/error from API
                
            } catch (InterruptedException e) {
                isLoading.postValue(false);
                errorMessage.postValue("Error creating tool: " + e.getMessage());
            }
        }).start();
    }

    public void clearForm() {
        selectedImage.setValue(null);
        errorMessage.setValue(null);
    }
} 