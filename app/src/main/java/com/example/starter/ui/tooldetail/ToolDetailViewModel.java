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

        android.util.Log.d("ToolDetailViewModel", "Loading tool with ID: " + toolId);
        Call<Tool> call = apiService.getTool(toolId);
        call.enqueue(new Callback<Tool>() {
            @Override
            public void onResponse(Call<Tool> call, Response<Tool> response) {
                isLoading.setValue(false);
                android.util.Log.d("ToolDetailViewModel", "Response received for tool ID " + toolId + ": " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    tool.setValue(response.body());
                    android.util.Log.d("ToolDetailViewModel", "Tool loaded successfully: " + response.body().getName());
                } else {
                    String errorMsg = "Failed to load tool details (HTTP " + response.code() + ")";
                    android.util.Log.e("ToolDetailViewModel", errorMsg);
                    error.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Tool> call, Throwable t) {
                isLoading.setValue(false);
                String errorMsg = "Network error: " + t.getMessage();
                android.util.Log.e("ToolDetailViewModel", errorMsg, t);
                error.setValue(errorMsg);
            }
        });
    }

    public void retry(int toolId) {
        loadTool(toolId);
    }
}