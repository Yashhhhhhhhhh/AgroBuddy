package com.example.Farmer;

public class BlogData {

    String title,description,ImageURL, username, profImage;

    public BlogData(){}
    public BlogData(String username, String profImage, String title, String description, String ImageURL) {
        this.username = username;
        this.profImage = profImage;
        this.title = title;
        this.description = description;
        this.ImageURL = ImageURL;
    }

    public String getUsername() {

        return username;
        //  return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfImage() {
        return profImage;
        //return profImage;
    }

    public void setProfImage(String profImage) {
        this.profImage = profImage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }

    private double similarityScore;

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }
}
