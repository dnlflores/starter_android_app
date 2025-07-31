package com.example.starter.ui.auth;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.starter.auth.AuthManager;
import com.example.starter.auth.MockAuthService;
import com.example.starter.model.User;
import com.example.starter.network.ApiClient;
import com.example.starter.network.ApiService;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>(false);
    private final ApiService apiService;
    private final AuthManager authManager;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        apiService = ApiClient.getClient().create(ApiService.class);
        authManager = AuthManager.getInstance(application);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public void login(String username, String password) {
        isLoading.setValue(true);
        error.setValue(null);

        // Use mock service for testing
        MockAuthService.login(username, password, new MockAuthService.AuthCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse response) {
                isLoading.setValue(false);
                User user = response.getUser();
                String token = response.getToken();
                
                // Save user session
                authManager.login(user, token);
                loginSuccess.setValue(true);
                
                Log.d("LoginViewModel", "Login successful for user: " + user.getUsername());
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.setValue(false);
                error.setValue(errorMessage);
                Log.e("LoginViewModel", "Login failed: " + errorMessage);
            }
        });
    }

    // Response class for login API
    public static class LoginResponse {
        private User user;
        private String token;

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
} 