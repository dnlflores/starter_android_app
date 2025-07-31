package com.example.starter.ui.post;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.starter.R;
import com.example.starter.model.AddressResult;

import java.util.ArrayList;
import java.util.List;

public class AddressSearchAdapter extends RecyclerView.Adapter<AddressSearchAdapter.AddressViewHolder> {

    private List<AddressResult> addresses = new ArrayList<>();
    private OnAddressClickListener listener;

    public interface OnAddressClickListener {
        void onAddressClick(AddressResult address);
    }

    public AddressSearchAdapter(OnAddressClickListener listener) {
        this.listener = listener;
    }

    public void setAddresses(List<AddressResult> addresses) {
        this.addresses = addresses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address_result, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        AddressResult address = addresses.get(position);
        holder.bind(address);
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    class AddressViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAddressTitle;
        private TextView tvAddressSubtitle;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddressTitle = itemView.findViewById(R.id.tv_address_title);
            tvAddressSubtitle = itemView.findViewById(R.id.tv_address_subtitle);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAddressClick(addresses.get(position));
                }
            });
        }

        public void bind(AddressResult address) {
            tvAddressTitle.setText(address.getTitle());
            tvAddressSubtitle.setText(address.getSubtitle());
        }
    }
} 