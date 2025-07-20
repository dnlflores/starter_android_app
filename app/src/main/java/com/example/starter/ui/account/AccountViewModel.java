package com.example.starter.ui.account;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.starter.network.NetworkManager;

public class AccountViewModel extends AndroidViewModel {

    private final NetworkManager networkManager;
    
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> logoutSuccess = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AccountViewModel(Application application) {
        super(application);
        this.networkManager = NetworkManager.getInstance(application);
    }

    // Getters for LiveData
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getLogoutSuccess() { return logoutSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void logout() {
        isLoading.setValue(true);
        
        // NetworkManager's logout() method doesn't use callbacks, it just clears local data
        try {
            networkManager.logout();
            isLoading.setValue(false);
            logoutSuccess.setValue(true);
            errorMessage.setValue(null);
        } catch (Exception e) {
            isLoading.setValue(false);
            errorMessage.setValue("Failed to logout: " + e.getMessage());
            logoutSuccess.setValue(false);
        }
    }
} 