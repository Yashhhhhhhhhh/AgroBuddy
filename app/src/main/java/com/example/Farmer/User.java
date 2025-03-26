package com.example.Farmer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class User {

    private String username;
    private String mobileNo;
    private String email;
    private String userType;
    private String location;
    private String photoUrl;

    @ServerTimestamp
    private Date timestamp;
    private List<String> postsDetails;

    // Default constructor for Firestore (optional, depending on Firebase version)
    public User() {
    }

    public User(String email, String location, String mobileNo, String userType, String username, String photoUrl, List<String> postsDetails) {
        this.email = email;
        this.location = location;
        this.mobileNo = mobileNo;
        this.userType = userType;
        this.username = username;
        this.photoUrl = photoUrl;
        this.postsDetails = postsDetails;
    }

    public User(String username, String mobileNo, String email, String userType, String location, String photoUrl) {
        this.username = username;
        this.mobileNo = mobileNo;
        this.email = email;
        this.userType = userType;
        this.location = location;
        this.photoUrl = photoUrl;
    }

    public <T> User(T requireNonNull) {
    }

    // Getters and setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return timestamp;
    }

    public List<String> getPostsDetails() {
        return postsDetails;
    }

    public void setPostsDetails(List<String> postsDetails) {
        this.postsDetails = postsDetails;
    }

    // Get the FirebaseUser object
    public FirebaseUser getFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }
}