package com.example.Farmer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class FullBlogActivity extends AppCompatActivity {

    private ImageView FullBlogImage, FullBlogProfileImage;
    private TextView FullBlogTitle, FullBlogUsername;
    private TextView FullBlogDescription;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_blog);

        FullBlogUsername = findViewById(R.id.full_blog_username);
        FullBlogImage = findViewById(R.id.blog_image_view);
        FullBlogProfileImage = findViewById(R.id.full_blog_profile_image);
        FullBlogTitle = findViewById(R.id.full_blog_title);
        FullBlogDescription = findViewById(R.id.full_blog_description);

        // Get the blog data from the intent
        String blogImage = getIntent().getStringExtra("blog_image");
        String fullBlogTitle = getIntent().getStringExtra("blog_title");
        String fullBlogDescription = getIntent().getStringExtra("blog_description");
        String blogUsername = getIntent().getStringExtra("blog_username");
        String blogProfImg = getIntent().getStringExtra("blog_profImg;");

        // Set the blog data in the views
        Picasso.get().load(blogImage).into(FullBlogImage);
        Picasso.get().load(blogImage).into(FullBlogProfileImage); //profileImage
        FullBlogUsername.setText(blogUsername);
        FullBlogTitle.setText(fullBlogTitle);
        FullBlogDescription.setText(Html.fromHtml(fullBlogDescription));
    }


}