package com.example.starter.ui.welcome;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.starter.models.Tool;

import java.util.List;

public class WelcomeViewModel extends ViewModel {

    private final MutableLiveData<List<Tool>> tools = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public WelcomeViewModel() {
        isLoading.setValue(false);
    }

    public LiveData<List<Tool>> getTools() {
        return tools;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setTools(List<Tool> toolsList) {
        tools.setValue(toolsList);
    }

    public void setLoading(boolean loading) {
        isLoading.setValue(loading);
    }

    public void setError(String error) {
        errorMessage.setValue(error);
    }

    public void clearError() {
        errorMessage.setValue(null);
    }
} 