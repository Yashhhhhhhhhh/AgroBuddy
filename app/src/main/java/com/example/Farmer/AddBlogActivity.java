package com.example.Farmer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import net.dankito.richtexteditor.android.RichTextEditor;
import net.dankito.richtexteditor.android.toolbar.AllCommandsEditorToolbar;

public class AddBlogActivity extends AppCompatActivity {
    private RichTextEditor editor;

    private EditText blogTitle;
    private AllCommandsEditorToolbar editorToolbar;
    Button submit, BSelectImage;
    ImageView IVPreviewImage;
    Uri imageUri;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference blogsCollec = db.collection("Blog_Post");
    CollectionReference blogscollection = db.collection("Blog_Post");


    DocumentReference newPostRef = blogsCollec.document();
    String blogId = newPostRef.getId();
    StorageReference storageRef = storage.getReference("images/"+blogId+".jpg");
    int SELECT_PICTURE = 200;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_blog);



        submit = findViewById(R.id.submit_blog);


        editor = (RichTextEditor) findViewById(R.id.editor);
        editorToolbar = (AllCommandsEditorToolbar) findViewById(R.id.editorToolbar);
        editorToolbar.setEditor(editor);
        editor.setEditorFontSize(20);
        editor.setPadding((int) (4 * getResources().getDisplayMetrics().density));

        blogTitle = findViewById(R.id.add_blog_title_edittext);
        BSelectImage = findViewById(R.id.imageslt);
        IVPreviewImage = findViewById(R.id.add_blog_imageView);
        /*
         some properties you also can set on editor
         editor.setEditorBackgroundColor(Color.YELLOW);
         editor.setEditorFontColor(Color.MAGENTA);
         editor.setEditorFontFamily("cursive");

         show keyboard right at start up
        editor.focusEditorAndShowKeyboardDelayed();*/

        BSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!blogTitle.getText().toString().isEmpty()
                        && !editor.getHtml().toString().isEmpty()) {
                    SharedPreferences preferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
                    String user_Id = preferences.getString("user_Id", null);

                    Toast.makeText(AddBlogActivity.this, "UserID"+ user_Id, Toast.LENGTH_SHORT).show();
                    if (user_Id != null && !user_Id.isEmpty()) {
                        String username = user_Id;
                        Toast.makeText(AddBlogActivity.this, "Usernamr"+ username, Toast.LENGTH_SHORT).show();

                        String addblogTitle = blogTitle.getText().toString();
                        String addblogDesc = editor.getHtml();
                        feedPostData(username, addblogTitle, addblogDesc);
                    } else {
                        // Handle the case where user_Id is null or empty
                        Toast.makeText(getApplicationContext(), "User ID not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    }
    @Override
    public void onBackPressed() {
        if(editorToolbar.handlesBackButtonPress() == false) {
            super.onBackPressed();
        }
    }

    void imageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    // Handle the result of image selection (onActivityResult)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            // Now you can use the selected image URI (e.g., display it in IVPreviewImage)
            IVPreviewImage.setImageURI(imageUri);
        }
    }


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
    //downloadUrl cha ithe profile pic cha taakaycha

    private void feedPostData(String blogUsername,String blog_title, String blog_desc) {
        storeImageToDatabase(imageUri).addOnSuccessListener(downloadUrl -> {
            uploadDataToDatabase(blogUsername, null, blog_title, blog_desc, downloadUrl);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
        });
    }
    private void uploadDataToDatabase(String blogUsername, String profImage ,String blogTitle, String blogDesc, String downloadUrl){

        BlogData Blogs = new BlogData(blogUsername,profImage,blogTitle, blogDesc,downloadUrl);

        //Add data to database
        blogscollection.add(Blogs)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(AddBlogActivity.this, "Blog added", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddBlogActivity.this, "Blog Not added", Toast.LENGTH_SHORT).show();
                        Log.w("isitworking", "Error adding document", e);
                    }
                });


    }
}
