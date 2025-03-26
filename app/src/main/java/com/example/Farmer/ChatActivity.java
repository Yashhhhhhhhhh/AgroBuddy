package com.example.Farmer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private CollectionReference messagesRef;
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private EditText messageEditText;
    private TextView receiverUsernameTextView;
    private Button sendButton;
    public static String senderId;
    private String receiverId;
    private String farmerOrderId;
    private String consumersTransactionId;
    private String receiverUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ImageView profileImageView = findViewById(R.id.profileImageView);

        db = FirebaseFirestore.getInstance();
        receiverUsername = getIntent().getStringExtra("receiverUsername");
        farmerOrderId = getIntent().getStringExtra("farmersOrderId");
        consumersTransactionId = getIntent().getStringExtra("consumersTransactionId");

        messagesRef = db.collection("Chats")
                .document(farmerOrderId)
                .collection("ConsumersTransactionId")
                .document(consumersTransactionId)
                .collection("Messages");


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter();
        recyclerView.setAdapter(adapter);

        SharedPreferences preferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        senderId = preferences.getString("user_Id", null);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        receiverUsernameTextView = findViewById(R.id.receiverUsername);
        //   receiverUsernameTextView.setText("" + receiverUsername);

        FirebaseFirestore.getInstance().collection("Users")
                .whereEqualTo("username", receiverUsername)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String profilePicUrl = documentSnapshot.getString("imageUrl");
                        String displayname = documentSnapshot.getString("fullName");
                        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                            // Load the image into ImageView using Picasso
                            Picasso.get().load(profilePicUrl).into(profileImageView);
                            receiverUsernameTextView.setText("" + displayname);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(ChatActivity.this, "Failed to load profile picture", Toast.LENGTH_SHORT).show();
                });


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageEditText.getText().toString().trim();
                if (!messageText.isEmpty()) {
                    sendMessage(messageText);
                } else {
                    Toast.makeText(ChatActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        messagesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(ChatActivity.this, "Error fetching messages", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    List<Message> messages = new ArrayList<>();
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                Message message = dc.getDocument().toObject(Message.class);
                                messages.add(message);
                                break;
                            case MODIFIED:
                                // Handle modified messages if needed
                                break;
                            case REMOVED:
                                // Handle removed messages if needed
                                break;
                        }
                    }

                    // Sort messages by timestamp
                    Collections.sort(messages, new Comparator<Message>() {
                        @Override
                        public int compare(Message m1, Message m2) {
                            return m1.getTimestamp().compareTo(m2.getTimestamp());
                        }
                    });

                    // Clear the adapter and add sorted messages
                    adapter.clearMessages();
                    adapter.addMessages(messages);
                    // Scroll to the last item
                    scrollToLastItem();
                }
            }
        });
    }

    private void scrollToLastItem() {
        if (adapter.getItemCount() > 0) {
            recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    private void sendMessage(String messageText) {
        Message message = new Message(senderId, receiverUsername, messageText, new Date());

        messagesRef.add(message)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            messageEditText.setText("");
                            Toast.makeText(ChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                            // Fetch updated messages after sending a new message
                            fetchMessages();
                        } else {
                            Toast.makeText(ChatActivity.this, "Error sending message", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void fetchMessages() {
        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Message> messages = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                Message message = document.toObject(Message.class);
                                messages.add(message);
                            }
                            // Clear the adapter and add sorted messages
                            adapter.clearMessages();
                            adapter.addMessages(messages);
                            // Scroll to the last item
                            scrollToLastItem();
                        } else {
                            Toast.makeText(ChatActivity.this, "Error fetching messages", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}