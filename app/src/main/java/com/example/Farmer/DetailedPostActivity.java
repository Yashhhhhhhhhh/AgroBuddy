package com.example.Farmer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// ... (Your existing imports)

public class DetailedPostActivity extends AppCompatActivity implements OnLikeClickListener, OnCommentClickListener, OnShareClickListener {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference postsCollection = db.collection("posts");
    private CollectionReference usersCollection = db.collection("Users");

    private RecyclerView recyclerView;
    private List<Post> itemList;
    private Post_Adapter myAdapter;
    String user_Id;

    private FirebaseFirestore firestore;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_post);

        SharedPreferences preferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        user_Id = preferences.getString("user_Id", null);
        recyclerView = findViewById(R.id.recyclerView);
        itemList = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);


        myAdapter = new Post_Adapter(itemList, this, this, this, user_Id);
        recyclerView.setAdapter(myAdapter);


        // Retrieve post details from the intent
        String postId = getIntent().getStringExtra("postId");

        // Fetch post details from Firestore based on the postId
        fetchData(postId);
    }

    private void fetchData(String postId) {
        List<Task<?>> tasks = new ArrayList<>();

        postsCollection.document(postId).get().addOnSuccessListener(postDocumentSnapshot -> {
            if (postDocumentSnapshot.exists()) {
                Post item = postDocumentSnapshot.toObject(Post.class);
                item.setImageUrl(postDocumentSnapshot.getString("imageUrl"));

                // Fetch likes for the post
                Task<DocumentSnapshot> likesTask = postsCollection.document(postId).get()
                        .addOnSuccessListener(likesDocumentSnapshot -> {
                            if (likesDocumentSnapshot.exists()) {
                                Long noOfLikes = likesDocumentSnapshot.getLong("noOfLikes");
                                if (noOfLikes != null) {
                                    item.setNoOfLikes(noOfLikes.intValue());
                                } else {
                                    item.setNoOfLikes(0);
                                }
                            }
                        });

                // Fetch profile photo URL for the post
                Task<DocumentSnapshot> userTask = usersCollection.document(item.getUsername()).get()
                        .addOnSuccessListener(userDocumentSnapshot -> {
                            String profilePhotoUrl = userDocumentSnapshot.getString("imageUrl");
                            item.setProfilePhotoUrl(profilePhotoUrl);
                        });

                tasks.add(likesTask);
                tasks.add(userTask);

                // Update RecyclerView with fetched data
                Tasks.whenAllComplete(tasks).addOnSuccessListener(results -> {
                    itemList.clear();
                    itemList.add(item);
                    myAdapter.notifyDataSetChanged();
                });
            } else {
                Toast.makeText(getApplicationContext(), "Post not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("FirestoreError", "Fetch failed.", e);
            Toast.makeText(getApplicationContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
        });
    }

    public void onLikeClick(int position) {

        SharedPreferences preferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        String user_Id = preferences.getString("user_Id", null);
        if (user_Id != null) {
            Post clickedPost = itemList.get(position);

            // Check if the user has already liked the post
            if (clickedPost.getLikes() == null || !clickedPost.getLikes().contains(user_Id)) {
                // If not liked, add the user ID to the likes list
                clickedPost.addLike(user_Id);
                clickedPost.setNoOfLikes(clickedPost.getNoOfLikes() + 1);
            } else {
                // If already liked, remove the user ID from the likes list
                clickedPost.removeLike(user_Id);
                clickedPost.setNoOfLikes(clickedPost.getNoOfLikes() - 1);
            }


            postsCollection.document(clickedPost.getPostId())
                    .update("likes", clickedPost.getLikes(), "noOfLikes", clickedPost.getNoOfLikes())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            myAdapter.notifyItemChanged(position, "likes");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("FirestoreError", "Update failed.", e);
                        }
                    });
//
        } else {
            // User is not logged in, handle accordingly
            Toast.makeText(getApplicationContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }


    }


    public void onCommentClick(int position) {

        Post clickedPost = itemList.get(position);

        Bundle bundle = new Bundle();
        bundle.putString("postId", clickedPost.getPostId());

        fragment_comment commentFragment = new fragment_comment();
        commentFragment.setArguments(bundle);

        commentFragment.show(getSupportFragmentManager(), commentFragment.getTag());

    }


    public void onShareClick(int position) {

        Post clickedPost = itemList.get(position);
        // Get the content of the post
        String content = clickedPost.getContent();

        // Check if the content is not null or empty
        if (content != null && !content.isEmpty()) {
            // Create the shareable text message with the content
            String shareMessage = "Post Content: " + content;
            downloadImageAndShare(clickedPost.getImageUrl(), clickedPost.getUsername(), clickedPost.getTimeStamp(), content);
            // Create an intent to share the text
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

            // Start the chooser activity to share the content
            //     startActivity(Intent.createChooser(shareIntent, "Share Post"));
        } else {
            // If content is null or empty, show a message to the user
            Toast.makeText(getApplicationContext(), "Post content is empty", Toast.LENGTH_SHORT).show();
        }
    }


    private void downloadImageAndShare(String imageUrl, String username, String timeStamp, String content) {
        // Obtain a reference to Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);

        // Create a temporary file to save the downloaded image
        File cachePath;
        try {
            cachePath = File.createTempFile("temp_image", ".jpg");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Failed to create temporary file", Toast.LENGTH_SHORT).show();
            return;
        }

        // Download the image file to the temporary file
        storageRef.getFile(cachePath).addOnSuccessListener(taskSnapshot -> {
            // Image downloaded successfully, create a composite image
            Bitmap imageBitmap = BitmapFactory.decodeFile(cachePath.getAbsolutePath());
            Bitmap compositeBitmap = createCompositeImage(imageBitmap, username, timeStamp);

            // Save the composite image to a temporary file
            File compositeFilePath = saveCompositeImage(compositeBitmap);

            // Create a content URI for sharing
            Uri imageUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.fileprovider", compositeFilePath);

            // Create the shareable text message with the content
            StringBuilder shareMessage = new StringBuilder();
            shareMessage.append("Post Content: ").append(content);

            // Create an intent to share both the image and text
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage.toString());
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setType("image/jpeg");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Grant read permission to the receiving app

            // Start the chooser activity to share the content
            startActivity(Intent.createChooser(shareIntent, "Share Post"));
        }).addOnFailureListener(exception -> {
            // Image download failed, show an error message
            Toast.makeText(getApplicationContext(), "Failed to download image", Toast.LENGTH_SHORT).show();
        });
    }


    private Bitmap createCompositeImage(Bitmap imageBitmap, String username, String timeStamp) {
        // Create a bitmap with extra height to accommodate text above the image
        int extraHeight = 100; // Adjust this value as needed
        Bitmap compositeBitmap = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight() + extraHeight, imageBitmap.getConfig());

        // Create a canvas for drawing on the composite image
        Canvas canvas = new Canvas(compositeBitmap);

        // Draw the original image on the canvas
        canvas.drawBitmap(imageBitmap, 0, extraHeight, null);

        // Create a paint object for styling the text
        Paint paint = new Paint();
        paint.setColor(Color.WHITE); // Text color
        paint.setTextSize(24); // Text size

        // Draw the username and timestamp above the image
        int textStartY = 20; // Starting Y position for the text
        canvas.drawText("Username: " + username, 20, textStartY, paint);
        canvas.drawText("Time: " + timeStamp, 20, textStartY + 40, paint);

        return compositeBitmap;
    }


    private File saveCompositeImage(Bitmap compositeBitmap) {
        // Save the composite bitmap to a file in the cache directory
        try {
            File cachePath = new File(getApplicationContext().getCacheDir(), "images");
            cachePath.mkdirs();
            File compositeFile = new File(cachePath, "composite_image.jpg");
            FileOutputStream stream = new FileOutputStream(compositeFile);
            compositeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.close();
            return compositeFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}