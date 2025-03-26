package com.example.Farmer;

public class FarmerSeedsData
{
    private String requestId;
    private String userId;
    private String productName;
    private String productType;
    private String quantity;
    private String status;
    private String productId;


    public FarmerSeedsData(){

    }

    public FarmerSeedsData(String productId, String requestId, String userId, String productName, String productType, String quantity, String status) {
        this.productId = productId;
        this.requestId = requestId;
        this.userId = userId;
        this.productName = productName;
        this.productType = productType;
        this.quantity = quantity;
        this.status = status;
    }

    public FarmerSeedsData(String requestId, String userId, String productName, String productType, String quantity, String status) {
        this.requestId = requestId;
        this.userId = userId;
        this.productName = productName;
        this.productType = productType;
        this.quantity = quantity;
        this.status = status;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
