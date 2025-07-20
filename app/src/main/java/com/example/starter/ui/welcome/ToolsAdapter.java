package com.example.starter.ui.welcome;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.starter.R;
import com.example.starter.models.Tool;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ToolsAdapter extends RecyclerView.Adapter<ToolsAdapter.ToolViewHolder> {

    private List<Tool> tools = new ArrayList<>();
    private OnToolClickListener listener;
    private Location userLocation;

    public interface OnToolClickListener {
        void onToolClick(Tool tool);
    }

    public ToolsAdapter(OnToolClickListener listener) {
        this.listener = listener;
    }

    public void updateTools(List<Tool> newTools) {
        this.tools.clear();
        this.tools.addAll(newTools);
        notifyDataSetChanged();
    }

    public void setTools(List<Tool> newTools) {
        updateTools(newTools); // Alias for updateTools() for compatibility
    }

    public void setUserLocation(Location location) {
        this.userLocation = location;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ToolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tool_card, parent, false);
        return new ToolViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToolViewHolder holder, int position) {
        Tool tool = tools.get(position);
        holder.bind(tool, userLocation, listener);
    }

    @Override
    public int getItemCount() {
        return tools.size();
    }

    static class ToolViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView toolImage;
        private final TextView toolName;
        private final TextView toolDescription;
        private final TextView toolPrice;
        private final TextView ownerName;
        private final TextView distance;

        public ToolViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_tool);
            toolImage = itemView.findViewById(R.id.iv_tool_image);
            toolName = itemView.findViewById(R.id.tv_tool_name);
            toolDescription = itemView.findViewById(R.id.tv_tool_description);
            toolPrice = itemView.findViewById(R.id.tv_tool_price);
            ownerName = itemView.findViewById(R.id.tv_owner_name);
            distance = itemView.findViewById(R.id.tv_distance);
        }

        public void bind(Tool tool, Location userLocation, OnToolClickListener listener) {
            toolName.setText(tool.getName());
            
            // Set description
            if (tool.getDescription() != null && !tool.getDescription().isEmpty()) {
                toolDescription.setText(tool.getDescription());
                toolDescription.setVisibility(View.VISIBLE);
            } else {
                toolDescription.setVisibility(View.GONE);
            }

            // Format and set price
            toolPrice.setText(formatPrice(tool.getPrice()));

            // Set owner name
            if (tool.getOwnerUsername() != null) {
                ownerName.setText("by " + tool.getOwnerUsername());
                ownerName.setVisibility(View.VISIBLE);
            } else {
                ownerName.setVisibility(View.GONE);
            }

            // Set distance if available
            String distanceText = calculateDistanceText(userLocation, tool);
            if (distanceText != null) {
                distance.setText(distanceText);
                distance.setVisibility(View.VISIBLE);
            } else {
                distance.setVisibility(View.GONE);
            }

            // Load image
            loadToolImage(tool);

            // Set click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToolClick(tool);
                }
            });
        }

        private String formatPrice(String priceString) {
            try {
                double price = Double.parseDouble(priceString);
                NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
                return formatter.format(price) + "/day";
            } catch (NumberFormatException e) {
                return "$" + priceString + "/day";
            }
        }

        private String calculateDistanceText(Location userLocation, Tool tool) {
            if (userLocation == null || tool.getLatitude() == null || tool.getLongitude() == null) {
                return null;
            }

            Location toolLocation = new Location("");
            toolLocation.setLatitude(tool.getLatitude());
            toolLocation.setLongitude(tool.getLongitude());

            float distanceMeters = userLocation.distanceTo(toolLocation);
            double distanceMiles = distanceMeters * 0.000621371; // Convert to miles

            if (distanceMiles < 0.1) {
                return "< 0.1 mi";
            } else if (distanceMiles < 1.0) {
                return String.format(Locale.US, "%.1f mi", distanceMiles);
            } else {
                return String.format(Locale.US, "%.0f mi", distanceMiles);
            }
        }

        private void loadToolImage(Tool tool) {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.placeholder_tool)
                    .error(R.drawable.error_tool)
                    .transform(new RoundedCorners(16));

            if (tool.getImageUrl() != null && !tool.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(tool.getImageUrl())
                        .apply(options)
                        .into(toolImage);
            } else {
                Glide.with(itemView.getContext())
                        .load(R.drawable.placeholder_tool)
                        .apply(options)
                        .into(toolImage);
            }
        }
    }
} 