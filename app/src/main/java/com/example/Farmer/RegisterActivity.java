package com.example.Farmer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    // Constants for Firestore fields
    private static final String USERS_COLLECTION = "Users";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_MOBILE_NO = "mobileNo";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_USER_TYPE = "userType";
    private static final String FIELD_LOCATION = "location";

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText mobileNoEditText;
    private EditText emailEditText;
    private EditText locationEditText;
    private Spinner userTypeSpinner;
    private FirebaseAuth firebaseAuth;
    private FusedLocationProviderClient fusedLocationClient;
    private DocumentReference currentUserDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize UI elements
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        mobileNoEditText = findViewById(R.id.mobileNoEditText);
        emailEditText = findViewById(R.id.emailEditText);
        locationEditText = findViewById(R.id.locationEditText);
        userTypeSpinner = findViewById(R.id.userTypeSpinner);
        Button registerButton = findViewById(R.id.registerButton);

        // Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up Firestore reference with UID as document ID
        if (firebaseAuth.getCurrentUser() != null) {
            String currentUserId = firebaseAuth.getCurrentUser().getUid();
            currentUserDb = FirebaseFirestore.getInstance().collection(USERS_COLLECTION).document(currentUserId);
        } else {
            // Handle the case where the user is not authenticated
            Toast.makeText(RegisterActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }

        // Populate user type spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.user_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userTypeSpinner.setAdapter(adapter);

        // Set click listener for locationEditText
        locationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchLocation();
            }
        });

        // Set click listener for registerButton
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        final String username = usernameEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();
        final String mobileNo = mobileNoEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        final String userType = userTypeSpinner.getSelectedItem().toString();

        // Use Firebase Authentication to create a user
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registration success

                            // Save user details to Firestore using User Name as document ID
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put(FIELD_USERNAME, username);
                            userMap.put(FIELD_MOBILE_NO, mobileNo);
                            userMap.put(FIELD_EMAIL, email);
                            userMap.put(FIELD_USER_TYPE, userType);
                            userMap.put(FIELD_LOCATION, locationEditText.getText().toString());

                            // Use the User Name as the document ID
                            FirebaseFirestore.getInstance().collection(USERS_COLLECTION)
                                    .document(username)
                                    .set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        // Display a toast message for successful registration
                                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                                        // Now, you can either redirect the user to the login page or any other desired activity
                                        // For example, start LoginActivity
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                        finish(); // Finish the current activity
                                    })
                                    .addOnFailureListener(e -> {
                                        // If registration fails, display a message to the user.
                                        // You might also want to handle other failure cases here.
                                        Toast.makeText(RegisterActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        e.printStackTrace(); // Print the exception stack trace
                                    });
                        } else {
                            // If registration fails, display a message to the user.
                            // You might also want to handle other failure cases here.
                            Toast.makeText(RegisterActivity.this, "Registration failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            task.getException().printStackTrace(); // Print the exception stack trace
                        }
                    }
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted, fetch location
                fetchLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchLocation() {
        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Fetch last known location
        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Location lastLocation = task.getResult();
                            if (lastLocation != null) {
                                // Reverse geocoding to get city name
                                Geocoder geocoder = new Geocoder(RegisterActivity.this, Locale.getDefault());
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
                                    if (addresses != null && addresses.size() > 0) {
                                        String location = addresses.get(0).getLocality();
                                        if (location != null && !location.isEmpty()) {
                                            locationEditText.setText(location);
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "City name is empty", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Unable to fetch city name", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(RegisterActivity.this, "Geocoding error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Location is null, prompt the user to enable location services
                                Toast.makeText(RegisterActivity.this, "Please enable location services", Toast.LENGTH_SHORT).show();
                                promptToEnableLocationServices();
                            }
                        } else {
                            // Handle the error
                            Toast.makeText(RegisterActivity.this, "Unable to fetch location: " + task.getException(), Toast.LENGTH_SHORT).show();
                            task.getException().printStackTrace();
                        }
                    }
                });
    }

    private void promptToEnableLocationServices() {
        // Create an intent to open location settings
        Intent locationSettingsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        // Start the intent
        startActivity(locationSettingsIntent);
    }

}
