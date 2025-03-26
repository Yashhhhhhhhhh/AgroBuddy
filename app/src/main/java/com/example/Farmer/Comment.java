package com.example.Farmer;

public class Comment {

    private String comment_username;
    private String profileImageUrl;
    private String comment_Content;

    public Comment(){

    }

    public Comment(String comment_username, String comment_Content) {
        this.comment_username = comment_username;
        this.comment_Content = comment_Content;
    }

    public String getComment_username() {
        return comment_username;
    }

    public void setComment_username(String username) {
        this.comment_username = username;
    }

    public String getComment_Content() {
        return comment_Content;
    }

    public void setComment_Content(String comment_Content) {
        this.comment_Content = comment_Content;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
