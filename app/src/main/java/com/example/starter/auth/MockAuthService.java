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
    
    public static void login(String email, String password, AuthCallback<LoginResponse> callback) {
        Log.d(TAG, "Mock login attempt for: " + email);
        
        // Simulate network delay
        handler.postDelayed(() -> {
            // Mock validation
            if (email.isEmpty() || password.isEmpty()) {
                callback.onError("Email and password are required");
                return;
            }
            
            if (!email.contains("@")) {
                callback.onError("Invalid email format");
                return;
            }
            
            if (password.length() < 6) {
                callback.onError("Password must be at least 6 characters");
                return;
            }
            
            // Mock successful login
            User user = new User(1, email.split("@")[0], email, "John", "Doe");
            LoginResponse response = new LoginResponse();
            response.setUser(user);
            response.setToken("mock_token_" + System.currentTimeMillis());
            
            Log.d(TAG, "Mock login successful for: " + email);
            callback.onSuccess(response);
        }, 1500); // 1.5 second delay
    }
    
    public static void signUp(String firstName, String lastName, String email, String password, AuthCallback<SignUpResponse> callback) {
        Log.d(TAG, "Mock signup attempt for: " + email);
        
        // Simulate network delay
        handler.postDelayed(() -> {
            // Mock validation
            if (firstName.isEmpty() || lastName.isEmpty()) {
                callback.onError("First name and last name are required");
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
            
            // Mock successful signup
            SignUpResponse response = new SignUpResponse();
            response.setMessage("Account created successfully");
            response.setUserId((int) System.currentTimeMillis());
            
            Log.d(TAG, "Mock signup successful for: " + email);
            callback.onSuccess(response);
        }, 1500); // 1.5 second delay
    }
} 