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

public class AuthSplashFragment extends Fragment {

    private static final String ARG_TAB_TYPE = "tab_type";
    
    public enum TabType {
        CHAT,
        POST,
        LISTINGS,
        ACCOUNT
    }

    public static AuthSplashFragment newInstance(TabType tabType) {
        AuthSplashFragment fragment = new AuthSplashFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAB_TYPE, tabType.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TabType tabType = getTabType();
        int layoutResId = getLayoutForTabType(tabType);
        return inflater.inflate(layoutResId, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Set up click listeners
        view.findViewById(R.id.buttonLogIn).setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_auth_splash_to_login);
        });
        
        view.findViewById(R.id.buttonSignUp).setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_auth_splash_to_signup);
        });
    }

    private TabType getTabType() {
        String tabTypeString = getArguments() != null ? getArguments().getString(ARG_TAB_TYPE) : null;
        if (tabTypeString != null) {
            try {
                return TabType.valueOf(tabTypeString);
            } catch (IllegalArgumentException e) {
                // Default to CHAT if invalid
                return TabType.CHAT;
            }
        }
        return TabType.CHAT;
    }

    private int getLayoutForTabType(TabType tabType) {
        switch (tabType) {
            case CHAT:
                return R.layout.fragment_chat_auth_required;
            case POST:
                return R.layout.fragment_post_auth_required;
            case LISTINGS:
                return R.layout.fragment_listings_auth_required;
            case ACCOUNT:
                return R.layout.fragment_account_auth_required;
            default:
                return R.layout.fragment_chat_auth_required;
        }
    }
} 