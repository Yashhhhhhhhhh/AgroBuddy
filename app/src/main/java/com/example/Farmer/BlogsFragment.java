package com.example.Farmer;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Farmer.AddBlogActivity;
import com.example.Farmer.BlogData;
import com.example.Farmer.Blog_Adapter;
import com.example.Farmer.CheckDisease;
import com.example.Farmer.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BlogsFragment extends Fragment {

    private RecyclerView recyclerView;

    private Context context;
    private Blog_Adapter blogAdapter;
    private List<BlogData> blogs;
    private FloatingActionButton fab;
    private Button cam;
    private SearchView searchView;
    private boolean isInitialDataLoaded = false;
    private FirebaseFirestore db;
//    private CollectionReference usersCollection = db.collection("Users");


    public static int REQUEST_CODE = 1;
    private CollectionReference blogscollection;

    public BlogsFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blogs, container, false);

        recyclerView = view.findViewById(R.id.recyclerview);
        fab = view.findViewById(R.id.fab);
        cam = view.findViewById(R.id.camCheck);
        searchView = view.findViewById(R.id.searchView);

        db = FirebaseFirestore.getInstance();
        blogscollection = db.collection("Blog_Post");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        blogs = new ArrayList<>();
        blogAdapter = new Blog_Adapter(getContext(), blogs);
        recyclerView.setAdapter(blogAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent add_blog_intent = new Intent(getContext(), AddBlogActivity.class);
                startActivity(add_blog_intent);
            }
        });

        cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAIIntent();
            }
        });


        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // Handle search submission
                    if (!query.isEmpty()) {
                        // Perform the search only when the user submits the query
                        searchBlogs(query);
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

        if (!isInitialDataLoaded) {
            fetchData();
        }

        return view;
    }

    public void openAIIntent(){
        Intent check_disease_intent = new Intent(getContext(),CheckDisease.class);
        startActivityForResult(check_disease_intent,REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String searchText = data.getStringExtra("KEY_TEXT");
            if (searchText != null) {
                Toast.makeText(getContext(), searchText, Toast.LENGTH_SHORT).show();
                searchView.setQuery(searchText, false);
            }
        }
    }
    private void searchBlogs(String keyword) {
        // Clear existing items
        blogs.clear();

        // Fetch all blogs and associated data
        fetchBlogs(keyword);
    }

    private void fetchBlogs(String keyword) {
        // Construct a Firestore query to fetch all blogs
        blogscollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        BlogData item = document.toObject(BlogData.class);
                        item.setImageURL(document.getString("imageURL"));
                        String blogTitle = item.getTitle();

                        // Check if the blog title contains the keyword
                        if (blogTitle != null && blogTitle.toLowerCase().contains(keyword.toLowerCase())) {
                            blogs.add(item);
                        }
                    }

                    blogAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Search failed.", e);
                    Toast.makeText(getContext(), "Firestore error", Toast.LENGTH_SHORT).show();
                });
    }


    private Task<DocumentSnapshot> fetchUserDataForBlog(String userId, BlogData blog) {
        // Fetch user data for the given blog
        return blogscollection.document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String profilePhotoUrl = documentSnapshot.getString("imageUrl");
                    // Set profile photo URL for the blog
                    blog.setProfImage(profilePhotoUrl);
                });
    }

    // Method to filter blogs based on keyword
    private List<BlogData> filterBlogsByKeyword(List<BlogData> blogs, String keyword) {
        List<BlogData> filteredBlogs = new ArrayList<>();
        for (BlogData blog : blogs) {
            String blogTitle = blog.getTitle();
            if (blogTitle != null && blogTitle.toLowerCase().contains(keyword.toLowerCase())) {
                filteredBlogs.add(blog);
            }
        }
        return filteredBlogs;
    }


    private void fetchData() {
        blogscollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<BlogData> items = new ArrayList<>();
                    List<Map<String, Integer>> blogContents = new ArrayList<>();

                    // Initialize SimilarityCalculator
                    SimilarityCalculator calculator = new SimilarityCalculator(context);

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        BlogData item = document.toObject(BlogData.class);
                        item.setImageURL(document.getString("imageURL"));
                        String username = item.getUsername();
                        FirebaseFirestore.getInstance().collection("Users").document(username).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                item.setProfImage(documentSnapshot.getString("imageUrl"));
                            }
                        });
                        String blogTitle = item.getTitle();
                        String test = item.getDescription();
                        String blogDescription = String.valueOf(Html.fromHtml(test));

                        // Preprocess text and add to blog contents list
                        Map<String, Integer> preprocessedText = calculator.preprocessText(blogDescription);
                        blogContents.add(preprocessedText);

                        // Add item to list
                        items.add(item);
                    }

                    // Calculate similarity with each blog post
                    for (BlogData blog : items) {
                        Map<String, Integer> targetBlog = calculator.preprocessText(blog.getDescription());
                        double totalSimilarity = 0;

                        for (Map<String, Integer> otherBlog : blogContents) {
                            List<Map.Entry<String, Double>> similarities = calculator.calculateSimilarity(targetBlog, Collections.singletonList(otherBlog));

                            // Calculate similarity score
                            for (Map.Entry<String, Double> entry : similarities) {
                                totalSimilarity += entry.getValue();
                            }
                        }

                        // Calculate average similarity score
                        double avgSimilarity = totalSimilarity / blogContents.size();

                        // Set similarity score to the blog data object
                        blog.setSimilarityScore(avgSimilarity);
                    }

                    // Sort blogs based on similarity score
                    Collections.sort(items, new Comparator<BlogData>() {
                        @Override
                        public int compare(BlogData o1, BlogData o2) {
                            return Double.compare(o2.getSimilarityScore(), o1.getSimilarityScore());
                        }
                    });

                    blogs.clear();
                    blogs.addAll(items);
                    blogAdapter.notifyDataSetChanged();

                    // Now, do something with sorted items if needed

                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Fetch failed.", e);
                    Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
                });

    }

}
