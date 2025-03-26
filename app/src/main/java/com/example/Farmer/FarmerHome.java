package com.example.Farmer;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FarmerHome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_home);

        // Initialize UI elements
        TextView welcomeTextView = findViewById(R.id.welcomeTextView);

        // Get the current user's display name (replace this with your actual user data retrieval logic)
        String displayName = "Farmer"; // Replace with actual display name retrieval

        // Display the welcome message
        welcomeTextView.setText("Welcome to Farmer Page, " + displayName + "!");
    }
}
