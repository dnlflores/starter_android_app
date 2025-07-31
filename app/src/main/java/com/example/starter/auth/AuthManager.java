package com.example.starter.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.starter.model.User;
import com.google.gson.Gson;

public class AuthManager {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_USER = "current_user";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    private static AuthManager instance;
    private SharedPreferences prefs;
    private User currentUser;
    private String authToken;
    private boolean isLoggedIn;
    private AuthStateListener authStateListener;
    
    private AuthManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadAuthState();
    }
    
    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context.getApplicationContext());
        }
        return instance;
    }
    
    private void loadAuthState() {
        isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        authToken = prefs.getString(KEY_TOKEN, null);
        
        String userJson = prefs.getString(KEY_USER, null);
        if (userJson != null) {
            try {
                currentUser = new Gson().fromJson(userJson, User.class);
            } catch (Exception e) {
                Log.e("AuthManager", "Error loading user data", e);
                currentUser = null;
            }
        }
    }
    
    public void login(User user, String token) {
        this.currentUser = user;
        this.authToken = token;
        this.isLoggedIn = true;
        
        // Save to SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER, new Gson().toJson(user));
        editor.apply();
        
        // Notify listeners
        if (authStateListener != null) {
            authStateListener.onAuthStateChanged(true, user);
        }
        
        Log.d("AuthManager", "User logged in: " + user.getUsername());
    }
    
    public void logout() {
        this.currentUser = null;
        this.authToken = null;
        this.isLoggedIn = false;
        
        // Clear SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        
        // Notify listeners
        if (authStateListener != null) {
            authStateListener.onAuthStateChanged(false, null);
        }
        
        Log.d("AuthManager", "User logged out");
    }
    
    public boolean isLoggedIn() {
        return isLoggedIn && currentUser != null;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public void setAuthStateListener(AuthStateListener listener) {
        this.authStateListener = listener;
    }
    
    public void removeAuthStateListener() {
        this.authStateListener = null;
    }
    
    public interface AuthStateListener {
        void onAuthStateChanged(boolean isLoggedIn, User user);
    }
} 