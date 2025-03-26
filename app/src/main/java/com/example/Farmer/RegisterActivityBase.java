package com.example.Farmer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class RegisterActivityBase extends AppCompatActivity {

    private static final String USERS_COLLECTION = "Users";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_FULL_NAME = "fullName";
    private static final String FIELD_MOBILE_NO = "mobileNo";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_USER_ID = "userID";
    private static final String FIELD_USER_TYPE = "userType";
    private static final String FIELD_LOCATION = "location";
    private static final String FIELD_GEOPOINT = "geopoint";

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText usernameEditText;
    private EditText fullNameEditText;
    private EditText passwordEditText;
    private EditText mobileNoEditText;
    private EditText emailEditText;
    private EditText locationEditText;
    private ImageView profileImageView;
    private Uri imageUri;
    private FirebaseAuth firebaseAuth;
    private FusedLocationProviderClient fusedLocationClient;
    private StorageReference storageReference;
    private String userId;
    private String fullName;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView()); // Replace with your layout file

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.bg_color));
        }

        usernameEditText = findViewById(R.id.usernameEditText);
        fullNameEditText = findViewById(R.id.fullNameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        mobileNoEditText = findViewById(R.id.mobileNoEditText);
        emailEditText = findViewById(R.id.emailEditText);
        locationEditText = findViewById(R.id.locationEditText);
        progressBar = findViewById(R.id.progressBar);

        Button registerButton = findViewById(R.id.registerButton);

        firebaseAuth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        storageReference = FirebaseStorage.getInstance().getReference();

        locationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchLocation();
            }
        });



        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInputs()) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    registerUser();
                }
            }
        });
    }

    protected abstract int getContentView();

    protected abstract String getUserType();

    private boolean validateInputs() {
        String username = usernameEditText.getText().toString().trim();
        fullName = fullNameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String mobileNo = mobileNoEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();

        if (username.isEmpty() || fullName.isEmpty() || password.isEmpty() || mobileNo.isEmpty() || email.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return false;
        }

        if (username.contains(" ")) {
            Toast.makeText(this, "Username should not contain spaces", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return false;
        }

        if (mobileNo.length() != 10) {
            Toast.makeText(this, "Mobile number should be 10 digits", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return false;
        }

        return true;
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Location lastLocation = task.getResult();
                            if (lastLocation != null) {
                                Geocoder geocoder = new Geocoder(RegisterActivityBase.this, Locale.getDefault());
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
                                    if (addresses != null && !addresses.isEmpty()) {
                                        String location = addresses.get(0).getLocality();
                                        if (location != null && !location.isEmpty()) {
                                            locationEditText.setText(location);
                                        } else {
                                            Toast.makeText(RegisterActivityBase.this, "City name is empty", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(RegisterActivityBase.this, "Unable to fetch city name", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(RegisterActivityBase.this, "Geocoding error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(RegisterActivityBase.this, "Please enable location services", Toast.LENGTH_SHORT).show();
                                promptToEnableLocationServices();
                            }
                        } else {
                            Toast.makeText(RegisterActivityBase.this, "Unable to fetch location: " + task.getException(), Toast.LENGTH_SHORT).show();
                            task.getException().printStackTrace();
                        }
                    }
                });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registerUser() {
        final String username = usernameEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();
        final String mobileNo = mobileNoEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        final String location = locationEditText.getText().toString().trim();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivityBase.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (firebaseUser != null) {
                                userId = firebaseUser.getUid();
                                fetchLocationAndRegister(username, mobileNo, email, location);
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(RegisterActivityBase.this, "Error creating user", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivityBase.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void fetchLocationAndRegister(final String username, final String mobileNo, final String email, final String location) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Location lastLocation = task.getResult();
                            if (lastLocation != null) {
                                GeoPoint geoPoint = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
                                saveImageToFirebaseStorage(username, mobileNo, email, location, geoPoint);
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(RegisterActivityBase.this, "Please enable location services", Toast.LENGTH_SHORT).show();
                                promptToEnableLocationServices();
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivityBase.this, "Unable to fetch location: " + task.getException(), Toast.LENGTH_SHORT).show();
                            task.getException().printStackTrace();
                        }
                    }
                });
    }

    private void saveImageToFirebaseStorage(final String username, final String mobileNo, final String email, final String location, final GeoPoint geoPoint) {
        if (imageUri != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] data = baos.toByteArray();
            String path = "profile_images/" + userId + ".jpg";
            final StorageReference ref = storageReference.child(path);

            ref.putBytes(data)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String imageUrl = uri.toString();
                                    saveUserToFirestoreWithImage(username, mobileNo, email, location, imageUrl, geoPoint);
                                });
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(RegisterActivityBase.this, "Error uploading image to Firebase Storage", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            saveUserToFirestoreWithoutImage(username, mobileNo, email, location, geoPoint);
        }
    }

    private void saveUserToFirestoreWithImage(String username, String mobileNo, String email, String location, String imageUrl, GeoPoint geoPoint) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put(FIELD_USERNAME, username);
        userMap.put(FIELD_FULL_NAME, fullName);
        userMap.put(FIELD_MOBILE_NO, mobileNo);
        userMap.put(FIELD_EMAIL, email);
        userMap.put(FIELD_USER_ID, userId);
        userMap.put(FIELD_USER_TYPE, getUserType());
        userMap.put(FIELD_LOCATION, location);
        userMap.put(FIELD_GEOPOINT, geoPoint);
        userMap.put("imageUrl", imageUrl);

        FirebaseFirestore.getInstance().collection(USERS_COLLECTION)
                .document(username)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivityBase.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivityBase.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivityBase.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void saveUserToFirestoreWithoutImage(String username, String mobileNo, String email, String location, GeoPoint geoPoint) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put(FIELD_USERNAME, username);
        userMap.put(FIELD_FULL_NAME, fullName);
        userMap.put(FIELD_MOBILE_NO, mobileNo);
        userMap.put(FIELD_EMAIL, email);
        userMap.put(FIELD_USER_ID, userId);
        userMap.put(FIELD_USER_TYPE, getUserType());
        userMap.put(FIELD_LOCATION, location);
        userMap.put(FIELD_GEOPOINT, geoPoint);

        FirebaseFirestore.getInstance().collection(USERS_COLLECTION)
                .document(username)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivityBase.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivityBase.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivityBase.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void promptToEnableLocationServices() {
        Intent locationSettingsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(locationSettingsIntent);
    }
}