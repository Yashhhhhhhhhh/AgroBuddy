package com.example.Farmer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.bg_color));
        }

        // Initialize UI elements
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        TextView registerTextView = findViewById(R.id.registerTextView);
        progressBar = findViewById(R.id.progressBar);

        // Set click listener for loginButton
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        // Set click listener for registerTextView
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectToRegisterPage();
            }
        });

        // Check if the user is already authenticated
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Check if OTP is verified
            SharedPreferences preferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
            boolean isOtpVerified = preferences.getBoolean("isOtpVerified", false);

            if (isOtpVerified) {
                // User is already authenticated and OTP is verified, proceed to Activity_Post
                redirectToPostActivity();
            }
        }
    }

    private void redirectToPostActivity() {
        SharedPreferences preferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        String userType = preferences.getString("userType", ""); // Retrieve userType from SharedPreferences

        // Redirect based on userType
        Intent redirectIntent;
        switch (userType) {
            case "Farmer":
                redirectIntent = new Intent(LoginActivity.this, Activity_Post.class);
                break;
            case "Consumer":
                redirectIntent = new Intent(LoginActivity.this, ConsumerHome.class); // Replace with your Consumer activity class
                break;
            case "Trader":
                redirectIntent = new Intent(LoginActivity.this, TraderHome.class); // Replace with your Trader activity class
                break;
            default:
                redirectIntent = new Intent(LoginActivity.this, LoginActivity.class); // Replace with a default activity if userType is not recognized
                break;
        }

        // Redirect to the appropriate activity
        startActivity(redirectIntent);
        finish(); // Finish the current activity
    }


    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE); // Show the ProgressBar
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setVisibility(View.INVISIBLE); // Hide the login button

        // Use Firebase Authentication to sign in the user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE); // Hide the ProgressBar
                        loginButton.setVisibility(View.VISIBLE); // Show the login button

                        if (task.isSuccessful()) {
                            // Sign in success

                            // Retrieve and save user authentication token locally
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                user.getIdToken(true)
                                        .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<GetTokenResult> tokenTask) {
                                                if (tokenTask.isSuccessful()) {
                                                    // Successfully retrieved the token
                                                    redirectToOtpVerification(email);
                                                } else {
                                                    // Handle token retrieval error
                                                    Toast.makeText(LoginActivity.this, "Error getting user token: " + tokenTask.getException(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            task.getException().printStackTrace(); // Print the exception stack trace
                        }
                    }
                });
    }

    private void redirectToOtpVerification(String email) {
        // Redirect to OtpVerificationActivity
        Intent intent = new Intent(LoginActivity.this, OtpVerificationActivity.class);
        intent.putExtra("userEmail", email);
        startActivity(intent);
        finish(); // Finish the current activity
    }

    private void redirectToRegisterPage() {
        // Redirect to RegisterPageActivity
        startActivity(new Intent(LoginActivity.this, RegisterPageActivity.class));
    }
}