package com.cdhgold.reserve.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

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
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.OK;
import static com.android.billingclient.api.BillingClient.SkuType.INAPP;


public  class BillingManager implements PurchasesUpdatedListener, ConsumeResponseListener {
    // 결제를 위해 가지온 상품 정보를 저장한 변수 입니다.
    public List<SkuDetails> mSkuDetailsList;
    private BillingClient billingClient;
    private Context ctx;
    private String skuId;
    private Activity mActivity;
    private List<SkuDetails> misProductos;
    String p_member = "p_member" ;  //제품 ID
    SkuDetails memberSku;
    FirebaseDatabase mFirebaseDatabase;
    String fkey = "";
    // Constructor de la clase Pagos
    public BillingManager(Activity _activity, FirebaseDatabase _mFirebaseDatabase,String _fkey) {
        mActivity = _activity;
        mFirebaseDatabase = _mFirebaseDatabase;
        fkey = _fkey;
        connectBillingClient();
    }


    // activity에서 호출
    public void setProd(String skuId) {

        this.skuId = skuId;
        if("p_member".equals(skuId) ) {
            doBillingFlow(memberSku);
        }

    }
    //구매
    private void doBillingFlow(SkuDetails skuDetails) {

        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
        billingClient.launchBillingFlow(mActivity, flowParams);
        /*if(responseCode == BillingClient.BillingResponse.ITEM_ALREADY_OWNED) {
            Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
            onPurchasesUpdated(BillingClient.BillingResponse.OK, purchasesResult.getPurchasesList());
        }*/
    }

