package com.example.Farmer;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post {

    private String postId;
    private String content;
    private String imageUrl;

    private String timeStamp;
    private String username;

    private List<String> likes;

    private String profilePhotoUrl;
    private int noOfLikes;
    private int noOfComments;
    private int noOfShares;

    public Post() {
    }


    public Post(String postId, String content, Date timeStamp, String username, String imageUrl, int noOfLikes, int noOfComments, int noOfShares, List<String> likes) {
        this.postId = postId;
        this.content = content;
        Date date = new Date(); // Milliseconds since epoch


        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String fullTimestamp = date.toString();
        this.timeStamp =  dateFormat.format(timeStamp).toString();
      //  this.timeStamp = timeStamp.toDate().toLocaleString();

        this.imageUrl = imageUrl;
        this.username = username;
        this.noOfLikes = noOfLikes;
        this.noOfComments = noOfComments;
        this.noOfShares = noOfShares;
        this.likes = likes;

    }
    public List<String> getLikes() {
        return likes;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
    }
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getNoOfLikes() {
        return noOfLikes;
    }

    public void setNoOfLikes(int noOfLikes) {
        this.noOfLikes = noOfLikes;
    }

    public int getNoOfComments() {
        return noOfComments;
    }

    public void setNoOfComments(int noOfComments) {
        this.noOfComments = noOfComments;
    }

    public int getNoOfShares() {
        return noOfShares;
    }

    public void setNoOfShares(int noOfShares) {
        this.noOfShares = noOfShares;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public void addLike(String userId) {
        if (likes == null) {
            likes = new ArrayList<>();
        }
        likes.add(userId);
    }

    // Method to remove a like
    public void removeLike(String userId) {
        if (likes != null) {
            likes.remove(userId);
        }
    }
}
