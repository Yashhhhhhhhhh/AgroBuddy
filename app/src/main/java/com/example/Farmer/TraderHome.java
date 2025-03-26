package com.example.Farmer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class TraderHome extends AppCompatActivity {

    private TraderProductAdapter myAdapter;
    private List<Product> productList;
    private RecyclerView recyclerView;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private View sidebarLayout;
    private FirebaseAuth mAuth; // Add this line to declare FirebaseAuth
    private FirebaseUser currentUser; // Add this line to declare FirebaseUser
    private ImageView profileImageView;
    private TextView profileNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trader_home);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        recyclerView = findViewById(R.id.recyclerView);
        productList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        // Fetch username from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        String username = preferences.getString("user_Id", null);

        // Fetch product details from Firebase Firestore
        firestore.collection("Seeds")  // Update the collection name to "Seeds"
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
                        recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        myAdapter = new TraderProductAdapter(productList);
                        recyclerView.setAdapter(myAdapter);

                        myAdapter.setOnCheckAvailabilityClickListener(position -> {
                            onCheckAvailabilityClick(position);
                        });

                    } else {
                        // Handle errors
                    }
                });

        // Get the FloatingActionButton
        FloatingActionButton fabAddSeed = findViewById(R.id.fabAddProduct);

        // Set click listener for the FloatingActionButton
        fabAddSeed.setOnClickListener(view -> {
            // Open AddSeedActivity when the button is clicked
            Intent intent = new Intent(TraderHome.this, AddSeedActivity.class);
            startActivity(intent);
        });

        // Initialize sidebar components
        initSidebar();

        // Load and display the profile picture
        loadProfilePicture();
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
        sidebarLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left));

        Button changeProfilePictureButton = sidebarLayout.findViewById(R.id.changeProfilePictureButton);
        changeProfilePictureButton.setOnClickListener(v -> {
            // Redirect to ProfileActivity when the button is clicked
            Intent profileIntent = new Intent(TraderHome.this, ProfileActivity.class);
            startActivity(profileIntent);

            // Close the sidebar after redirection
            closeSidebar();
        });

        Button browseSeeds = sidebarLayout.findViewById(R.id.browseSeeds);
        browseSeeds.setVisibility(View.GONE);
        browseSeeds.setOnClickListener(v -> {
            // Redirect to FarmerTraderSeedsRequest when the button is clicked
            Intent traderIntent = new Intent(TraderHome.this, FarmerTraderSeedsRequest.class);
            startActivity(traderIntent);

            // Close the sidebar after redirection
            closeSidebar();
        });

        sidebarLayout.findViewById(R.id.logoutButton).setOnClickListener(v -> logout());

        loadProfilePictureIntoSidebar();
    }

    private void closeSidebar() {
        sidebarLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));
        ((ViewGroup) sidebarLayout.getParent()).removeView(sidebarLayout);
        sidebarLayout = null;
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

    private void onCheckAvailabilityClick(int position) {
        Product clickedProduct = productList.get(position);

        String orderId = clickedProduct.getOrderId();

        // Fetch additional data for the specific orderId document
        firestore.collection("Seeds")
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
                                Intent intent = new Intent(getApplicationContext(), CheckRequestTrader.class);

                                // Pass the requestedIds as an extra to the NextActivity
                                intent.putStringArrayListExtra("requestedIds", new ArrayList<>(requestedIds));

                                String fOrderID = clickedProduct.getOrderId().toString();

                                intent.putExtra("farmerOrderId", fOrderID);
                                // Start the NextActivity
                                startActivity(intent);

                                String requestedIdsString = requestedIds.toString();

                            } else {
                                // Handle case where requestedIds is null or empty
                                Toast.makeText(getApplicationContext(), "No requestedIds available", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle document not existing
                            Toast.makeText(getApplicationContext(), "Document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle errors
                        Toast.makeText(getApplicationContext(), "Error fetching document", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
