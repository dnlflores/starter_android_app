package com.example.starter.ui.post;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.starter.models.Tool;
import com.example.starter.network.NetworkManager;

public class PostViewModel extends AndroidViewModel {
    private final NetworkManager networkManager;
    
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> successMessage = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public PostViewModel(Application application) {
        super(application);
        this.networkManager = NetworkManager.getInstance(application);
    }

    // Getters for LiveData
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getSuccessMessage() { return successMessage; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void createTool(String name, String price, String description, 
                          Double latitude, Double longitude, Bitmap image) {
        
        isLoading.setValue(true);
        
        networkManager.createTool(name, price, description, 1, // defaulting ownerId to 1 for now
                latitude, longitude, image, new NetworkManager.NetworkCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoading.setValue(false);
                if (result) {
                    successMessage.setValue("Tool listing created successfully!");
                    errorMessage.setValue(null);
                } else {
                    errorMessage.setValue("Failed to create tool listing");
                }
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Error: " + error);
            }
        });
    }
} 