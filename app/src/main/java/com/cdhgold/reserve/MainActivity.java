package com.cdhgold.reserve;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.cdhgold.reserve.ui.HomeFragm;
import com.cdhgold.reserve.ui.MyShopListFragm;
import com.cdhgold.reserve.ui.PaymentFragm;
import com.cdhgold.reserve.ui.SanghoListFragm;
import com.cdhgold.reserve.util.BillingModule;
import com.cdhgold.reserve.util.BillingModuleCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

/*
춘천시 미용예약
신북읍	동면	동산면	신동면	남면	서면	사북면	북산면	동내면	남산면	교동	조운동	약사명동 근화동	소양동	후평1동 후평2동
후평3동 효자1동 효자2동 효자3동 석사동 퇴계동	강남동	 신사우동
등록된 미용실목록이 나온다.
결제는 6개월 5천원 , 결제를 하려면 회원등록, 결제여부확인 처리

 */
public class MainActivity extends AppCompatActivity {
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private SanghoListFragm slist = new SanghoListFragm();
    private BillingModuleCallback billingModuleCallback;
    private FragmentTransaction transaction ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.bottom_navigation_view);
        navView.setOnNavigationItemSelectedListener(new ItemSelectListener());
        transaction =  getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.infoFrameLayout, slist).commitAllowingStateLoss();

    }

    /*
    하단메뉴 home하단메뉴 home하단메뉴 home하단메뉴 home하단메뉴 home
     */
    private class ItemSelectListener implements BottomNavigationView.OnNavigationItemSelectedListener{

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            TransThrd tt = null;
            switch (menuItem.getItemId()) {
                case R.id.nav_home: // 첫화면 (메인 - 상품list  )
                    HomeFragm home = new HomeFragm();
                    tt = new TransThrd(home); // too much work으로 thread로 처리해야함.
                    tt.start();
                    break;

                case R.id.sangho: //선택상품 chat화면으로 이동 firebase
                    tt = new TransThrd(slist); // too much work으로 thread로 처리해야함.
                    tt.start();
                    break;

                case R.id.myshop: //내미용실 선택 ( 내 미용실만 나오게 )
                    MyShopListFragm my = new MyShopListFragm();
                    tt = new TransThrd(my); // too much work으로 thread로 처리해야함.
                    tt.start();
                    break;
                case R.id.payment: // 결제 ( 6개월 5천원 ) 화면으로 이동
                    PaymentFragm pay = new PaymentFragm();
                    tt = new TransThrd(pay); // too much work으로 thread로 처리해야함.
                    tt.start();
                    break;


            }
            return true;
        }
    }
    //메뉴 thread
    class TransThrd  extends Thread {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        private Fragment frg;
        public TransThrd(Fragment fragment ){
            this.frg = fragment;
        }
        public void run() {
            transaction.replace(R.id.infoFrameLayout, this.frg).commitAllowingStateLoss();
        }
    }
    //fragment 전환
    public void replaceFragment(Fragment fragment ) {
        TransThrd rt = new TransThrd(fragment);
        rt.start();
    }
    //in app결제
    public void billing( String mykey ) {
        List<String> sku_contents_list = new ArrayList<>();
        sku_contents_list.add("p_member");
        BillingModule billingModule = BillingModule.getInstance()
                .whereToUse(this)
                .setContentsList(sku_contents_list)
                .setBillingModuleCallback(billingModuleCallback);
        //start
        billingModule.start();
    }
}