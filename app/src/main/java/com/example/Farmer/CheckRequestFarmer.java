package com.example.Farmer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CheckRequestFarmer extends AppCompatActivity {
    private ConsumerProductAdapter myAdapter;
    private List<ConsumerProductData> productList;
    private RecyclerView recyclerView;
    private FirebaseFirestore firestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_request_farmer);

        ArrayList<String> requestedIds = getIntent().getStringArrayListExtra("requestedIds");

        recyclerView = findViewById(R.id.recyclerView);
        productList = new ArrayList<>();

        firestore = FirebaseFirestore.getInstance();

        firestore.collection("ProductRequest")
                .whereIn("requestId", requestedIds)  // Only retrieve documents where the OrderId matches any orderId in the orderIdList
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String orderID = document.getString("requestId");
                            String userName = document.getString("userId");
                            String product = document.getString("productName");
                            String productType = document.getString("productType");
                            String quantity = document.getString("quantity");
                            String status = document.getString("status");
                            String productId = document.getString("productId");

                            ConsumerProductData consumerProductDataObj = new ConsumerProductData(productId, "Order ID: " + orderID, userName, "Product: " + product, "Product Type: " + productType, "Quantity: " + quantity, "Status: " + status);
                            productList.add(consumerProductDataObj);
                        }

                        // Set up RecyclerView
                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        myAdapter = new ConsumerProductAdapter(productList);
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

            ConsumerProductData clickedProduct = productList.get(position);

            String consumerRequest[] = clickedProduct.getRequestId().toString().split(": ");

            String farmerOrderId = getIntent().getStringExtra("farmerOrderId");


            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("farmersOrderId", farmerOrderId);
            intent.putExtra("receiverUsername", clickedProduct.getUserId());
            intent.putExtra("consumersTransactionId", consumerRequest[1]);
            startActivity(intent);


        }



}