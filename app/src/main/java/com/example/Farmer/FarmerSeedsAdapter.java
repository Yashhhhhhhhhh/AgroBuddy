package com.example.Farmer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FarmerSeedsAdapter extends RecyclerView.Adapter<FarmerSeedsAdapter.MyViewHolder> {

    private List<FarmerSeedsData> farmerSeedsDataList;
    private OnCheckAvailabilityClickListener checkAvailabilityClickListener;

    public FarmerSeedsAdapter(List<FarmerSeedsData> farmerSeedsDataList) {
        this.farmerSeedsDataList = farmerSeedsDataList;
    }

    public interface OnCheckAvailabilityClickListener {
        void onCheckAvailabilityClick(int position);
    }

    public void setOnCheckAvailabilityClickListener(OnCheckAvailabilityClickListener listener) {
        this.checkAvailabilityClickListener = listener;
    }

    @NonNull
    @Override
    public FarmerSeedsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_layout, parent, false);
        return new MyViewHolder(itemView, checkAvailabilityClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        FarmerSeedsData farmerSeedsData = farmerSeedsDataList.get(holder.getAdapterPosition());
        holder.orderId.setText(farmerSeedsData.getRequestId());
        holder.productName.setText(farmerSeedsData.getProductName());
        holder.productType.setText(farmerSeedsData.getProductType());
        holder.quantity.setText(farmerSeedsData.getQuantity());
        holder.status.setText(farmerSeedsData.getStatus());

    }

    @Override
    public int getItemCount() {
        return farmerSeedsDataList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView orderId, productName, productType, quantity, status;
        Button checkAvailabilityButton;

        public MyViewHolder(@NonNull View itemView, final OnCheckAvailabilityClickListener listener) {
            super(itemView);
            orderId = itemView.findViewById(R.id.orderId);
            productName = itemView.findViewById(R.id.productName);
            productType = itemView.findViewById(R.id.productType);
            quantity = itemView.findViewById(R.id.quantity);
            status = itemView.findViewById(R.id.status);
            checkAvailabilityButton = itemView.findViewById(R.id.checkAvailability);

            // Set OnClickListener for the checkAvailabilityButton
            checkAvailabilityButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onCheckAvailabilityClick(position);
                    }
                }
            });
        }
    }
}
