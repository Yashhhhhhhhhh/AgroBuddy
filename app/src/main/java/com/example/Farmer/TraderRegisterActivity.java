package com.example.Farmer;

public class TraderRegisterActivity extends RegisterActivityBase {

    @Override
    protected int getContentView() {
        return R.layout.activity_trader_register;
    }

    @Override
    protected String getUserType() {
        return "Trader";
    }
}
