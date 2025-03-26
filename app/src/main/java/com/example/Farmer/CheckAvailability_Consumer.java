package com.example.Farmer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CheckAvailability_Consumer extends AppCompatActivity {

    private FarmerProductAdapter myAdapter;
    private List<FarmerProductData> productList;
    private RecyclerView recyclerView;
    private FirebaseFirestore firestore;

    String requestedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_availability_consumer);

        recyclerView = findViewById(R.id.recyclerView);
        productList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        // Retrieve the orderId list from the intent extras
        ArrayList<String> orderIdList = getIntent().getStringArrayListExtra("orderIdList");

        requestedId = getIntent().getStringExtra("requestedId");
        // Fetch product details from Firebase Firestore
        firestore.collection("Products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String orderId = document.getString("OrderId"); // Corrected field name

                            // Check if the orderId matches any orderId in the orderIdList
                            if (orderIdList.contains(orderId)) {
                                String userId = document.getString("User_Id");
                                String productName = document.getString("productName");
                                String productType = document.getString("productType");
                                String quantity = document.getString("quantity");
                                String status = document.getString("Status");
                                String imageUrl = document.getString("imageUrl");
                                String price = document.getString("price");

                                FarmerProductData productObj = new FarmerProductData(orderId, userId, productName, productType, quantity, status, imageUrl, price);
                                productList.add(productObj);

                            }
                        }

                        // Set up RecyclerView
                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        myAdapter = new FarmerProductAdapter(productList);
                        recyclerView.setAdapter(myAdapter);

                        myAdapter.setOnCheckAvailabilityClickListener(position -> {
                            OnCheckAvailabilityClick(position);
                        });

                    } else {
                        // Handle errors
                    }
                });

    }

    private void OnCheckAvailabilityClick(int position) {

        FarmerProductData clickedProduct = productList.get(position);

        String farmerOrderId = clickedProduct.getOrderId();

        //Requesting in Farmers Database that you are interested

        String requestedId = getIntent().getStringExtra("requestedId");

        // Query for the product document with the matching orderId
        firestore.collection("Products")
                .whereEqualTo("OrderId", farmerOrderId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Reference to the product document
                            DocumentReference productRef = document.getReference();

                            // Add the requestedId to the requestedIds array in the product document
                            productRef.update("requestedIds", FieldValue.arrayUnion(requestedId))
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Product Requested", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to add requested ID to the product document.", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Error querying for product document with OrderId: " + farmerOrderId, Toast.LENGTH_SHORT).show();
                    }
                });



        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("farmersOrderId", farmerOrderId);
        intent.putExtra("receiverUsername", clickedProduct.getUserId());
        intent.putExtra("consumersTransactionId", requestedId);
        startActivity(intent);


    }

}