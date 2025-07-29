package com.example.starter.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.starter.R;
import com.example.starter.models.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {

    private List<Tool> properties = new ArrayList<>();
    private OnPropertyClickListener listener;

    public interface OnPropertyClickListener {
        void onPropertyClick(Tool property);
    }

    public PropertyAdapter(OnPropertyClickListener listener) {
        this.listener = listener;
    }

    public void setProperties(List<Tool> properties) {
        this.properties = properties;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_property_card, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        Tool property = properties.get(position);
        holder.bind(property);
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    class PropertyViewHolder extends RecyclerView.ViewHolder {
        private ImageView propertyImage;
        private ImageView favoriteButton;
        private TextView propertyLocation;
        private TextView propertyRating;
        private TextView propertyDistance;
        private TextView propertyDates;
        private TextView propertyPrice;

        public PropertyViewHolder(@NonNull View itemView) {
            super(itemView);
            propertyImage = itemView.findViewById(R.id.property_image);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
            propertyLocation = itemView.findViewById(R.id.property_location);
            propertyRating = itemView.findViewById(R.id.property_rating);
            propertyDistance = itemView.findViewById(R.id.property_distance);
            propertyDates = itemView.findViewById(R.id.property_dates);
            propertyPrice = itemView.findViewById(R.id.property_price);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onPropertyClick(properties.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Tool property) {
            // Set location (using property name as location for this demo)
            propertyLocation.setText(property.getName());

            // Generate a random rating for demo purposes
            Random random = new Random(property.getId());
            double rating = 4.5 + (random.nextDouble() * 0.5); // Rating between 4.5 and 5.0
            propertyRating.setText(String.format("%.2f", rating));

            // Generate distance (using owner info)
            int distance = 500 + random.nextInt(3000); // Random distance between 500-3500km
            propertyDistance.setText(distance + " kilometers away");

            // Generate random dates
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            String[] dates = {"1 – 6", "7 – 12", "15 – 20", "22 – 27", "3 – 8", "10 – 15", "18 – 23", "25 – 30"};
            String randomMonth = months[random.nextInt(months.length)];
            String randomDate = dates[random.nextInt(dates.length)];
            propertyDates.setText(randomMonth + " " + randomDate);

            // Set price
            propertyPrice.setText(property.getPrice());

            // For now, use placeholder image
            // In a real app, you would load the image using Glide or Picasso
            // Glide.with(itemView.getContext()).load(property.getImageUrl()).into(propertyImage);
            propertyImage.setImageResource(R.drawable.placeholder_tool);
        }
    }
} 