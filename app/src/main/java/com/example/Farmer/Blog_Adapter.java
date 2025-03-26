package com.example.Farmer;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
//import com.bumptech.glide.Glide;

import java.util.List;

public class Blog_Adapter extends RecyclerView.Adapter<Blog_Adapter.MyViewHolder> {
    Context context;
    List<BlogData> Bloglist;

    public Blog_Adapter(Context context, List<BlogData> bloglist) {
        this.context = context;
        this.Bloglist = bloglist;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_post_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        BlogData blog = Bloglist.get(position);
        holder.tvTitle.setText(Bloglist.get(position).getTitle());
        Picasso.get().load(Bloglist.get(position).getImageURL()).into(holder.imgBlog);
        Picasso.get().load(Bloglist.get(position).getProfImage()).into(holder.profileImage);
        holder.tvDesc.setText(Html.fromHtml(Bloglist.get(position).getDescription()));


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new Intent to open the full blog activity
                Intent intent = new Intent(v.getContext(), FullBlogActivity.class);
                intent.putExtra("blog_username",blog.getUsername());
                intent.putExtra("blog_profImg",blog.getProfImage());// profile image ithe taak
                intent.putExtra("blog_title",blog.getTitle());
                intent.putExtra("blog_image",blog.getImageURL());
                intent.putExtra("blog_description", blog.getDescription());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return Bloglist.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tvTitle, tvDesc;
        ImageView imgBlog, profileImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.row_blog_title);
            tvDesc = itemView.findViewById(R.id.row_blog_desc);
            imgBlog = itemView.findViewById(R.id.row_blog_img);
            profileImage = itemView.findViewById(R.id.row_blog_profile_img);
        }
    }
}
