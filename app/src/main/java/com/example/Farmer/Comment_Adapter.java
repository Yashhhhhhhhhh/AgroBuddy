package com.example.Farmer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class Comment_Adapter extends RecyclerView.Adapter<Comment_Adapter.MyViewHolder>{

    private List<Comment> commentList;

    public Comment_Adapter(List<Comment> commentList) {

        this.commentList = commentList;
    }

    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Comment item = commentList.get(holder.getAdapterPosition());
        holder.comment_userName.setText(item.getComment_username());
        holder.commentContent.setText(item.getComment_Content());
        Picasso.get().load(item.getProfileImageUrl()).into(holder.profileImageUrl);

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView profileImageUrl;
        TextView comment_userName, commentContent;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            comment_userName = itemView.findViewById(R.id.comment_userName);
            commentContent = itemView.findViewById(R.id.comment_Content);
            profileImageUrl = itemView.findViewById(R.id.profileImageUrl);

        }
    }
}
