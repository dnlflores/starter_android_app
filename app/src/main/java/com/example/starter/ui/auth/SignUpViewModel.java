package com.example.starter.ui.auth;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.starter.auth.MockAuthService;
import com.example.starter.network.ApiClient;
import com.example.starter.network.ApiService;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> signUpSuccess = new MutableLiveData<>(false);
    private final ApiService apiService;

    public SignUpViewModel(@NonNull Application application) {
        super(application);
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getSignUpSuccess() {
        return signUpSuccess;
    }

    public void signUp(String firstName, String lastName, String email, String password) {
        isLoading.setValue(true);
        error.setValue(null);

        // Use mock service for testing
        MockAuthService.signUp(firstName, lastName, email, password, new MockAuthService.AuthCallback<SignUpResponse>() {
            @Override
            public void onSuccess(SignUpResponse response) {
                isLoading.setValue(false);
                signUpSuccess.setValue(true);
                
                Log.d("SignUpViewModel", "Sign up successful for user: " + email);
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.setValue(false);
                error.setValue(errorMessage);
                Log.e("SignUpViewModel", "Sign up failed: " + errorMessage);
            }
        });
    }

    // Response class for signup API
    public static class SignUpResponse {
        private String message;
        private int userId;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }
    }
} 