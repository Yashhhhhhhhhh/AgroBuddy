package com.example.Farmer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class  HomeFragment extends Fragment implements OnLikeClickListener, OnCommentClickListener, OnShareClickListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference postsCollection = db.collection("posts");
    private CollectionReference usersCollection = db.collection("Users");

    private RecyclerView recyclerView;
    private List<Post> itemList;
    private Post_Adapter myAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    private String user_Id;
    private boolean isInitialDataLoaded = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        SharedPreferences preferences = requireActivity().getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        user_Id = preferences.getString("user_Id", null);

        recyclerView = view.findViewById(R.id.recyclerView);
        itemList = new ArrayList<>();
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        myAdapter = new Post_Adapter(itemList, this, this, this, user_Id);
        recyclerView.setAdapter(myAdapter);

        SearchView searchView = getActivity().findViewById(R.id.searchView);
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // Handle search submission
                    if (!query.isEmpty()) {
                        // Perform the search only when the user submits the query
                        searchPosts(query);
                    } else {
                        // If the search query is empty, you may want to show all posts or the previous data
                        fetchData();
                    }
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // Handle search text change (optional)
                    // You can leave this method empty or use it for real-time filtering while the user types
                    return true;
                }
            });
        } else {
            Toast.makeText(getContext(), "Search view is null", Toast.LENGTH_SHORT).show();
        }

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (isNetworkConnected()) {
                fetchData();
            } else {
                Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
            swipeRefreshLayout.setRefreshing(false);
        });

        if (!isInitialDataLoaded && isNetworkConnected()) {
            fetchData();
        } else if (!isNetworkConnected()) {
            Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void searchPosts(String keyword) {
        // Clear existing items
        itemList.clear();
        List<Task<?>> tasks = new ArrayList<>();

        // Construct a Firestore query to fetch all posts
        postsCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Task<?>> postTasks = new ArrayList<>(); // Track tasks for individual posts
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Post item = document.toObject(Post.class);
                        item.setImageUrl(document.getString("imageUrl"));

                        // Fetch likes for each post
                        Task<DocumentSnapshot> likesTask = postsCollection.document(item.getPostId()).get()
                                .addOnSuccessListener(postSnapshot -> {
                                    if (postSnapshot.exists()) {
                                        Long noOfLikes = postSnapshot.getLong("noOfLikes");
                                        if (noOfLikes != null) {
                                            item.setNoOfLikes(noOfLikes.intValue());
                                        } else {
                                            item.setNoOfLikes(0);
                                        }
                                    }
                                });


                        String postUserId = item.getUsername();

                        Task<DocumentSnapshot> userTask = usersCollection.document(postUserId).get().addOnSuccessListener(documentSnapshot -> {
                            String profilePhotoUrl = documentSnapshot.getString("imageUrl");
                            item.setProfilePhotoUrl(profilePhotoUrl);
                        });

                        // Add tasks for each post to postTasks list
                        postTasks.add(likesTask);
                        postTasks.add(userTask);
                    }

                    // Wait for all tasks for individual posts to complete
                    Task<List<Task<?>>> allPostsTask = Tasks.whenAllComplete(postTasks);

                    // Once all post tasks are completed, filter and sort posts
                    allPostsTask.addOnSuccessListener(results -> {
                        List<Post> items = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Post item = document.toObject(Post.class);
                            item.setImageUrl(document.getString("imageUrl"));

                            // Add the post to the list only if it matches the search keyword
                            if (item.getContent() != null && item.getContent().toLowerCase().contains(keyword.toLowerCase())) {
                                items.add(item);
                            }
                        }

                        // Sort filtered posts based on likes and recency
                        sortPosts(items);

                        itemList.addAll(items);
                        myAdapter.notifyDataSetChanged();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Search failed.", e);
                    Toast.makeText(getContext(), "Firestore error", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to filter posts based on keyword
    private List<Post> filterPostsByKeyword(List<Post> posts, String keyword) {
        List<Post> filteredPosts = new ArrayList<>();
        for (Post post : posts) {
            String postContent = post.getContent();
            if (postContent != null && postContent.toLowerCase().contains(keyword.toLowerCase())) {
                filteredPosts.add(post);
            }
        }
        return filteredPosts;
    }

    // Method to sort posts based on likes and recency
    private void sortPosts(List<Post> posts) {
        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post post1, Post post2) {
                // First compare based on number of likes
                int compareLikes = Integer.compare(post2.getNoOfLikes(), post1.getNoOfLikes());
                if (compareLikes != 0) {
                    return compareLikes; // Descending order of likes
                } else {
                    // If likes are equal, compare based on recency
                    long time1 = calculatePostTimeInMillis(post1.getTimeStamp());
                    long time2 = calculatePostTimeInMillis(post2.getTimeStamp());
                    return Long.compare(time2, time1); // Descending order of time
                }
            }
        });
    }

    private void fetchData() {
        List<Task<?>> tasks = new ArrayList<>();

        postsCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Post> items = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Post item = document.toObject(Post.class);
                item.setImageUrl(document.getString("imageUrl"));

                // Fetch likes for each post
                Task<DocumentSnapshot> likesTask = postsCollection.document(item.getPostId()).get()
                        .addOnSuccessListener(postSnapshot -> {
                            if (postSnapshot.exists()) {
                                Long noOfLikes = postSnapshot.getLong("noOfLikes");
                                if (noOfLikes != null) {
                                    item.setNoOfLikes(noOfLikes.intValue());
                                } else {
                                    item.setNoOfLikes(0);
                                }
                            }
                        });

                // Fetch profile photo URL for each post
                Task<DocumentSnapshot> userTask = usersCollection.document(item.getUsername()).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            String profilePhotoUrl = documentSnapshot.getString("imageUrl");
                            item.setProfilePhotoUrl(profilePhotoUrl);
                        });

                tasks.add(likesTask);
                tasks.add(userTask);

                items.add(item);
            }

            // Update RecyclerView with fetched data
            Tasks.whenAllComplete(tasks).addOnSuccessListener(results -> {
                itemList.clear();
                itemList.addAll(items);
                myAdapter.notifyDataSetChanged();
                isInitialDataLoaded = true;
            });
        }).addOnFailureListener(e -> {
            Log.e("FirestoreError", "Fetch failed.", e);
            Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
        });
    }


    private long calculatePostTimeInMillis(String timeStamp) {
        // Assuming timeStamp is in the format "dd/MM/yyyy HH:mm:ss"
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        try {
            Date date = sdf.parse(timeStamp);
            if (date != null) {
                return date.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0; // Return 0 if parsing fails or timestamp is null
    }



    public void onLikeClick(int position) {

        SharedPreferences preferences = requireActivity().getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
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
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }


    }


    public void onCommentClick(int position) {

        Post clickedPost = itemList.get(position);

        Bundle bundle = new Bundle();
        bundle.putString("postId", clickedPost.getPostId());

        fragment_comment commentFragment = new fragment_comment();
        commentFragment.setArguments(bundle);

        commentFragment.show(getChildFragmentManager(), commentFragment.getTag());

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
            Toast.makeText(getContext(), "Post content is empty", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getContext(), "Failed to create temporary file", Toast.LENGTH_SHORT).show();
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
            Uri imageUri = FileProvider.getUriForFile(getContext(), "com.example.fileprovider", compositeFilePath);

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
            Toast.makeText(getContext(), "Failed to download image", Toast.LENGTH_SHORT).show();
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
            File cachePath = new File(getContext().getCacheDir(), "images");
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