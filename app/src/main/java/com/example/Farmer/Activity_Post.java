package com.example.Farmer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.Farmer.databinding.ActivityPostBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class Activity_Post extends AppCompatActivity {

    private ActivityPostBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private StorageReference storageReference;

    // Sidebar variables
    private boolean isSidebarOpen = false;
    private View sidebarLayout;
    private static final int IMAGE_SELECTION_CODE = 100;
    private boolean isHomeFragmentVisible = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Change status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.bg_color));
        }


        setSupportActionBar(binding.toolbar);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        // Get the current user
        currentUser = mAuth.getCurrentUser();

        // Display the HomeFragment initially
        replaceFragment(new HomeFragment());

        // Set up the BottomNavigationView listener
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.news) {
                replaceFragment(new NewsFragment());
            } else if (itemId == R.id.addPost) {
                replaceFragment(new AddPostFragment());
            } else if (itemId == R.id.blogs) {
                replaceFragment(new BlogsFragment(getBaseContext()));
            } else if (itemId == R.id.agriculture) {
                replaceFragment(new AgricultureFragment());
            }
            return true;
        });

        // Load and display the profile picture
        loadProfilePicture();

        // Sidebar setup
        binding.profileImageView.setOnClickListener(v -> toggleSidebar());
    }

    @Override
    public void onBackPressed() {
        if (isSidebarOpen) {
            // If the sidebar is open, close it
            closeSidebar();
        } else {
            // If the sidebar is not open, perform the default back button behavior
            super.onBackPressed();
        }
    }

    private void closeSidebar() {
        // Close sidebar animation
        sidebarLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));
        binding.getRoot().removeView(sidebarLayout);
        isSidebarOpen = false;
    }

    // Method to replace fragments in the FrameLayout
    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_Layout, fragment);
        fragmentTransaction.commit();

        isHomeFragmentVisible = fragment instanceof HomeFragment;
        updateToolbarVisibility();
    }

    // Method to load and display the profile picture from Firebase Storage using Picasso
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
                    Picasso.get().load(uri).into(binding.profileImageView);
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

    // Inside your Activity_Post class

    private void toggleSidebar() {
        if (isSidebarOpen) {
            closeSidebar();
        } else {
            sidebarLayout = LayoutInflater.from(this).inflate(R.layout.sidebar_layout, binding.getRoot(), false);
            binding.getRoot().addView(sidebarLayout);
            sidebarLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left));

            Button changeProfilePictureButton = sidebarLayout.findViewById(R.id.changeProfilePictureButton);
            changeProfilePictureButton.setOnClickListener(v -> {
                // Redirect to ProfileActivity when the button is clicked
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                startActivity(profileIntent);

                // Close the sidebar after redirection
                closeSidebar();
            });

            Button browseSeeds = sidebarLayout.findViewById(R.id.browseSeeds);
            browseSeeds.setOnClickListener(v -> {
                // Redirect to ProfileActivity when the button is clicked
                Intent traderIntent = new Intent(this, FarmerTraderSeedsRequest.class);
                startActivity(traderIntent);

                // Close the sidebar after redirection
                closeSidebar();
            });


            sidebarLayout.findViewById(R.id.logoutButton).setOnClickListener(v -> logout());

            loadProfilePictureIntoSidebar();
        }
        isSidebarOpen = !isSidebarOpen;
    }





    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_SELECTION_CODE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            Picasso.get().load(imageUri).into(binding.profileImageView);

            ImageView sidebarProfileImage = sidebarLayout.findViewById(R.id.sidebarProfileImage);
            Picasso.get().load(imageUri).into(sidebarProfileImage);

            Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show();

            // Remove the uploadProfilePicture() method call
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





    private void updateToolbarVisibility() {
        binding.toolbar.setVisibility(isHomeFragmentVisible ? View.VISIBLE : View.GONE);
    }
}
