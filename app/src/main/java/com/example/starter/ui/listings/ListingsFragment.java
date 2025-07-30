package com.example.starter.ui.listings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.starter.databinding.FragmentListingsBinding;

public class ListingsFragment extends Fragment {

    private FragmentListingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ListingsViewModel listingsViewModel =
                new ViewModelProvider(this).get(ListingsViewModel.class);

        binding = FragmentListingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textListings;
        listingsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 