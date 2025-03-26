package com.example.Farmer;

public class FarmerRegisterActivity extends RegisterActivityBase {

    @Override
    protected int getContentView() {
        return R.layout.activity_farmer_register;
    }

    @Override
    protected String getUserType() {
        return "Farmer";
    }
}
