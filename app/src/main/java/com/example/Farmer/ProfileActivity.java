package com.example.Farmer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private Button changeProfilePictureButton;
    private TextView usernameTextView;
    private TextView fullNameTextView;
    private GridView postsGridView;

    private FirebaseFirestore firestore;

    private String user_Id;

    private List<Post> userPostsList;
    private PostGridAdapter postsGridAdapter;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImageView = findViewById(R.id.profileImageView);
        changeProfilePictureButton = findViewById(R.id.changeProfilePictureButton);
        usernameTextView = findViewById(R.id.usernameTextView);
        postsGridView = findViewById(R.id.postsGridView);
        fullNameTextView = findViewById(R.id.fullNameTextView);
        firestore = FirebaseFirestore.getInstance();

        SharedPreferences preferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        user_Id = preferences.getString("user_Id", null);

        // Fetch user details and update UI
        fetchUserDetails();

        // Fetch user posts and update GridView
        fetchUserPosts();

        changeProfilePictureButton.setOnClickListener(view -> {
            // Handle profile picture change logic (e.g., open gallery, upload new image)
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
        });

        postsGridView.setOnItemClickListener((parent, view, position, id) -> {
            // Handle click event, e.g., open the detailed post activity
            openDetailedPostActivity(userPostsList.get(position));
        });
    }

    private void openDetailedPostActivity(Post post) {
        Intent intent = new Intent(ProfileActivity.this, DetailedPostActivity.class);
        intent.putExtra("postId", post.getPostId());
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            // Load the selected image into the profileImageView using Picasso
            Picasso.get().load(imageUri).transform(new RoundedTransformation()).into(profileImageView);

            // Update the user's profile image URL in Firestore
            updateProfileImageInFirestore(imageUri);
        }
    }

    private void updateProfileImageInFirestore(Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("profile_images/" + user_Id + ".jpg");
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        // Now update the user's profileImageUrl in Firestore
                        updateProfileImageUrlInFirestore(downloadUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle the failure case
                    Toast.makeText(ProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfileImageUrlInFirestore(String downloadUrl) {
        firestore.collection("Users")
                .document(user_Id)
                .update("imageUrl", downloadUrl)
                .addOnSuccessListener(aVoid -> {
                    // Handle success
                    Toast.makeText(ProfileActivity.this, "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(ProfileActivity.this, "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchUserDetails() {
        if (user_Id != null) {
            firestore.collection("Users")
                    .document(user_Id)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            String fullName = documentSnapshot.getString("fullName");
                            String profileImageUrl = documentSnapshot.getString("imageUrl");
                            // Update UI with fetched user details
                            usernameTextView.setText(username);
                            fullNameTextView.setText(fullName);

                            // Load rounded profile image using Picasso
                            Picasso.get().load(profileImageUrl).placeholder(R.drawable.ic_camera)
                                    .transform(new RoundedTransformation()).into(profileImageView);
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to fetch user details", Toast.LENGTH_SHORT).show());
        } else {
            // Handle the case when user_Id is null
            Toast.makeText(ProfileActivity.this, "User ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    class RoundedTransformation implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "rounded";
        }
    }

    private void fetchUserPosts() {
        firestore.collection("posts")
                .whereEqualTo("username", user_Id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userPostsList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Post post = document.toObject(Post.class);
                        userPostsList.add(post);
                    }
                    setupPostsGridView();
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to fetch user posts", Toast.LENGTH_SHORT).show());
    }

    private void setupPostsGridView() {
        postsGridAdapter = new PostGridAdapter(ProfileActivity.this, userPostsList);
        postsGridView.setAdapter(postsGridAdapter);
    }
}
