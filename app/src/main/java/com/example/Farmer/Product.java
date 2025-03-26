package com.example.Farmer;

public class Product {
    private String orderId;
    private String productId;
    private String status;
    private String userId;
    private String imageUrl;
    private String price;
    private String productName;
    private String productType;
    private String quantity;

    public Product() {
    }

    public Product(String orderId, String productId, String status, String userId, String imageUrl, String price, String productName, String productType, String quantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.status = status;
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.price = price;
        this.productName = productName;
        this.productType = productType;
        this.quantity = quantity;
    }

    public Product(String orderId, String userId, String productName, String productType, String quantity, String status, String imageUrl, String price){
        this.orderId = orderId;
        this.userId = userId;
        this.productName = productName;
        this.productType = productType;
        this.quantity = quantity;
        this.status = status;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
