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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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
        private final androidx.cardview.widget.CardView cardView;
        private final ImageView toolImage;
        private final ImageView favoriteIcon;
        private final TextView toolName;
        private final TextView toolRating;
        private final TextView toolPrice;
        private final TextView ownerName;
        private final TextView distance;

        public ToolViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_tool);
            toolImage = itemView.findViewById(R.id.iv_tool_image);
            favoriteIcon = itemView.findViewById(R.id.iv_favorite);
            toolName = itemView.findViewById(R.id.tv_tool_name);
            toolRating = itemView.findViewById(R.id.tv_rating);
            toolPrice = itemView.findViewById(R.id.tv_tool_price);
            ownerName = itemView.findViewById(R.id.tv_owner_name);
            distance = itemView.findViewById(R.id.tv_distance);
        }

        public void bind(Tool tool, Location userLocation, OnToolClickListener listener) {
            toolName.setText(tool.getName());

            // Generate and set a random rating (4.0-5.0)
            Random random = new Random(tool.getId());
            double rating = 4.0 + (random.nextDouble() * 1.0);
            toolRating.setText(String.format(Locale.US, "%.2f", rating));

            // Format and set price (clean format without "/day" in the main text)
            String cleanPrice = formatPrice(tool.getPrice());
            toolPrice.setText(cleanPrice);

            // Set owner name in tool rental style
            if (tool.getOwnerFullName() != null && !tool.getOwnerFullName().isEmpty()) {
                ownerName.setText("Owned by " + tool.getOwnerFullName());
                ownerName.setVisibility(View.VISIBLE);
            } else if (tool.getOwnerUsername() != null) {
                ownerName.setText("Owned by " + tool.getOwnerUsername());
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
                // Generate random distance for demo
                int randomDistance = 500 + random.nextInt(3000);
                distance.setText(randomDistance + " meters away");
                distance.setVisibility(View.VISIBLE);
            }

            // Load image
            loadToolImage(tool);

            // Set click listeners
            View.OnClickListener clickListener = v -> {
                if (listener != null) {
                    listener.onToolClick(tool);
                }
            };
            
            cardView.setOnClickListener(clickListener);
            itemView.setOnClickListener(clickListener);
        }

        private String formatPrice(String priceString) {
            try {
                // Remove any existing currency symbols and extra text
                String cleanPrice = priceString.replaceAll("[^0-9.]", "");
                double price = Double.parseDouble(cleanPrice);
                return "$" + String.format(Locale.US, "%.0f", price);
            } catch (NumberFormatException e) {
                // If parsing fails, just add $ prefix
                return "$" + priceString.replaceAll("[^0-9.]", "");
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

            if (distanceMeters < 1000) {
                return String.format(Locale.US, "%.0f meters away", distanceMeters);
            } else {
                double distanceKm = distanceMeters / 1000.0;
                return String.format(Locale.US, "%.1f km away", distanceKm);
            }
        }

        private void loadToolImage(Tool tool) {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.placeholder_tool)
                    .error(R.drawable.error_tool)
                    .centerCrop();

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