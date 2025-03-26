package com.example.Farmer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AgricultureFragment extends Fragment {

    private ProductAdapter myAdapter;
    private List<Product> productList;
    private RecyclerView recyclerView;
    private FirebaseFirestore firestore;
    private String currentUserId;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_agriculture, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        productList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        SharedPreferences preferences = requireActivity().getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        String username = preferences.getString("user_Id", null);

        // Fetch product details from Firebase Firestore
        firestore.collection("Products")
                .whereEqualTo("User_Id", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String orderId = document.getString("OrderId");
                            String userId = document.getString("User_Id");
                            String productName = document.getString("productName");
                            String productType = document.getString("productType");
                            String quantity = document.getString("quantity");
                            String status = document.getString("Status");
                            String imageUrl = document.getString("imageUrl");
                            String price = document.getString("price");

                            Product productObj = new Product(orderId, userId, productName, productType, quantity, status, imageUrl, price);
                            productList.add(productObj);
                        }

                        // Set up RecyclerView
                        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                        myAdapter = new ProductAdapter(productList);
                        recyclerView.setAdapter(myAdapter);

                        myAdapter.setOnCheckAvailabilityClickListener(position -> {
                            onCheckAvailabilityClick(position);
                        });

                    } else {
                        // Handle errors
                    }
                });

        // Get the FloatingActionButton
        FloatingActionButton fabAddProduct = view.findViewById(R.id.fabAddProduct);


        fabAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open AddProductActivity when the button is clicked
                Intent intent = new Intent(requireContext(), AddProductActivity.class);
                startActivity(intent);
            }
        });



        return view;
    }

    private void onCheckAvailabilityClick(int position) {
        Product clickedProduct = productList.get(position);

        String orderId = clickedProduct.getOrderId();

        // Fetch additional data for the specific orderId document
        firestore.collection("Products")
                .document(orderId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Retrieve data from the document and handle it
                            List<String> requestedIds = (List<String>) document.get("requestedIds");

                            if (requestedIds != null && !requestedIds.isEmpty()) {
                                // Create an Intent to open the NextActivity
                                Intent intent = new Intent(requireContext(), CheckRequestFarmer.class);

                                // Pass the requestedIds as an extra to the NextActivity
                                intent.putStringArrayListExtra("requestedIds", new ArrayList<>(requestedIds));

                                String fOrderID = clickedProduct.getOrderId().toString();

                                intent.putExtra("farmerOrderId", fOrderID);
                                // Start the NextActivity
                                startActivity(intent);

                                String requestedIdsString = requestedIds.toString();

                                // Display requestedIds in a Toast
                            } else {
                                // Handle case where requestedIds is null or empty
                                Toast.makeText(requireContext(), "No requestedIds available", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle document not existing
                            Toast.makeText(requireContext(), "Document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle errors
                        Toast.makeText(requireContext(), "Error fetching document", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
