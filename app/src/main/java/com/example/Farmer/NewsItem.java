package com.example.Farmer;

public class NewsItem {
    private String newsText;
    private String link;
    private String sourceUrl; // Add a field for the URL

    public NewsItem(String newsText, String link, String sourceUrl) {
        this.newsText = newsText;
        this.link = link;
        this.sourceUrl = sourceUrl;
    }

    public void setNewsText(String newsText) {
        this.newsText = newsText;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getNewsText() {
        return newsText;
    }

    public String getLink() {
        return link;
    }
}