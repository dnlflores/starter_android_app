package com.example.starter.ui.listings;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.starter.models.Tool;
import com.example.starter.network.NetworkManager;

import java.util.ArrayList;
import java.util.List;

public class ListingsViewModel extends AndroidViewModel {

    private final NetworkManager networkManager;
    
    private MutableLiveData<List<Tool>> tools = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ListingsViewModel(Application application) {
        super(application);
        this.networkManager = NetworkManager.getInstance(application);
    }

    // Getters for LiveData
    public LiveData<List<Tool>> getTools() { return tools; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadUserTools() {
        isLoading.setValue(true);
        
        // Using fetchTools() since getUserTools() doesn't exist in NetworkManager
        networkManager.fetchTools(new NetworkManager.NetworkCallback<List<Tool>>() {
            @Override
            public void onSuccess(List<Tool> result) {
                isLoading.setValue(false);
                tools.setValue(result != null ? result : new ArrayList<>());
                errorMessage.setValue(null);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to load tools: " + error);
                tools.setValue(new ArrayList<>());
            }
        });
    }

    public void refreshTools() {
        loadUserTools();
    }
} 