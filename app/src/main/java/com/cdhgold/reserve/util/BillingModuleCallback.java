package com.cdhgold.reserve.util;

public interface BillingModuleCallback {
    //store 연결여부
    public void onStoreConnection(BillingModule billingModule ,int result);
    final static int  CONNECTION_OK = 200;
    final static int  CONNECTION_FAIL = 0;
    final static int  CONNECTION_CANCEL = -100;

}
