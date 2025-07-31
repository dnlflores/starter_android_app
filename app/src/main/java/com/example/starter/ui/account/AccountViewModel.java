package com.example.starter.ui.account;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.starter.model.User;
import com.example.starter.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountViewModel extends ViewModel {

    private final MutableLiveData<User> mUser;
    private final MutableLiveData<Boolean> mIsLoading;
    private final MutableLiveData<String> mError;

    public AccountViewModel() {
        mUser = new MutableLiveData<>();
        mIsLoading = new MutableLiveData<>();
        mError = new MutableLiveData<>();
        
        mIsLoading.setValue(true);
        
        // Try to load from backend first, fallback to mock data
        loadUser(1);
    }

    public LiveData<User> getUser() {
        return mUser;
    }

    public LiveData<Boolean> getIsLoading() {
        return mIsLoading;
    }

    public LiveData<String> getError() {
        return mError;
    }

    private void loadMockUser() {
        // Simulate network delay
        new android.os.Handler().postDelayed(() -> {
            mIsLoading.setValue(false);
            
            // Create a mock user for demonstration
            User mockUser = new User(1, "john_doe", "john.doe@example.com", "John", "Doe");
            mUser.setValue(mockUser);
            mError.setValue(null);
        }, 1000); // 1 second delay to show loading state
    }

    public void loadUser(int userId) {
        mIsLoading.setValue(true);
        mError.setValue(null);

        // Try to load from backend first, fallback to mock data
        Call<User> call = ApiClient.getApiService().getUser(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                mIsLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    mUser.setValue(response.body());
                } else {
                    // Fallback to mock data if backend fails
                    loadMockUser();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                mIsLoading.setValue(false);
                // Fallback to mock data if network fails
                loadMockUser();
            }
        });
    }

    public void retry(int userId) {
        loadUser(userId);
    }
} 