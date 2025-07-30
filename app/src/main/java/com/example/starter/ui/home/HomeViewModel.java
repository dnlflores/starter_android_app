package com.example.starter.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.starter.model.Tool;
import com.example.starter.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final MutableLiveData<List<Tool>> mTools;
    private final MutableLiveData<Boolean> mIsLoading;
    private final MutableLiveData<String> mError;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mTools = new MutableLiveData<>();
        mIsLoading = new MutableLiveData<>();
        mError = new MutableLiveData<>();

        mIsLoading.setValue(false);
        
        loadTools();
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<List<Tool>> getTools() {
        return mTools;
    }

    public LiveData<Boolean> getIsLoading() {
        return mIsLoading;
    }

    public LiveData<String> getError() {
        return mError;
    }

    public void loadTools() {
        mIsLoading.setValue(true);
        mError.setValue(null);

        Call<List<Tool>> call = ApiClient.getApiService().getTools();
        call.enqueue(new Callback<List<Tool>>() {
            @Override
            public void onResponse(Call<List<Tool>> call, Response<List<Tool>> response) {
                mIsLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    mTools.setValue(response.body());
                } else {
                    mError.setValue("Failed to load tools");
                }
            }

            @Override
            public void onFailure(Call<List<Tool>> call, Throwable t) {
                mIsLoading.setValue(false);
                mError.setValue("Network error: " + t.getMessage());
            }
        });
    }

    public void retry() {
        loadTools();
    }
}