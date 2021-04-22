package com.cdhgold.reserve.util;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.List;

public class BillingModule {

    private String PRODUCT_TYPE = BillingClient.SkuType.INAPP;

    /**
     *
     */
    private Activity activity;
    private List<String> sku_contents_list;//product name in your store

    private BillingClient billingClient;
    private PurchasesUpdatedListener purchaseUpdateListener;
    private BillingModuleCallback billingModuleCallback;

    /**
     * late init
     */
    private BillingModule(){ }
    private static class BillingPlaceHolder{
        private static final  BillingModule INSTANCE = new BillingModule();
    }
    public static BillingModule getInstance(){
        return BillingPlaceHolder.INSTANCE;
    }

    /**
     * step1
     * create instance
     */
    public BillingModule whereToUse(@NonNull Activity activity){
        this.activity = activity;
        return this;
    }

    /**
     * step2
     * set product list
     */
    public BillingModule setContentsList(@NonNull List<String> sku_contents_list){
        this.sku_contents_list = sku_contents_list;
        return this;
    }
    public BillingModule setPurchaseType(@BillingClient.SkuType String purchaseType){
        this.PRODUCT_TYPE = purchaseType;
        return this;
    }

    /**
     * optional
     * add listener
     */
    public BillingModule setBillingModuleCallback(BillingModuleCallback billingModuleCallback){
        this.billingModuleCallback = billingModuleCallback;
        return this;
    }
//    public BillingModule setPurchaseUpdateListener(PurchasesUpdatedListener purchaseUpdateListener){
//        this.purchaseUpdateListener = purchaseUpdateListener;
//        return this;
//    }
//    public BillingModule setPurchaseUpdateListener(){
//        this.purchaseUpdateListener = purchaseUpdateListener2;
//        return this;
//    }


    /**
     * ### FLOW1: open connection with store ###
     * open
     */
    public BillingModule start(){

        //check validation
        if (activity == null) throw new IllegalArgumentException("activity must be 'Not Null'");
        if (sku_contents_list == null) throw new IllegalArgumentException("sku product list must be 'not Null'");
        if (sku_contents_list.size()==0) throw new IllegalArgumentException("sku product list size greater than '0'");

        //initialize billingClient
        if (billingClient == null){
            billingClient = BillingClient.newBuilder(activity)
                    .setListener(purchaseUpdateListener2)
                    .enablePendingPurchases()
                    .build();
        }

        //request to store connection open
        getStoreBillingConnection();
        return this;
    }
    //request to store connection open
    private void getStoreBillingConnection(){
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                //ready to connection
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    if (billingModuleCallback !=null){
                        billingModuleCallback.onStoreConnection(BillingModule.this,BillingModuleCallback.CONNECTION_OK);
                    }

                    getProduct();
                }
                // billing is not ready
                else {
                    if (billingModuleCallback !=null){
                        billingModuleCallback.onStoreConnection(BillingModule.this,BillingModuleCallback.CONNECTION_FAIL);
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                if (billingModuleCallback !=null) {
                   billingModuleCallback.onStoreConnection(BillingModule.this,BillingModuleCallback.CONNECTION_CANCEL);
                }
            }
        });
    }

    /**
     *  ### FLOW 2: Check if there is a product in store ###
     *
     *  BillingClient.SkuType.INAPP : 일회성 상품
     *  BillingClient.SkuType.SUBS : 구독상품
     *
     *  check product
     */
    private void getProduct(){
        if (sku_contents_list==null || sku_contents_list.size() == 0) return;

        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(sku_contents_list).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),getProductCallback);
    }
    //get product callback
    private SkuDetailsResponseListener getProductCallback = new SkuDetailsResponseListener() {
        @Override
        public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
            //연결성공
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                //상품 가저오기 실패
                if (list == null){
                    if (billingModuleCallback !=null) {
                        //billingModuleCallback.onCheckProduct(BillingModule.this,BillingModuleCallback.CheckProduct.PRODUCT_NULL);
                    }
                    return;
                }
                //can get
                if (list.size()==0){
                    if (billingModuleCallback !=null) {
                        //billingModuleCallback.onCheckProduct(BillingModule.this,BillingModuleCallback.CheckProduct.PRODUCT_EMPTY);
                    }
                    return;
                }

                //success
                if (billingModuleCallback !=null) {
                    //billingModuleCallback.onCheckProduct(BillingModule.this,BillingModuleCallback.CheckProduct.PRODUCT_GET);
                }
                for (SkuDetails details:list){
                    showBilling(details);
                }
            }else {
                //fail to connect
                if (billingModuleCallback !=null) {
                    //billingModuleCallback.onCheckProduct(BillingModule.this,BillingModuleCallback.CheckProduct.CONNECTION_FAIL);
                }
            }
        }
    };


    /**
     *  ### FLOW3: show Billing module and Check purchase your Product ###
     * show purchase
     */
    //결제화면 보여주기
    private void showBilling(SkuDetails skuDetails) {
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
        billingClient.launchBillingFlow(activity,flowParams);
        //go to PurchasesUpdatedListener
    }
    //결제 결과 처리
    private PurchasesUpdatedListener purchaseUpdateListener2 = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                //when you complete purchase , confirm product purchase
                for (Purchase purchase : list) {
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                // Handle any other error codes.
            }
        }
    };

    /**
     * ### FLOW4: Confirm purchased your product ###
     *
     *
     * check purchase type
     * confirm purchased
     */
    private void handlePurchase(Purchase purchase){
        switch (PRODUCT_TYPE){
            case BillingClient.SkuType.INAPP:
                handlePurchaseINAPP(purchase);
                break;
            case BillingClient.SkuType.SUBS:
                handlePurchaseSUBS(purchase);
                break;
            default:
                throw new IllegalArgumentException("Undefined purchase type!");
        }
    }
    /**
     * 소비성 결제 BillingClient.SkuType.INAPP
     */
    //소비성 결제 상품 결제확정
    private void handlePurchaseINAPP(Purchase purchase) {
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
        billingClient.consumeAsync(consumeParams, consumeResponseListener);
    }
    //소비성 결제 상품 결제확정 callback
    private ConsumeResponseListener consumeResponseListener = new ConsumeResponseListener() {
        @Override
        public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                // Handle the success of the consume operation.
            } else {
                //fail connection
            }
        }
    };

    /**
     * 인앱결제 BillingClient.SkuType.SUBS
     * - 결제 확정
     *         int UNSPECIFIED_STATE = 0;
     *         int PURCHASED = 1;
     *         int PENDING = 2;
     */
    private void handlePurchaseSUBS(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING){
            //거래 중지 등등 ... 결제관련 문제가 발생했을때
        } else {
            //그외 알 수 없는 에러들...
        }
    }
    //인앱결제 확정 callback
    private AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                //success
            } else {
                //fail
            }
        }
    };


    /**
     * etc tool
     */
    private void showToast(String msg){
        Toast.makeText(activity,msg,Toast.LENGTH_SHORT).show();
    }
}