    // Configura el Billing Client para iniciar la conexión con Google Play Console
    private void connectBillingClient() {

        //  1. Configura el Billing Client
        billingClient = BillingClient.newBuilder(mActivity)
                .enablePendingPurchases()
                .setListener(this)
                .build();
        // 구글플레이 접속
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult   ) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK ) {
                    // The billing client is ready. You can query purchases here.
                    List<String> skuList = new ArrayList<> ();
                    skuList.add(skuId); // 제품id

                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                    billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
                        @Override
                        public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> list) {
                            // Process the result.
                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                                for (SkuDetails skuDetails : list) {
                                    String sku = skuDetails.getSku();
                                    String price = skuDetails.getPrice();

                                    if (skuId.equals(sku)) {
                                        memberSku = skuDetails; // 상품을 담는다

                                    }
                                }
                            }
                        }
                    });

                }
            }


            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                //billingClient.startConnection();
            }
        });
        get_Sku_Detail_List();
    }



    // Obtiene el Producto que se comprará según el Sku ingresado mediante comprar(sku);
    private SkuDetails getSkuIdDetails() {

        if (misProductos == null) return null;
        for (SkuDetails skuProducto : misProductos) {
            if (skuId.equals(skuProducto.getSku())) return skuProducto;
        }
        return null;

    }

    // 결제 처리후 callback
    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {

        int responseCode = billingResult.getResponseCode();
        // 결제에 성공한 경우.
        if( responseCode == BillingClient.BillingResponseCode.OK && purchases != null )
        {

            Log.d( TAG, "결제에 성공했으며, 아래에 구매한 상품들이 나열될 것 입니다." );

            for( Purchase _pur : purchases )
            {
                Log.d( TAG, "결제에 대해 응답 받은 데이터 :"+ _pur.getOriginalJson() );
                ConsumeParams params = ConsumeParams.newBuilder()
                        .setPurchaseToken(_pur.getPurchaseToken())
                        .build();
                //결제성공후 소비를 해야, 재 구매가 가능하다
                billingClient.consumeAsync( params, this );
            }
        }

        // 사용자가 결제를 취소한 경우.
        else if( responseCode == BillingClient.BillingResponseCode.USER_CANCELED )
        {
            Log.d( TAG, "사용자에 의해 결제가 취소 되었습니다." );
        }

        // 그 외에 다른 결제 실패 이유.
        else
        {
            Log.d( TAG, "결제가 취소 되었습니다. 종료코드 : " + responseCode );
        }
    }

    @Override
    // consumeAsync callback, 결제성공후 소비 callback
    public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
        if (billingResult.getResponseCode() == OK) {
            Util.writeNewPayment(mFirebaseDatabase, fkey);
            Log.i("Pagos", "Token de Compra: " + purchaseToken + " consumida");
        } else {
            Log.i("Pagos", "Error al consumir compra, responseCode: " + billingResult.getResponseCode());
        }
    }

    // Valida la compra y Devuelve True si encuentra la compra del usuario en el Servidor de Google
    private boolean validaCompra() {

        List<Purchase> purchasesList = billingClient.queryPurchases(INAPP).getPurchasesList();
        if (purchasesList != null && !purchasesList.isEmpty()) {
            for (Purchase purchase : purchasesList) {
                if (purchase.getSku().equals(skuId)) {
                    return true;
                }
            }
        }
        return false;

    }


    // 상품정보를 가져온다
    private void consumeCompras() {

        Purchase.PurchasesResult queryPurchases = billingClient.queryPurchases(INAPP);
        if (queryPurchases.getResponseCode() == OK) {
            List<Purchase> purchasesList = queryPurchases.getPurchasesList();
            if (purchasesList != null && !purchasesList.isEmpty()) {
                for (Purchase purchase : purchasesList) {
                    ConsumeParams params = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();
                    billingClient.consumeAsync(params, this);
                }
            }
        }

    }



    // 구입 가능한 상품의 리스트를 받아 오는 메소드 입니다.
    public void get_Sku_Detail_List()
    {
        // 구글의 상품 정보들의 ID를 만들어 줍니다.
        List<String> Sku_ID_List = new ArrayList<>();
        Sku_ID_List.add( "p_member" );
        // SkuDetailsParam 객체를 만들어 줍니다. (1회성 소모품에 대한)
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList( Sku_ID_List ).setType(BillingClient.SkuType.INAPP);
        // 비동기 상태로 앱의, 정보를 가지고 옵니다.
        billingClient.querySkuDetailsAsync(params.build() ,  new SkuDetailsResponseListener()
                {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                        // 상품 정보를 가지고 오지 못한 경우, 오류를 반환하고 종료합니다.
                        if( billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK)
                        {

                            Log.d(TAG, "상품 정보를 가지고 오던 중 오류를 만났습니다. 오류코드 : " + billingResult.getResponseCode());
                            return;
                        }
                        // 하나의 상품 정보도 가지고 오지 못했습니다.
                        if( skuDetailsList == null )
                        {

                            Log.d(TAG, "상품 정보가 존재하지 않습니다.");
                            return;
                        }
                        // 응답 받은 데이터들의 숫자를 출력 합니다.
                        Log.d(TAG, "응답 받은 데이터 숫자:" + skuDetailsList.size());

                        // 받아 온 상품 정보를 차례로 호출 합니다.
                        for( int _sku_index = 0;_sku_index < skuDetailsList.size(); _sku_index++)
                        {
                            // 해당 인덱스의 객체를 가지고 옵니다.
                            SkuDetails _skuDetail = skuDetailsList.get( _sku_index );
                            String pid = _skuDetail.getSku();
                            if(pid.equals(p_member)){ // 회원가입 sku
                                memberSku = _skuDetail;
                            }

                            // 해당 인덱스의 상품 정보를 출력하도록 합니다.
                            Log.d( TAG, _skuDetail.getSku() + ": " + _skuDetail.getTitle() + ", " + _skuDetail.getPrice() + ", " + _skuDetail.getDescription() );
                            Log.d( TAG, _skuDetail.getOriginalJson() );
                        }
                        // 받은 값을 멤버 변수로 저장합니다.
                        mSkuDetailsList = skuDetailsList;
                    }
                }
        );
    }




}