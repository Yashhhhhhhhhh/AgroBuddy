package com.example.Farmer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ConsumerHome extends AppCompatActivity {

    private ConsumerProductAdapter myAdapter;
    private List<ConsumerProductData> consumerProductDataList;
    private RecyclerView recyclerView;
    private FirebaseFirestore ProductRequest;
    String username;

    // Sidebar components
    private View sidebarLayout;
    private StorageReference storageReference;
    private FirebaseAuth mAuth; // Add this line to declare FirebaseAuth
    private FirebaseUser currentUser;
    private ImageView profileImageView;
    private TextView profileNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumer_home);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerView);
        consumerProductDataList = new ArrayList<>();
        ProductRequest = FirebaseFirestore.getInstance();

        // Get the FloatingActionButton
        findViewById(R.id.fabAddProduct).setOnClickListener(view -> {
            // Open AddProductActivity when the button is clicked
            Intent intent = new Intent(ConsumerHome.this, ConsumerRequest.class);
            startActivity(intent);
        });

        // Initialize sidebar components
        initSidebar();

        // Load and display the profile picture
        loadProfilePicture();

        SharedPreferences preferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        username = preferences.getString("user_Id", null);

        fetchData();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            consumerProductDataList.clear();
            fetchData();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void initSidebar() {
        profileImageView = findViewById(R.id.profileImageView);
        profileNameTextView = findViewById(R.id.profileNameTextView);

        profileImageView.setOnClickListener(v -> toggleSidebar());
    }

    private void toggleSidebar() {
        if (isSidebarOpen()) {
            closeSidebar();
        } else {
            openSidebar();
        }
    }

    private boolean isSidebarOpen() {
        return sidebarLayout != null;
    }

    @Override
    public void onBackPressed() {
        if (isSidebarOpen()) {
            closeSidebar();
        } else {
            super.onBackPressed();
        }
    }

    private void openSidebar() {
        sidebarLayout = LayoutInflater.from(this).inflate(R.layout.sidebar_layout, findViewById(android.R.id.content), false);
        addContentView(sidebarLayout, sidebarLayout.getLayoutParams());

        // Start the slide-in animation
        Animation slideInLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        sidebarLayout.startAnimation(slideInLeft);

        Button browseSeeds = findViewById(R.id.browseSeeds);
        browseSeeds.setVisibility(View.GONE);

        Button changeProfilePictureButton = sidebarLayout.findViewById(R.id.changeProfilePictureButton);
        changeProfilePictureButton.setOnClickListener(v -> {
            // Redirect to ProfileActivity when the button is clicked
            Intent profileIntent = new Intent(ConsumerHome.this, ProfileActivity.class);
            startActivity(profileIntent);

            // Close the sidebar after redirection
            closeSidebar();
        });

        // ... (other buttons initialization)

        sidebarLayout.findViewById(R.id.logoutButton).setOnClickListener(v -> logout());

        loadProfilePictureIntoSidebar();
    }


    private void closeSidebar() {
        // Start the slide-out animation
        Animation slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                ((ViewGroup) sidebarLayout.getParent()).removeView(sidebarLayout);
                sidebarLayout = null;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        sidebarLayout.startAnimation(slideOutLeft);
    }




    private void loadProfilePicture() {
        if (currentUser != null) {
            SharedPreferences preferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
            String user_Id = preferences.getString("user_Id", null);

            // Construct the reference to the profile picture in Firebase Storage
            StorageReference profilePictureRef = storageReference.child(user_Id + ".jpg");

            if (profilePictureRef != null) {
                // Load the profile picture using Picasso
                profilePictureRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Log or Toast to check the retrieved image URI

                    // Load the image into the ImageView using Picasso
                    Picasso.get().load(uri).into(profileImageView);
                }).addOnFailureListener(e -> {
                    // Handle failure to load the image
                    e.printStackTrace();
                    Log.e("ProfilePicture", "Failed to load image: " + e.getMessage());
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                });
            } else {
                // Log or Toast if profilePictureRef is null
                Log.e("ProfilePicture", "Profile picture reference is null");
                Toast.makeText(this, "Profile picture reference is null", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadProfilePictureIntoSidebar() {
        if (currentUser != null) {
            SharedPreferences preferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
            String user_Id = preferences.getString("user_Id", null);

            StorageReference profilePictureRef = storageReference.child(user_Id + ".jpg");

            if (profilePictureRef != null) {
                profilePictureRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    ImageView sidebarProfileImage = sidebarLayout.findViewById(R.id.sidebarProfileImage);

                    // Load the image into ImageView using Picasso and apply a transformation to make it circular
                    Picasso.get()
                            .load(uri)
                            .transform(new CircleTransformation()) // Apply circular transformation
                            .into(sidebarProfileImage);
                }).addOnFailureListener(e -> {
                    e.printStackTrace();
                    Log.e("ProfilePicture", "Failed to load image: " + e.getMessage());
                });
            } else {
                Log.e("ProfilePicture", "Profile picture reference is null");
            }
        }
    }

    private void logout() {
        // Clear SharedPreferences
        SharedPreferences preferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        // Sign out from Firebase Authentication
        mAuth.signOut();

        // Redirect to LoginActivity
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clears activity stack
        startActivity(loginIntent);
        finish();
    }

    public void fetchData() {
        ProductRequest.collection("ProductRequest")
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

                            ConsumerProductData consumerProductDataObj = new ConsumerProductData(productId, "Order ID: " + orderID, userName, "Product: " + product, "Product Type: " + productType, "Quantity: " + quantity, "Status: " + status);
                            consumerProductDataList.add(consumerProductDataObj);
                        }

                        // Set up RecyclerView
                        recyclerView.setLayoutManager(new LinearLayoutManager(ConsumerHome.this));
                        myAdapter = new ConsumerProductAdapter(consumerProductDataList);
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
        if (position >= 0 && position < consumerProductDataList.size()) {
            ConsumerProductData clickedConsumerProductData = consumerProductDataList.get(position);

            // Ensure that clickedProduct and getUserId() do not return null
            if (clickedConsumerProductData != null && clickedConsumerProductData.getUserId() != null) {
                String consumerUsername = clickedConsumerProductData.getUserId();

                getConsumerGeopoint(consumerUsername, new GeoPointCallback() {
                    @Override
                    public void onGeoPointReceived(GeoPoint consumerGeoPoint) {
                        if (consumerGeoPoint != null) {
                            FirebaseFirestore.getInstance().collection("Products")
                                    .whereEqualTo("productId", clickedConsumerProductData.getProductId())
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
                                                    int requestedQuantity = Integer.parseInt(clickedConsumerProductData.getQuantity().split(": ")[1]);

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

                                                                            // Change the distance accordingly
                                                                            if (distance <= 100) {
                                                                                String orderId = productDocument.getString("OrderId");
                                                                                orderIdList.add(orderId);
                                                                                Log.d("OrderId = ", orderId);
                                                                            } else {
                                                                                Toast.makeText(ConsumerHome.this, "The user is not inside 100 km radius", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }

                                                                        if (tasksCount.get() == successCount.get()) {
                                                                            // Store orderIdList and start the activity here
                                                                            Intent intent = new Intent(ConsumerHome.this, CheckAvailability_Consumer.class);
                                                                            String requestedID[] = clickedConsumerProductData.getRequestId().toString().split(": ");
                                                                            String reqId = requestedID[1];
                                                                            intent.putExtra("requestedId", reqId);
                                                                            intent.putStringArrayListExtra("orderIdList", (ArrayList<String>) orderIdList);
                                                                            startActivity(intent);
                                                                        }
                                                                    } else {
                                                                        // Handle query failure
                                                                        Toast.makeText(ConsumerHome.this, "Query failed", Toast.LENGTH_SHORT).show();
                                                                        task1.getException().printStackTrace();
                                                                    }
                                                                });
                                                    }
                                                } catch (NumberFormatException e) {
                                                    Toast.makeText(ConsumerHome.this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                                                    e.printStackTrace();
                                                    Log.d("Number Exception : ", e.toString());
                                                }
                                            }
                                        } else {
                                            // Handle query failure
                                            Toast.makeText(ConsumerHome.this, "Query failed", Toast.LENGTH_SHORT).show();
                                            task.getException().printStackTrace();
                                        }
                                    });
                        } else {
                            Toast.makeText(ConsumerHome.this, "Consumer geopoint is null", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(ConsumerHome.this, "Exception " + e.toString(), Toast.LENGTH_SHORT).show();
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
