package com.example.Farmer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class PostGridAdapter extends BaseAdapter {

    private Context context;
    private List<Post> postList;

    public PostGridAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @Override
    public int getCount() {
        return postList.size();
    }

    @Override
    public Object getItem(int position) {
        return postList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item_post, parent, false);
        }

        ImageView postImageView = convertView.findViewById(R.id.postImageView);

        // Load the post image into ImageView using Picasso
        Picasso.get().load(postList.get(position).getImageUrl()).resize(300, 300).centerCrop().into(postImageView);

        return convertView;
    }
}
