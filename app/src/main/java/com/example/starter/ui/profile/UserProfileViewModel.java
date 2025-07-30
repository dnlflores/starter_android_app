package com.example.starter.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.starter.model.User;
import com.example.starter.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileViewModel extends ViewModel {
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public UserProfileViewModel() {
        isLoading.setValue(false);
    }

    public LiveData<User> getUser() {
        return user;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadUser(int userId) {
        isLoading.setValue(true);
        error.setValue(null);

        Call<User> call = ApiClient.getApiService().getUser(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    user.setValue(response.body());
                } else {
                    error.setValue("Failed to load user profile");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue("Network error: " + t.getMessage());
            }
        });
    }

    public void retry(int userId) {
        loadUser(userId);
    }
} 