package com.example.Farmer;

import android.Manifest;
import android.app.Activity;
import android.widget.ProgressBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AddSeedActivity extends Activity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private ImageView productImageView;
    private Bitmap capturedImage;

    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    ProgressBar progressBar;

    private AutoCompleteTextView productNameAutoComplete;
    private AutoCompleteTextView productTypeAutoComplete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_seed);

        // Initialize UI elements
        productNameAutoComplete = findViewById(R.id.seedNameAutoComplete);
        productTypeAutoComplete = findViewById(R.id.seedTypeAutoComplete);
        EditText priceEditText = findViewById(R.id.seedPriceEditText);
        EditText quantityEditText = findViewById(R.id.seedQuantityEditText);
        Button addProductButton = findViewById(R.id.addSeedButton);
        productImageView = findViewById(R.id.seedImageView);
        Button captureImageButton = findViewById(R.id.captureSeedImageButton);
        progressBar = findViewById(R.id.seedProgressBar);


        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();

        // Fetch product names from Firestore and populate the productNameAutoComplete
        fetchProductNamesForAutoComplete();

        // Set up product type AutoCompleteTextView based on selected product name
        productNameAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedProductName = productNameAutoComplete.getText().toString();
                fetchProductTypes(selectedProductName);
            }
        });

        // Set click listener for captureImageButton
        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check and request camera permission
                if (ContextCompat.checkSelfPermission(AddSeedActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddSeedActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                } else {
                    // Camera permission already granted, open the camera
                    dispatchTakePictureIntent();
                }
            }
        });

        // Set click listener for addProductButton
        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user inputs
                String productName = productNameAutoComplete.getText().toString();
                String productType = productTypeAutoComplete.getText().toString();
                String price = priceEditText.getText().toString().trim();
                String quantity = quantityEditText.getText().toString().trim();

                // Validate inputs
                if (productName.isEmpty() || productType.isEmpty() || price.isEmpty() || quantity.isEmpty()) {
                    Toast.makeText(AddSeedActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    progressBar = findViewById(R.id.seedProgressBar);
                    progressBar.setVisibility(View.VISIBLE);
                    // Upload image to Firebase Storage
                    uploadImageToStorage(productName, productType, price, quantity);
                }
            }
        });
    }

    private void fetchProductNamesForAutoComplete() {
        firestore.collection("Seed_Dataset")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> productNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String productName = document.getString("productName");
                            if (productName != null && !productNames.contains(productName)) {
                                productNames.add(productName);
                            }
                        }

                        ArrayAdapter<String> productNameAdapter = new ArrayAdapter<>(AddSeedActivity.this, android.R.layout.simple_dropdown_item_1line, productNames);
                        productNameAutoComplete.setAdapter(new FilteredArrayAdapter<>(AddSeedActivity.this, android.R.layout.simple_dropdown_item_1line, productNames));
                    } else {
                        Toast.makeText(AddSeedActivity.this, "Error fetching product names: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static class FilteredArrayAdapter<T> extends ArrayAdapter<T> {

        private List<T> items;
        private List<T> filteredItems;

        public FilteredArrayAdapter(Context context, int resource, List<T> objects) {
            super(context, resource, objects);
            this.items = objects;
            this.filteredItems = new ArrayList<>(objects); // Ensure proper initialization
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();

                    String query = constraint.toString().toLowerCase();

                    List<T> filteredList = new ArrayList<>();
                    for (T item : items) {
                        String itemName = item.toString().toLowerCase();
                        if (itemName.contains(query)) {
                            filteredList.add(item);
                        }
                    }

                    results.values = filteredList;
                    results.count = filteredList.size();

                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredItems = (List<T>) results.values;
                    notifyDataSetChanged();
                }
            };
        }

        @Override
        public int getCount() {
            if (filteredItems == null) {
                return 0; // Ensure a valid count even if filteredItems is null
            }
            return filteredItems.size();
        }

        @Nullable
        @Override
        public T getItem(int position) {
            if (filteredItems == null || position < 0 || position >= filteredItems.size()) {
                return null;
            }
            return filteredItems.get(position);
        }
    }

    private void fetchProductTypes(String selectedProductName) {
        firestore.collection("Seed_Dataset")
                .whereEqualTo("productName", selectedProductName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> productTypes = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String productType = document.getString("productType");
                            productTypes.add(productType);
                        }

                        ArrayAdapter<String> productTypeAdapter = new ArrayAdapter<>(AddSeedActivity.this, android.R.layout.simple_dropdown_item_1line, productTypes);
                        productTypeAutoComplete.setAdapter(productTypeAdapter);
                    } else {
                        Toast.makeText(AddSeedActivity.this, "Error fetching product types: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImageToStorage(final String productName, final String productType, final String price, final String quantity) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Continue with the upload process

            // Fetch the email from Firebase Authentication
            String userEmail = currentUser.getEmail();

            // Create a storage reference
            StorageReference storageRef = storage.getReference().child("product_images").child(System.currentTimeMillis() + ".jpg");

            // Convert Bitmap to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            capturedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            // Upload the image
            UploadTask uploadTask = storageRef.putBytes(data);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.INVISIBLE);
                        // Image uploaded successfully, get download URL
                        Uri downloadUri = task.getResult();

                        // Log the download URL for debugging
                        Log.d("FirebaseUpload", "Download URL: " + downloadUri);

                        // Continue with the rest of the upload process
                        String orderId = generateRandomOrderId();
                        SharedPreferences preferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
                        String username = preferences.getString("user_Id", null);

                        // Fetch the product ID for the selected product type from Products_Dataset
                        fetchProductId(productType, new ProductIdCallback() {
                            @Override
                            public void onProductIdReceived(String productId) {
                                // Use the received product ID in your Firestore document
                                Map<String, Object> productData = new HashMap<>();
                                productData.put("OrderId", orderId);
                                productData.put("User_Id", username);
                                productData.put("Status", "Available");
                                productData.put("productName", productName);
                                productData.put("productType", productType);
                                productData.put("price", price);
                                productData.put("quantity", quantity);
                                productData.put("imageUrl", downloadUri.toString());
                                productData.put("productId", productId);

                                firestore.collection("Seeds")
                                        .document(orderId)
                                        .set(productData)
                                        .addOnSuccessListener(documentReference -> {
                                            // Document added successfully
                                            String message = "Seeds added: " + productName + ", Type: " + productType + ", Price: " + price + " per kg, Quantity: " + quantity + " kg";
                                            Toast.makeText(AddSeedActivity.this, message, Toast.LENGTH_LONG).show();

                                            // Clear input fields after adding product
                                            clearInputFields();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Error adding document
                                            Toast.makeText(AddSeedActivity.this, "Error adding product to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        });
                    } else {
                        // Log the failure message for debugging
                        Log.e("FirebaseUpload", "Error uploading image to Firebase Storage", task.getException());

                        // Handle failures
                        Toast.makeText(AddSeedActivity.this, "Error uploading image to Firebase Storage", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // Handle case where user is not signed in
            Toast.makeText(AddSeedActivity.this, "Error: User not signed in", Toast.LENGTH_SHORT).show();
        }
    }

    // Fetch the product ID for the selected product type from Products_Dataset
    private void fetchProductId(String selectedProductType, ProductIdCallback callback) {
        firestore.collection("Seed_Dataset")
                .whereEqualTo("productType", selectedProductType)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String productId = document.getString("productId");
                            if (productId != null) {
                                callback.onProductIdReceived(productId);
                                return;
                            }
                        }
                    }
                    // If product ID is not found, you may handle it accordingly
                    callback.onProductIdReceived(null);
                });
    }

    // Callback interface for receiving product ID
    interface ProductIdCallback {
        void onProductIdReceived(String productId);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            try {
                // Get the captured image
                Bundle extras = data.getExtras();
                if (extras != null) {
                    capturedImage = (Bitmap) extras.get("data");

                    // Update the ImageView with the captured image
                    if (capturedImage != null) {
                        productImageView.setImageBitmap(capturedImage);
                        // Proceed with other tasks or validations
                    } else {
                        Toast.makeText(AddSeedActivity.this, "Error: Captured image is null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddSeedActivity.this, "Error: Extras bundle is null", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(AddSeedActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(AddSeedActivity.this, "Error: Image capture failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, open the camera
                dispatchTakePictureIntent();
            } else {
                // Camera permission denied, show a message or handle accordingly
                Toast.makeText(AddSeedActivity.this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void clearInputFields() {
        // Clear all input fields
        EditText priceEditText = findViewById(R.id.priceEditText);
        EditText quantityEditText = findViewById(R.id.quantityEditText);

        productNameAutoComplete.setText("");
        productTypeAutoComplete.setText("");
        priceEditText.setText("");
        quantityEditText.setText("");
        productImageView.setImageResource(R.drawable.add_product_bg); // Reset ImageView to default image
    }

    private String generateRandomOrderId() {
        // For demonstration purposes, generate a random OrderId (you may use a better algorithm)
        Random random = new Random();
        return String.valueOf(random.nextInt(100000));
    }
}
