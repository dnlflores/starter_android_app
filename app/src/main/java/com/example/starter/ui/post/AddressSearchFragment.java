package com.example.starter.ui.post;

import android.content.Context;
import android.location.Geocoder;
import android.location.Address;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.starter.R;
import com.example.starter.databinding.FragmentAddressSearchBinding;
import com.example.starter.model.AddressResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddressSearchFragment extends Fragment implements AddressSearchAdapter.OnAddressClickListener {

    private FragmentAddressSearchBinding binding;
    private AddressSearchAdapter adapter;
    private ExecutorService executorService;

    public static final String ARG_ADDRESS_RESULT = "address_result";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddressSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        executorService = Executors.newSingleThreadExecutor();
        setupRecyclerView();
        setupClickListeners();
        setupTextWatcher();
        
        // Focus on the search input and show keyboard
        binding.etSearchAddress.requestFocus();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(binding.etSearchAddress, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void setupRecyclerView() {
        adapter = new AddressSearchAdapter(this);
        binding.rvAddressResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAddressResults.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    private void setupTextWatcher() {
        binding.etSearchAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.length() >= 3) {
                    searchAddresses(query);
                } else {
                    adapter.setAddresses(new ArrayList<>());
                    showNoResults(false);
                }
            }
        });
    }

    private void searchAddresses(String query) {
        showLoading(true);
        showNoResults(false);

        executorService.execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(query, 10);

                List<AddressResult> results = new ArrayList<>();
                for (Address address : addresses) {
                    String title = getAddressTitle(address);
                    String subtitle = getAddressSubtitle(address);
                    String fullAddress = getFullAddress(address);
                    
                    AddressResult result = new AddressResult(
                        title, 
                        subtitle, 
                        address.getLatitude(), 
                        address.getLongitude(), 
                        fullAddress
                    );
                    results.add(result);
                }

                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    adapter.setAddresses(results);
                    showNoResults(results.isEmpty());
                });

            } catch (IOException e) {
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    showNoResults(true);
                    Toast.makeText(requireContext(), getString(R.string.error_geocoding), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String getAddressTitle(Address address) {
        StringBuilder title = new StringBuilder();
        
        if (address.getThoroughfare() != null) {
            title.append(address.getThoroughfare());
        }
        
        if (address.getSubThoroughfare() != null) {
            title.insert(0, address.getSubThoroughfare() + " ");
        }
        
        if (title.length() == 0 && address.getFeatureName() != null) {
            title.append(address.getFeatureName());
        }
        
        return title.toString();
    }

    private String getAddressSubtitle(Address address) {
        StringBuilder subtitle = new StringBuilder();
        
        if (address.getLocality() != null) {
            subtitle.append(address.getLocality());
        }
        
        if (address.getAdminArea() != null) {
            if (subtitle.length() > 0) subtitle.append(", ");
            subtitle.append(address.getAdminArea());
        }
        
        if (address.getPostalCode() != null) {
            if (subtitle.length() > 0) subtitle.append(" ");
            subtitle.append(address.getPostalCode());
        }
        
        return subtitle.toString();
    }

    private String getFullAddress(Address address) {
        StringBuilder fullAddress = new StringBuilder();
        
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            if (address.getAddressLine(i) != null) {
                if (fullAddress.length() > 0) fullAddress.append(", ");
                fullAddress.append(address.getAddressLine(i));
            }
        }
        
        return fullAddress.toString();
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showNoResults(boolean show) {
        binding.tvNoResults.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onAddressClick(AddressResult address) {
        // Pass the result back to the previous fragment using Navigation Component
        Bundle result = new Bundle();
        result.putParcelable(ARG_ADDRESS_RESULT, address);
        getParentFragmentManager().setFragmentResult(ARG_ADDRESS_RESULT, result);
        
        Navigation.findNavController(requireView()).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        binding = null;
    }
} 