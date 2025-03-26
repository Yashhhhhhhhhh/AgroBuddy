package com.example.Farmer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Farmer.Product;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TraderProductAdapter extends RecyclerView.Adapter<TraderProductAdapter.MyViewHolder> {

    private List<Product> productList;
    private OnCheckAvailabilityClickListener checkAvailabilityClickListener;

    public TraderProductAdapter(List<Product> productList) {
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
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_trader, parent, false);
        return new MyViewHolder(itemView, checkAvailabilityClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.orderId.setText("Order ID: " + product.getOrderId());
        holder.productName.setText("Product: " + product.getProductName());
        holder.productType.setText("Product Type: " + product.getProductType());
        holder.productPrice.setText("Product Price: " + product.getPrice());
        holder.status.setText("Status: " + product.getStatus());
        holder.quantity.setText("Quantity: " + product.getQuantity());

        // Load image using Picasso
      //  Picasso.get().load(product.getImageUrl()).into(holder.productImage);

        // Load image using Picasso library
        Picasso.get()
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_camera)
                .error(R.drawable.ic_camera)
                .into(holder.productImage);

        // Set click listener for the check availability button
        holder.checkAvailabilityButton.setOnClickListener(v -> {
            if (checkAvailabilityClickListener != null) {
                checkAvailabilityClickListener.onCheckAvailabilityClick(position);
            }
        });
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
