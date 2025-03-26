package com.example.Farmer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class FarmerProductAdapter extends RecyclerView.Adapter<FarmerProductAdapter.MyViewHolder> {

    private List<FarmerProductData> productList;
    private OnCheckAvailabilityClickListener checkAvailabilityClickListener;

    public FarmerProductAdapter(List<FarmerProductData> productList) {
        this.productList = productList;
    }

    public interface OnCheckAvailabilityClickListener {
        void onCheckAvailabilityClick(int position);
    }

    public void setOnCheckAvailabilityClickListener(OnCheckAvailabilityClickListener listener) {
        this.checkAvailabilityClickListener = listener;
    }

    @NonNull
    @Override
    public FarmerProductAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.farmer_product_layout, parent, false);
        return new MyViewHolder(itemView, checkAvailabilityClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        FarmerProductData product = productList.get(position);
        holder.orderId.setText("Order ID: " + product.getOrderId());
        holder.productName.setText("Product: " + product.getProductName());
        holder.productType.setText("Product Type: " + product.getProductType());
        holder.productPrice.setText("Product Price: " + product.getPrice());
        holder.status.setText("Status: " + product.getStatus());
        holder.quantity.setText("Quantity: " + product.getQuantity());

        // Load image using Picasso
        Picasso.get().load(product.getImageUrl()).into(holder.productImage);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView orderId, productName, productType, productPrice, status, quantity;
        ImageView productImage;
        Button checkAvailabilityButton;

        public MyViewHolder(@NonNull View itemView, final OnCheckAvailabilityClickListener listener) {
            super(itemView);
            orderId = itemView.findViewById(R.id.orderId);
            productName = itemView.findViewById(R.id.productName);
            productType = itemView.findViewById(R.id.productType);
            productPrice = itemView.findViewById(R.id.productPrice);
            status = itemView.findViewById(R.id.status);
            quantity = itemView.findViewById(R.id.quantity);
            productImage = itemView.findViewById(R.id.productImage);
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

