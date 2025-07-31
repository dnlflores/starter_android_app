package com.example.starter.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.starter.R;
import com.example.starter.auth.AuthManager;

public abstract class AuthAwareFragment extends Fragment implements AuthManager.AuthStateListener {

    protected AuthManager authManager;
    protected boolean isAuthenticated = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authManager = AuthManager.getInstance(requireContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Check authentication status
        isAuthenticated = authManager.isLoggedIn();
        
        if (!isAuthenticated) {
            showAuthRequired();
        } else {
            showAuthenticatedContent();
        }
        
        // Register for auth state changes
        authManager.setAuthStateListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        authManager.removeAuthStateListener();
    }

    @Override
    public void onAuthStateChanged(boolean isLoggedIn, com.example.starter.model.User user) {
        isAuthenticated = isLoggedIn;
        
        if (isLoggedIn) {
            showAuthenticatedContent();
        } else {
            showAuthRequired();
        }
    }

    protected void showAuthRequired() {
        // Navigate to auth splash fragment with appropriate tab type
        if (getView() != null) {
            AuthSplashFragment.TabType tabType = getTabType();
            Bundle args = new Bundle();
            args.putString("tab_type", tabType.name());
            Navigation.findNavController(getView()).navigate(R.id.navigation_auth_splash, args);
        }
    }
    
    protected AuthSplashFragment.TabType getTabType() {
        // Default to CHAT - subclasses should override this
        return AuthSplashFragment.TabType.CHAT;
    }

    protected void showAuthenticatedContent() {
        // This will be implemented by subclasses
        // The actual content will be shown when the fragment is created
    }

    protected boolean isUserAuthenticated() {
        return isAuthenticated;
    }
} 