package com.example.Farmer;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class Post_Adapter extends RecyclerView.Adapter<Post_Adapter.MyViewHolder> {

    private List<Post> itemList;
    private OnLikeClickListener likeClickListener;
    private OnCommentClickListener commentClickListener;
    private OnShareClickListener shareClickListener;
    private String userId;


    public Post_Adapter(List<Post> itemList, OnLikeClickListener likeClickListener, OnCommentClickListener commentClickListener, OnShareClickListener shareClickListener, String userId) {
        this.itemList = itemList;
        this.likeClickListener = likeClickListener;
        this.commentClickListener = commentClickListener;
        this.shareClickListener = shareClickListener;
        this.userId = userId;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Post item = itemList.get(position);
        holder.userName.setText(item.getUsername());
        holder.timeStamp.setText(item.getTimeStamp());
        holder.postDescription.setText(item.getContent());

        int likesCount = item.getNoOfLikes();
        String likesText;
        if (likesCount == 1) {
            likesText = likesCount + " like";
        } else {
            likesText = likesCount + " likes";
        }
        holder.noOfLikes.setText(likesText);

        if (item.getNoOfLikes() > 0) {
            holder.noOfLikes.setVisibility(View.VISIBLE);
        } else {
            holder.noOfLikes.setVisibility(View.GONE);
        }


        Picasso.get().load(item.getImageUrl()).into(holder.postImage);
        Picasso.get().load(item.getProfilePhotoUrl()).into(holder.profileImage);

        // Check if the user has liked the post and update like button accordingly
        boolean isLiked = item.getLikes() != null && item.getLikes().contains(userId);
        holder.likeButton.setImageResource(isLiked ? R.drawable.after_like : R.drawable.before_like);

        // Add onClickListener for likeButton
        holder.likeButton.setOnClickListener(view -> {
            if (likeClickListener != null) {
                likeClickListener.onLikeClick(holder.getAdapterPosition());
            }
        });

        // Add onClickListener for commentButton
        holder.commentButton.setOnClickListener(view -> {
            if (commentClickListener != null) {
                commentClickListener.onCommentClick(holder.getAdapterPosition());
            }
        });

        // Add onClickListener for shareButton
        holder.shareButton.setOnClickListener(view -> {
            if (shareClickListener != null) {
                shareClickListener.onShareClick(holder.getAdapterPosition());
            }
        });
    }




    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView postImage, profileImage;
        TextView userName, noOfLikes, timeStamp, postDescription;
        ImageButton likeButton, commentButton, shareButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            timeStamp = itemView.findViewById(R.id.timeStamp);
            postImage = itemView.findViewById(R.id.postImage);
            postDescription = itemView.findViewById(R.id.postDescription);
            likeButton = itemView.findViewById(R.id.likeButton);
            commentButton = itemView.findViewById(R.id.commentButton);
            shareButton = itemView.findViewById(R.id.shareButton);
            profileImage = itemView.findViewById(R.id.profilePhoto);
            noOfLikes = itemView.findViewById(R.id.noOfLikes);
        }
    }
}


interface OnCommentClickListener {
    void onCommentClick(int position);
}

interface OnLikeClickListener {
    void onLikeClick(int position);
}

interface OnShareClickListener {
    void onShareClick(int position);
}




