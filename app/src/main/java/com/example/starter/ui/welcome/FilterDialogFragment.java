package com.example.starter.ui.welcome;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.starter.R;
import com.google.android.material.button.MaterialButton;

public class FilterDialogFragment extends DialogFragment {

    private static final String ARG_MIN_PRICE = "min_price";
    private static final String ARG_MAX_PRICE = "max_price";
    private static final String ARG_DISTANCE_RANGE = "distance_range";

    private SeekBar minPriceSeekBar;
    private SeekBar maxPriceSeekBar;
    private TextView minPriceText;
    private TextView maxPriceText;
    private RadioGroup distanceRadioGroup;
    private MaterialButton applyButton;
    private MaterialButton resetButton;

    private double minPrice;
    private double maxPrice;
    private WelcomeFragment.DistanceRange distanceRange;
    private FilterListener filterListener;

    public interface FilterListener {
        void onFiltersApplied(double minPrice, double maxPrice, WelcomeFragment.DistanceRange distanceRange);
        void onFiltersReset();
    }

    public static FilterDialogFragment newInstance(double minPrice, double maxPrice, 
                                                 WelcomeFragment.DistanceRange distanceRange) {
        FilterDialogFragment fragment = new FilterDialogFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_MIN_PRICE, minPrice);
        args.putDouble(ARG_MAX_PRICE, maxPrice);
        args.putString(ARG_DISTANCE_RANGE, distanceRange.name());
        fragment.setArguments(args);
        return fragment;
    }

    public void setFilterListener(FilterListener listener) {
        this.filterListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() != null) {
            minPrice = getArguments().getDouble(ARG_MIN_PRICE, 10.0);
            maxPrice = getArguments().getDouble(ARG_MAX_PRICE, 10000.0);
            String distanceRangeName = getArguments().getString(ARG_DISTANCE_RANGE, 
                    WelcomeFragment.DistanceRange.ALL.name());
            distanceRange = WelcomeFragment.DistanceRange.valueOf(distanceRangeName);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_filter, null);

        initViews(view);
        setupViews();
        setupClickListeners();

        builder.setView(view).setTitle("Filter Tools");
        return builder.create();
    }

    private void initViews(View view) {
        minPriceSeekBar = view.findViewById(R.id.seekbar_min_price);
        maxPriceSeekBar = view.findViewById(R.id.seekbar_max_price);
        minPriceText = view.findViewById(R.id.tv_min_price);
        maxPriceText = view.findViewById(R.id.tv_max_price);
        distanceRadioGroup = view.findViewById(R.id.radio_group_distance);
        applyButton = view.findViewById(R.id.btn_apply_filters);
        resetButton = view.findViewById(R.id.btn_reset_filters);
    }

    private void setupViews() {
        minPriceSeekBar.setMax(999);
        maxPriceSeekBar.setMax(999);
        
        minPriceSeekBar.setProgress((int) ((minPrice - 10) / 10));
        maxPriceSeekBar.setProgress((int) ((maxPrice - 10) / 10));
        
        updatePriceTexts();
        setupDistanceRadioButtons();
    }

    private void setupDistanceRadioButtons() {
        distanceRadioGroup.removeAllViews();
        
        WelcomeFragment.DistanceRange[] ranges = WelcomeFragment.DistanceRange.values();
        for (WelcomeFragment.DistanceRange range : ranges) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(range.title);
            radioButton.setTag(range);
            radioButton.setChecked(range == distanceRange);
            distanceRadioGroup.addView(radioButton);
        }
    }

    private void setupClickListeners() {
        minPriceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    minPrice = 10 + (progress * 10);
                    if (minPrice > maxPrice) {
                        maxPrice = minPrice;
                        maxPriceSeekBar.setProgress((int) ((maxPrice - 10) / 10));
                    }
                    updatePriceTexts();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        maxPriceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    maxPrice = 10 + (progress * 10);
                    if (maxPrice < minPrice) {
                        minPrice = maxPrice;
                        minPriceSeekBar.setProgress((int) ((minPrice - 10) / 10));
                    }
                    updatePriceTexts();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        distanceRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedButton = group.findViewById(checkedId);
            if (selectedButton != null) {
                distanceRange = (WelcomeFragment.DistanceRange) selectedButton.getTag();
            }
        });

        applyButton.setOnClickListener(v -> {
            if (filterListener != null) {
                filterListener.onFiltersApplied(minPrice, maxPrice, distanceRange);
            }
            dismiss();
        });

        resetButton.setOnClickListener(v -> {
            if (filterListener != null) {
                filterListener.onFiltersReset();
            }
            dismiss();
        });
    }

    private void updatePriceTexts() {
        minPriceText.setText(String.format("Min: $%.0f", minPrice));
        maxPriceText.setText(String.format("Max: $%.0f", maxPrice));
    }
} 