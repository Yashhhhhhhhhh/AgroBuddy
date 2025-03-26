package com.example.Farmer;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.Instant;
import java.util.Date;

public class AddPostFragment extends Fragment {



    EditText editText;
    Button postBtn;
    ImageButton imageButton;
    ImageView imageView;
    Uri imageUri;

    ProgressBar progressBar;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference postsCollection = db.collection("posts");
    CollectionReference usersCollection = db.collection("Users");
    FirebaseStorage storage = FirebaseStorage.getInstance();

    DocumentReference newPostRef = postsCollection.document();
    String postId = newPostRef.getId();

    StorageReference storageRef = storage.getReference("images/"+postId+".jpg");
    private int code=69;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_post, container, false);

        editText = view.findViewById(R.id.postContent);
        postBtn = view.findViewById(R.id.postButton);
        imageButton = view.findViewById(R.id.addImage);
        imageView = view.findViewById(R.id.displayImage);
        progressBar = view.findViewById(R.id.progressBar);

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String postContent = editText.getText().toString();
                if (postContent.isEmpty() && imageUri == null) {
                    // Show a toast or handle the case where neither text nor image is selected
                    Toast.makeText(getActivity(), "Please select an image or enter text", Toast.LENGTH_SHORT).show();
                } else {
                    // At least one of text or image is selected, proceed with posting
                    progressBar.setVisibility(View.VISIBLE);
                    feedPostData(postContent);
                }
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("Image Button", "Button Clicked");
                if (checkPermission()) {
                    fetchImageData();
                } else {
                    requestPermission();
                    if(checkPermission()){
                        fetchImageData();
                    }

                }
            }
        });
        return view;
    }


    //Used to pick image from a gallery
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            imageView = imageView.findViewById(R.id.displayImage);
            imageView.setImageURI(imageUri);
            imageView.setAlpha(1.0f);
        }
    }

    public void fetchImageData(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 3);
    }

    //Stores the image to Database Handles error while uploading file and retrieving download Url
    public Task<String> storeImageToDatabase(Uri imageUri) {
            return storageRef.putFile(imageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException(); // Handle upload failure
                        }
                        return storageRef.getDownloadUrl();
                    })
                    .continueWith(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException(); // Handle URL retrieval failure
                        }
                        String downloadUrl = task.getResult().toString();
                        return downloadUrl;
                    });
    }




    //Stores all the post content to database

    private void feedPostData(String postContent) {
        SharedPreferences preferences = requireActivity().getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        final String user_Id = preferences.getString("user_Id", null);

        if (user_Id != null) {
            if (imageUri != null) {
                storeImageToDatabase(imageUri)
                        .addOnSuccessListener(downloadUrl -> uploadDataToDatabase(user_Id, postContent, downloadUrl))
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);

                            Toast.makeText(getActivity(), "Image Upload Failed", Toast.LENGTH_SHORT).show();

                        });
            } else {
                // If imageUri is null, directly proceed to upload text
                uploadDataToDatabase(user_Id, postContent, null);
            }
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "User Id not Found", Toast.LENGTH_SHORT).show();
        }
    }




    //Upload the data to database
    private void uploadDataToDatabase(String user_Id, String postContent, String downloadUrl){

        if(downloadUrl != null) {
            // Generate a unique post ID
            String postId = postsCollection.document().getId();

            // Create a Post object with the post ID
            Post post = new Post(postId, postContent, Date.from(Instant.now()), user_Id, downloadUrl, 0, 0, 0, null);

            // Add the post to the postsCollection
            postsCollection.document(postId)
                    .set(post)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Update the user's postDetails in the usersCollection
                            usersCollection.document(user_Id).update("postDetails", FieldValue.arrayUnion(postId));

                            progressBar.setVisibility(View.GONE);
                            editText.setText("");
                            imageView.setImageResource(R.drawable.baseline_image_search_24);

                            Toast.makeText(requireContext(), "Post Added Successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), "Post Upload Failed" + e, Toast.LENGTH_SHORT).show();
                        }
                    });

        }else{
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Image upload Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestPermission(){
        Log.d("Permission", "Requesting Permission");
        ActivityCompat.requestPermissions(getActivity(), new String[]{
                android.Manifest.permission.READ_MEDIA_IMAGES
        }, code);

    }

    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_MEDIA_IMAGES);

                if(result == PackageManager.PERMISSION_GRANTED){
                    return true;
                }
                else{
                    return false;
                }
    }


}
