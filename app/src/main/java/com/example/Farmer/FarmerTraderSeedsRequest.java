package com.example.Farmer;

import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FarmerTraderSeedsRequest extends AppCompatActivity {

    private FarmerSeedsAdapter myAdapter;
    private List<FarmerSeedsData> farmerSeedsDataList;
    private RecyclerView recyclerView;
    private FirebaseFirestore SeedsRequest;
    private FirebaseFirestore FarmerProduct;
    String username;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_trader_seeds_request);

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerView);
        farmerSeedsDataList = new ArrayList<>();
        SeedsRequest = FirebaseFirestore.getInstance();

        // Get the FloatingActionButton
        FloatingActionButton fabAddProduct = findViewById(R.id.fabAddProduct);

        // Set click listener for the FloatingActionButton
        // Set click listener for the FloatingActionButton
        fabAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open AddProductActivity when the button is clicked
                Intent intent = new Intent(getApplicationContext(), FarmerSeedsRequest.class);
                startActivity(intent);
            }
        });



        final String[] requestedUsername = {""};
        SharedPreferences preferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        String user_Id = preferences.getString("user_Id", null);

        //  String username = "anishbandal";
        username = preferences.getString("user_Id", null);

        fetchData();
        swipeRefreshLayout.setOnRefreshListener(() -> {
            {
                farmerSeedsDataList.clear();
                fetchData();
            }
            swipeRefreshLayout.setRefreshing(false);
        });



    }

    public void fetchData(){
        SeedsRequest.collection("SeedRequest")
                .whereEqualTo("userId", username) // Query documents where "userId" is equal to the specified username
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

                            FarmerSeedsData farmerSeedsData = new FarmerSeedsData(productId, "Order ID: " + orderID, userName, "Product: " + product, "Product Type: " + productType, "Quantity: " + quantity, "Status: " + status);
                            farmerSeedsDataList.add(farmerSeedsData);
                        }

                        // Set up RecyclerView
                        recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        myAdapter = new FarmerSeedsAdapter(farmerSeedsDataList);
                        recyclerView.setAdapter(myAdapter);

                        myAdapter.setOnCheckAvailabilityClickListener(position -> {
                            OnCheckAvailabilityClick(position);
                        });

                    } else {
                        // Handle errors
                    }
                });
    }

    public interface GeoPointCallback {
        void onGeoPointReceived(GeoPoint geoPoint);
    }

    public void OnCheckAvailabilityClick(int position) {
        if (position >= 0 && position < farmerSeedsDataList.size()) {
          FarmerSeedsData   clickedFarmerSeedsData = farmerSeedsDataList.get(position);

            // Ensure that clickedProduct and getUserId() do not return null
            if (clickedFarmerSeedsData != null && clickedFarmerSeedsData.getUserId() != null) {
                String consumerUsername = clickedFarmerSeedsData.getUserId();

                getConsumerGeopoint(consumerUsername, new GeoPointCallback() {
                    @Override
                    public void onGeoPointReceived(GeoPoint consumerGeoPoint) {
                        if (consumerGeoPoint != null) {
                            FirebaseFirestore.getInstance().collection("Seeds")
                                    .whereEqualTo("productId", clickedFarmerSeedsData.getProductId())
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            List<String> orderIdList = new ArrayList<>();
                                            AtomicInteger tasksCount = new AtomicInteger(0);
                                            AtomicInteger successCount = new AtomicInteger(0);

                                            for (QueryDocumentSnapshot productDocument : task.getResult()) {
                                                tasksCount.incrementAndGet();
                                                try {
                                                    int quantity = Integer.parseInt(productDocument.getString("quantity"));
                                                    int requestedQuantity = Integer.parseInt(clickedFarmerSeedsData.getQuantity().split(": ")[1]);

                                                    if (quantity >= requestedQuantity) {
                                                        String farmerUserId = productDocument.getString("User_Id");
                                                        FirebaseFirestore.getInstance().collection("Users")
                                                                .whereEqualTo("username", farmerUserId)
                                                                .get()
                                                                .addOnCompleteListener(task1 -> {
                                                                    if (task1.isSuccessful()) {
                                                                        successCount.incrementAndGet();
                                                                        for (QueryDocumentSnapshot userDocument : task1.getResult()) {
                                                                            // Get the user location
                                                                            GeoPoint farmerLocation = userDocument.getGeoPoint("geopoint");

                                                                            // Compare farmer location with consumer location
                                                                            double distance = calculateDistance(farmerLocation.getLatitude(), farmerLocation.getLongitude(), consumerGeoPoint.getLatitude(), consumerGeoPoint.getLongitude());

                                                                            //Change the distance accordingly
                                                                            if (distance <= 100) {
                                                                                String orderId  = productDocument.getString("OrderId");
                                                                                orderIdList.add(orderId);
                                                                                Log.d("OrderId = ", orderId);
                                                                            }
                                                                            else{
                                                                                Toast.makeText(getApplicationContext(), "The user is not inside 100 km radius", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }

                                                                        if (tasksCount.get() == successCount.get()) {

                                                                            // Store orderIdList and start the activity here
                                                                            Intent intent = new Intent(getApplicationContext(), CheckAvailabilitySeeds_Farmer.class);
                                                                            String requestedID[] = clickedFarmerSeedsData.getRequestId().toString().split(": ");
                                                                            String reqId = requestedID[1];
                                                                            intent.putExtra("requestedId", reqId);
                                                                            intent.putStringArrayListExtra("orderIdList", (ArrayList<String>) orderIdList);
                                                                            startActivity(intent);
                                                                        }
                                                                    } else {
                                                                        // Handle query failure
                                                                        Toast.makeText(getApplicationContext(), "Query failed", Toast.LENGTH_SHORT).show();
                                                                        task1.getException().printStackTrace();
                                                                    }
                                                                });
                                                    }
                                                } catch (NumberFormatException e) {
                                                    Toast.makeText(getApplicationContext(), "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                                                    e.printStackTrace();
                                                    Log.d("Number Exception : ", e.toString());
                                                }
                                            }
                                        } else {
                                            // Handle query failure
                                            Toast.makeText(getApplicationContext(), "Query failed", Toast.LENGTH_SHORT).show();
                                            task.getException().printStackTrace();
                                        }
                                    });
                        } else {
                            Toast.makeText(getApplicationContext(), "Consumer geopoint is null", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                // Handle case when clickedProduct or getUserId() is null
                Toast.makeText(this, "Clicked product or user ID is null", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getConsumerGeopoint(String consumerUsername, GeoPointCallback callback) {

        FirebaseFirestore.getInstance().collection("Users")
                .whereEqualTo("username", consumerUsername)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot userDocument : task.getResult()) {
                            // Get the user location
                            GeoPoint userLocation = userDocument.getGeoPoint("geopoint");
                            callback.onGeoPointReceived(userLocation);
                        }
                    } else {
                        // Handle query failure
                        Toast.makeText(this, "Query failed", Toast.LENGTH_SHORT).show();
                        task.getException().printStackTrace();
                        callback.onGeoPointReceived(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FarmerTraderSeedsRequest.this, "Exception " + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

    }



    // Calculate the distance between two points using the Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Radius of the Earth in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}


