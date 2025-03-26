package com.example.Farmer;

import java.util.Date;

public class Message {
    private String senderId;
    private String receiverId;
    private String text;
    private Date timestamp;

    public Message() {
        // Required default constructor for Firestore
    }

    public Message(String senderId, String receiverId,String text, Date timestamp) {
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
        this.receiverId = receiverId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
