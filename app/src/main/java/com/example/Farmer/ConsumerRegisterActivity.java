package com.example.Farmer;

public class ConsumerRegisterActivity extends RegisterActivityBase {

    @Override
    protected int getContentView() {
        return R.layout.activity_consumer_register;
    }

    @Override
    protected String getUserType() {
        return "Consumer";
    }
}
