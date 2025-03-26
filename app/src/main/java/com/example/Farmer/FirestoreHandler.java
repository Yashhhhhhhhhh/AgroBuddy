package com.example.Farmer;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreHandler {

    private FirebaseFirestore firestore;
    private CollectionReference ProductRequest;

    public FirestoreHandler() {
        // Initialize the Firestore instance and reference to the "products" collection
        firestore = FirebaseFirestore.getInstance();
        ProductRequest = firestore.collection("ProductRequest");
    }

    public Task<Void> addProductToFirestore(ConsumerProductData consumerProductData) {
        // Add the product to the "products" collection in Firestore
        return ProductRequest.document(consumerProductData.getRequestId()).set(consumerProductData);
    }
}
