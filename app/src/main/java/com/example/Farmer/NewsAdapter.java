package com.example.Farmer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Farmer.NewsItem;
import com.example.Farmer.R;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private List<NewsItem> newsItemList;

    public NewsAdapter() {
        this.newsItemList = new ArrayList<>();
    }

    public void setData(List<NewsItem> newsItemList) {
        this.newsItemList.clear();
        this.newsItemList.addAll(newsItemList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsItem item = newsItemList.get(position);

        holder.textView.setText(item.getNewsText());
        holder.referenceTextView.setText(item.getSourceUrl()); // Set the reference URL

        holder.moreInfoButton.setTag(item.getLink());

        // Set an OnClickListener for the button
        holder.moreInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the link from the button's tag
                String link = (String) view.getTag();

                // Check if the link is not empty or null
                if (link != null && !link.isEmpty()) {
                    // Check if there is a web browser available
                    if (isWebBrowserAvailable(view.getContext())) {
                        System.out.println("Opening browser with link: " + link);
                        // Open the web browser using an Intent
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                        view.getContext().startActivity(browserIntent);
                    } else {
                        Toast.makeText(view.getContext(), "Browser not available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(view.getContext(), "Link not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsItemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView, referenceTextView;
        Button moreInfoButton;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.newsTextView);
            moreInfoButton = itemView.findViewById(R.id.moreInfoButton);
            referenceTextView = itemView.findViewById(R.id.referenceTextView); // Add this line to initialize referenceTextView

            referenceTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = referenceTextView.getText().toString();
                    if (!url.isEmpty()) {
                        // Open the web browser using an Intent
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        view.getContext().startActivity(browserIntent);
                    }
                }
            });
        }
    }

    private boolean isWebBrowserAvailable(Context context) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        return context.getPackageManager().resolveActivity(browserIntent, 0) != null;
    }
}