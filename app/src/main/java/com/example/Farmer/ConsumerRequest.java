package com.example.Farmer;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ConsumerRequest extends AppCompatActivity {
    private AutoCompleteTextView productNameAutoComplete;
    private AutoCompleteTextView productTypeAutoComplete;
    private TextInputEditText setQuantity; // Add this declaration
    private FirebaseFirestore firestore;
    Button requestBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consumer_request);

        productNameAutoComplete = findViewById(R.id.productNameAutoComplete);
        productTypeAutoComplete = findViewById(R.id.productTypeAutoComplete);
        setQuantity = findViewById(R.id.setQuantity); // Initialize setQuantity
        firestore = FirebaseFirestore.getInstance();
        requestBtn=findViewById(R.id.btnSaveProduct);

        // Example: Call methods to populate spinners
        fetchProductNamesForAutoComplete();

        // Set up product type AutoCompleteTextView based on selected product name
        productNameAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedProductName = productNameAutoComplete.getText().toString();
                fetchProductTypes(selectedProductName);
            }
        });

        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProduct();
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

    private void fetchProductNamesForAutoComplete() {
        firestore.collection("Product_Dataset")
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

                        ArrayAdapter<String> productNameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, productNames);
                        productNameAutoComplete.setAdapter(new FilteredArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, productNames));
                    } else {
                        Toast.makeText(this, "Error fetching product names: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void fetchProductTypes(String selectedProductName) {
        firestore.collection("Product_Dataset")
                .whereEqualTo("productName", selectedProductName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> productTypes = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String productType = document.getString("productType");
                            productTypes.add(productType);
                        }

                        ArrayAdapter<String> productTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, productTypes);
                        productTypeAutoComplete.setAdapter(productTypeAdapter);
                    } else {
                        Toast.makeText(this, "Error fetching product types: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchProductId(String selectedProductType, ProductIdCallback callback) {
        firestore.collection("Product_Dataset")
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






    private void saveProduct() {


        String quantity = setQuantity.getText().toString().trim();
        String productName = productNameAutoComplete.getText().toString();
        String productType = productTypeAutoComplete.getText().toString();


        if (!quantity.isEmpty()) {

            fetchProductId(productType, new ProductIdCallback() {
                @Override
                public void onProductIdReceived(String productId) {
                    CollectionReference productsCollection = firestore.collection("ProductRequest");

                    // Generate a unique document ID
                    String requestId = productsCollection.document().getId();
                    SharedPreferences preferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
                    String username = preferences.getString("user_Id", null);

                    // Create a new product with auto-generated document ID
                    ConsumerProductData newConsumerProductData = new ConsumerProductData(productId,requestId, username, productName, productType, quantity, "Requested");

                    // Add the product to Firestore
                    productsCollection.document(requestId)
                            .set(newConsumerProductData)
                            .addOnSuccessListener(documentReference -> {
                                // Product saved successfully
                                Toast.makeText(getApplicationContext(), "Product saved successfully", Toast.LENGTH_SHORT).show();

                                // Now update the product ID in the user's collection

                            })
                            .addOnFailureListener(e -> {
                                // Handle failure to save product
                                Toast.makeText(getApplicationContext(), "Failed to request product", Toast.LENGTH_SHORT).show();
                            });

                    // Clear EditText field
                    setQuantity.setText("");
                    productNameAutoComplete.setText("");
                    productTypeAutoComplete.setText("");

                }
            });
            // Get a reference to the "Product_Dataset" collection
        } else {
            Toast.makeText(getApplicationContext(), "Please enter quantity", Toast.LENGTH_SHORT).show();
        }
    }
}
