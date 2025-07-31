package com.example.starter.ui.listings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.example.starter.R;
import com.example.starter.databinding.FragmentListingsBinding;
import com.example.starter.ui.auth.AuthAwareFragment;
import com.example.starter.ui.auth.AuthSplashFragment;

public class ListingsFragment extends AuthAwareFragment {

    private FragmentListingsBinding binding;
    private ListingsViewModel listingsViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        listingsViewModel = new ViewModelProvider(this).get(ListingsViewModel.class);
        binding = FragmentListingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected void showAuthenticatedContent() {
        if (binding != null) {
            final TextView textView = binding.textListings;
            listingsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        }
    }
    
    @Override
    protected AuthSplashFragment.TabType getTabType() {
        return AuthSplashFragment.TabType.LISTINGS;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 