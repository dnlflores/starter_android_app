package com.example.starter.ui.tooldetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.starter.R;
import com.example.starter.model.Tool;

import java.util.Locale;

public class ToolDetailFragment extends Fragment {
    
    private static final String ARG_TOOL_ID = "tool_id";
    
    private ToolDetailViewModel viewModel;
    private ImageView toolImage;
    private TextView toolName;
    private TextView toolPrice;
    private TextView toolDescription;
    private TextView toolOwner;
    private ProgressBar progressBar;
    private TextView errorText;
    
    private int toolId;

    public static ToolDetailFragment newInstance(int toolId) {
        ToolDetailFragment fragment = new ToolDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TOOL_ID, toolId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            toolId = getArguments().getInt(ARG_TOOL_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tool_detail, container, false);
        
        initViews(root);
        setupViewModel();
        observeViewModel();
        
        // Load tool details
        viewModel.loadTool(toolId);
        
        return root;
    }

    private void initViews(View root) {
        toolImage = root.findViewById(R.id.tool_image);
        toolName = root.findViewById(R.id.tool_name);
        toolPrice = root.findViewById(R.id.tool_price);
        toolDescription = root.findViewById(R.id.tool_description);
        toolOwner = root.findViewById(R.id.tool_owner);
        progressBar = root.findViewById(R.id.progress_bar);
        errorText = root.findViewById(R.id.error_text);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ToolDetailViewModel.class);
    }

    private void observeViewModel() {
        viewModel.getTool().observe(getViewLifecycleOwner(), this::displayTool);
        
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            errorText.setVisibility(error != null ? View.VISIBLE : View.GONE);
            if (error != null) {
                errorText.setText(error);
                // Add click listener to retry
                errorText.setOnClickListener(v -> viewModel.retry(toolId));
            }
        });
    }

    private void displayTool(Tool tool) {
        if (tool == null) return;
        
        toolName.setText(tool.getName());
        toolPrice.setText(String.format(Locale.getDefault(), "$%.2f", tool.getPrice()));
        toolDescription.setText(tool.getDescription());
        toolOwner.setText(tool.getOwnerUsername());
        
        // Load tool image
        loadToolImage(tool.getImageUrl());
    }
    
    private void loadToolImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .centerCrop()
                    .into(toolImage);
        } else {
            toolImage.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }
}