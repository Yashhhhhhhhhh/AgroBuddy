package com.example.Farmer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NewsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NewsAdapter customAdapter;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(getContext()));
        }

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        customAdapter = new NewsAdapter();
        recyclerView.setAdapter(customAdapter);

        progressBar = view.findViewById(R.id.progressBar);

        // Fetch and populate data including links
        new FetchDataAsyncTask().execute();

        return view;
    }

    // AsyncTask is now an inner class of the Fragment
    @SuppressLint("StaticFieldLeak")
    private class FetchDataAsyncTask extends AsyncTask<Void, Void, List<NewsItem>> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<NewsItem> doInBackground(Void... voids) {
            List<NewsItem> newsItemList = new ArrayList<>();

            try {
                Python py = Python.getInstance();
                PyObject module = py.getModule("web_scrap");
                PyObject getInfo1Function = module.get("get_info1");
                PyObject getInfo2Function = module.get("get_info2");
                PyObject getInfo3Function = module.get("get_info3");
                PyObject getLinks1Function = module.get("get_links1");
                PyObject getLinks2Function = module.get("get_links2");
                PyObject getLinks3Function = module.get("get_links3");
                PyObject getURL1Function = module.get("get_url1");
                PyObject getURL2Function = module.get("get_url2");
                PyObject getURL3Function = module.get("get_url3");

                // Fetch links from Python functions
                List<String> links1 = fetchLinks(getLinks1Function);
                List<String> links2 = fetchLinks(getLinks2Function);
                List<String> links3 = fetchLinks(getLinks3Function);

                // Fetch news from Python functions
                List<String> news1 = fetchNews(getInfo1Function);
                List<String> news2 = fetchNews(getInfo2Function);
                List<String> news3 = fetchNews(getInfo3Function);

                //fetch url from python function
                String url1 = fetchURL(getURL1Function);
                String url2 = fetchURL(getURL2Function);
                String url3 = fetchURL(getURL3Function);

                // Combine news and links for display
                List<NewsItem> newsItems1 = combineNewsAndLinks(news1, links1, url1.toString());
                List<NewsItem> newsItems2 = combineNewsAndLinks(news2, links2, url2.toString());
                List<NewsItem> newsItems3 = combineNewsAndLinks(news3, links3, url3.toString());

                // Combine results
                newsItemList.addAll(newsItems1);
                newsItemList.addAll(newsItems2);
                newsItemList.addAll(newsItems3);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return newsItemList;
        }

        @Override
        protected void onPostExecute(List<NewsItem> result) {
            customAdapter.setData(result);
            progressBar.setVisibility(View.GONE);
        }

        private List<String> fetchLinks(PyObject getLinksFunction) {
            List<String> links = new ArrayList<>();
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    links = getLinksFunction.call().asList().stream()
                            .map(PyObject::toString)
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return links;
        }

        private List<String> fetchNews(PyObject getInfoFunction) {
            List<String> news = new ArrayList<>();
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    news = getInfoFunction.call().asList().stream()
                            .map(PyObject::toString)
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return news;
        }

        private String fetchURL(PyObject getURLFunction) {
            String url = ""; // Initialize as an empty string
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    // Call the Python function to get the URL
                    url = getURLFunction.call().toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return url;
        }

        private List<NewsItem> combineNewsAndLinks(List<String> news, List<String> links, String sourceUrl) {
            List<NewsItem> newsItemList = new ArrayList<>();

            // Get the combined URLs string
            String combinedUrls = combineUrls(links);

            // Ensure that both news and links have the same size
            int size = Math.min(news.size(), links.size());

            for (int i = 0; i < size; i++) {
                String newsText = news.get(i);
                String link = links.get(i);
                newsItemList.add(new NewsItem(newsText, link, sourceUrl)); // Pass the source URL
            }

            return newsItemList;
        }

        private String combineUrls(List<String> urls) {
            // Join the URLs into a single string separated by newline character
            return String.join(" ", urls);
        }
    }
}
