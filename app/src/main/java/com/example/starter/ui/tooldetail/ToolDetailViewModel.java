package com.example.starter.ui.tooldetail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.starter.model.Tool;
import com.example.starter.network.ApiClient;
import com.example.starter.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ToolDetailViewModel extends ViewModel {
    private final MutableLiveData<Tool> tool = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    
    private final ApiService apiService;

    public ToolDetailViewModel() {
        apiService = ApiClient.getApiService();
        isLoading.setValue(false);
    }

    public LiveData<Tool> getTool() {
        return tool;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadTool(int toolId) {
        isLoading.setValue(true);
        error.setValue(null);

        Call<Tool> call = apiService.getTool(toolId);
        call.enqueue(new Callback<Tool>() {
            @Override
            public void onResponse(Call<Tool> call, Response<Tool> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    tool.setValue(response.body());
                } else {
                    error.setValue("Failed to load tool details");
                }
            }

            @Override
            public void onFailure(Call<Tool> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue("Network error: " + t.getMessage());
            }
        });
    }

    public void retry(int toolId) {
        loadTool(toolId);
    }
}