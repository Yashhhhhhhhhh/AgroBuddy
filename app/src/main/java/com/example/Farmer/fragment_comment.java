package com.example.Farmer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class fragment_comment extends BottomSheetDialogFragment {

    private String post_Id;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference commentCollection;
    CollectionReference usersCollection = db.collection("Users");
    ;
    List<Comment> comment_List;
    Comment_Adapter myAdapter;

    RecyclerView corecyclerView;

    Button buttonPostComment;
    EditText editTextComment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this bottom sheet dialog fragment
        View view = inflater.inflate(R.layout.fragment_comment, container, false);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_BottomSheetDialog);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Bundle arguments = getArguments();
        if (arguments != null) {
            post_Id = arguments.getString("postId");
        }else{
            Toast.makeText(getContext(), "Post id not found", Toast.LENGTH_SHORT).show();
        }


        editTextComment = getView().findViewById(R.id.editTextComment);
        buttonPostComment = getView().findViewById(R.id.buttonPostComment);
        corecyclerView = getView().findViewById(R.id.commentRecyclerView);
        comment_List = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        corecyclerView.setLayoutManager(linearLayoutManager);

        myAdapter = new Comment_Adapter(comment_List);
        corecyclerView.setAdapter(myAdapter);

        if(post_Id != null) {
            commentCollection = db.collection("posts").document(post_Id).collection("comments");

            fetchCommentData();

        }
        else{
            Toast.makeText(getContext(), "post id not found", Toast.LENGTH_SHORT).show();
        }




        buttonPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (post_Id != null) {

                    SharedPreferences preferences = requireActivity().getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
                    String user_Id = preferences.getString("user_Id", null);

                    String commentText = String.valueOf(editTextComment.getText());

                    Comment comment = new Comment(user_Id, commentText);

                    if (commentText != null) {
                        commentCollection.add(comment).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(getContext(), "Comment Added on Post", Toast.LENGTH_SHORT).show();
                                dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Failed to Comment", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Comment can't be blank", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

    }

    public void fetchCommentData() {
        // Fetch data once when needed
        commentCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comment> items = new ArrayList<>();
                    int totalProfilesToFetch = queryDocumentSnapshots.size();
                    int[] profilesFetched = {0};  // Using an array as a mutable container

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Comment item = document.toObject(Comment.class);

                        String post_User_Id = item.getComment_username();
                        usersCollection.document(post_User_Id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String profilePhotoUrl = documentSnapshot.getString("imageUrl");
                                item.setProfileImageUrl(profilePhotoUrl);

                                // Increment the counter
                                profilesFetched[0]++;

                                // Check if all profiles are fetched
                                if (profilesFetched[0] == totalProfilesToFetch) {
                                    // Update UI with the fetched data when all profiles are retrieved
                                    comment_List.clear();
                                    comment_List.addAll(items);
                                    myAdapter.notifyDataSetChanged();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle the failure
                                Log.e("FirestoreError", "Profile fetch failed.", e);
                            }
                        });

                        // Check if the item is not already in the itemList
                        if (!comment_List.contains(item)) {
                            items.add(item);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Fetch failed.", e);
                    Toast.makeText(getContext(), "Firestore Error", Toast.LENGTH_SHORT).show();
                    // Handle the error
                });
    }




}
