package com.example.starter.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// import com.bumptech.glide.Glide;
// import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.starter.R;
import com.example.starter.model.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ToolsAdapter extends RecyclerView.Adapter<ToolsAdapter.ToolViewHolder> {
    private List<Tool> tools = new ArrayList<>();
    private OnToolClickListener onToolClickListener;

    public interface OnToolClickListener {
        void onToolClick(Tool tool);
    }

    public void setOnToolClickListener(OnToolClickListener listener) {
        this.onToolClickListener = listener;
    }

    public void setTools(List<Tool> tools) {
        this.tools = tools != null ? tools : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ToolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tool, parent, false);
        return new ToolViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToolViewHolder holder, int position) {
        Tool tool = tools.get(position);
        holder.bind(tool, onToolClickListener);
    }

    @Override
    public int getItemCount() {
        return tools.size();
    }

    static class ToolViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView nameTextView;
        private final TextView priceTextView;
        private final TextView descriptionTextView;
        private final TextView ownerTextView;

        public ToolViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.tool_image);
            nameTextView = itemView.findViewById(R.id.tool_name);
            priceTextView = itemView.findViewById(R.id.tool_price);
            descriptionTextView = itemView.findViewById(R.id.tool_description);
            ownerTextView = itemView.findViewById(R.id.tool_owner);
        }

        public void bind(Tool tool, OnToolClickListener clickListener) {
            // TODO: Implement proper image loading when Glide dependency is resolved
            // For now, just set a placeholder image
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
            
            // When Glide is working, uncomment this code:
            /*
            // Load image with Glide
            if (tool.getImageUrl() != null && !tool.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(tool.getImageUrl())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .centerCrop()
                        .into(imageView);
            } else {
                // Set default placeholder if no image URL
                imageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
            */

            nameTextView.setText(tool.getName());
            priceTextView.setText(String.format(Locale.getDefault(), "$%.2f", tool.getPrice()));
            descriptionTextView.setText(tool.getDescription());
            ownerTextView.setText(itemView.getContext().getString(R.string.owner_label, tool.getOwnerUsername()));
            
            // Set click listener for the entire item
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onToolClick(tool);
                }
            });
        }
    }
}