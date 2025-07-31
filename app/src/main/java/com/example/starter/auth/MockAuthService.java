package com.example.starter.auth;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.starter.model.User;
import com.example.starter.ui.auth.LoginViewModel.LoginResponse;
import com.example.starter.ui.auth.SignUpViewModel.SignUpResponse;

public class MockAuthService {
    
    private static final String TAG = "MockAuthService";
    private static final Handler handler = new Handler(Looper.getMainLooper());
    
    public interface AuthCallback<T> {
        void onSuccess(T response);
        void onError(String error);
    }
    
    public static void login(String username, String password, AuthCallback<LoginResponse> callback) {
        Log.d(TAG, "Mock login attempt for: " + username);
        
        // Simulate network delay
        handler.postDelayed(() -> {
            // Mock validation
            if (username.isEmpty() || password.isEmpty()) {
                callback.onError("Username and password are required");
                return;
            }
            
            if (password.length() < 6) {
                callback.onError("Password must be at least 6 characters");
                return;
            }
            
            // Mock successful login
            User user = new User(1, username, username + "@example.com", "John", "Doe");
            LoginResponse response = new LoginResponse();
            response.setUser(user);
            response.setToken("mock_token_" + System.currentTimeMillis());
            
            Log.d(TAG, "Mock login successful for: " + username);
            callback.onSuccess(response);
        }, 1500); // 1.5 second delay
    }
    
    public static void signUp(String username, String email, String password, String streetAddress, String city, String state, String zipCode, String phone, AuthCallback<SignUpResponse> callback) {
        Log.d(TAG, "Mock signup attempt for: " + username);
        
        // Simulate network delay
        handler.postDelayed(() -> {
            // Mock validation
            if (username.isEmpty()) {
                callback.onError("Username is required");
                return;
            }
            
            if (email.isEmpty() || !email.contains("@")) {
                callback.onError("Valid email is required");
                return;
            }
            
            if (password.length() < 6) {
                callback.onError("Password must be at least 6 characters");
                return;
            }
            
            if (streetAddress.isEmpty() || city.isEmpty() || state.isEmpty() || zipCode.isEmpty() || phone.isEmpty()) {
                callback.onError("All address fields are required");
                return;
            }
            
            // Mock successful signup
            SignUpResponse response = new SignUpResponse();
            response.setMessage("Account created successfully");
            response.setUserId((int) System.currentTimeMillis());
            
            Log.d(TAG, "Mock signup successful for: " + username);
            callback.onSuccess(response);
        }, 1500); // 1.5 second delay
    }
} 