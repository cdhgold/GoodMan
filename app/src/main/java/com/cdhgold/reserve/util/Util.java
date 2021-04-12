package com.cdhgold.reserve.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cdhgold.reserve.MainActivity;
import com.cdhgold.reserve.ui.SanghoListFragm;
import com.cdhgold.reserve.vo.ReserveVo;
import com.cdhgold.reserve.vo.SanghoVo;
import com.google.firebase.database.FirebaseDatabase;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static java.security.AccessController.getContext;


public class Util {
    // 암호화
    public static String md5(String str){
        String MD5 = "";
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes("UTF-8"));
            byte byteData[] = md.digest();
            StringBuffer sb = new StringBuffer();
            for(int i = 0 ; i < byteData.length ; i++) sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            MD5 = sb.toString();
        }
        catch(NoSuchAlgorithmException e) { e.printStackTrace(); MD5 = null; }
        catch (UnsupportedEncodingException e) { e.printStackTrace(); MD5 = null; }
        return MD5;
    }
    public static void showAlim(String nm, Context ctx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Anabada!");
        builder.setMessage(nm );
        AlertDialog ad = builder.create();
        ad.show();
        //ad.dismiss();
        //builder.show();
    }
    public static String getComma(String tmp){
        String ret = "";
        DecimalFormat formatter = new DecimalFormat("###,###");
        ret = "$"+formatter.format(Double.parseDouble(tmp));

        return ret;
    }
    /*
    미용실 등록
     */
    public static void writeNewPost(FirebaseDatabase mDatabase, String juso,  String sangho ) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.getReference("shop").push().getKey();
        SanghoVo vo = new SanghoVo();
        vo.juso = juso;
        vo.sangho = sangho;
        Map<String, Object> postValues = vo.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("shop" + key, postValues);
        mDatabase.getReference().updateChildren(childUpdates);
        //db에 저장 - key ( cc_beauty : fkey, payment )
        SetMember setMem = new SetMember("shop" + key);
        FutureTask futureTask = new FutureTask(setMem);
        Thread thread = new Thread(futureTask);
        thread.start();
        try {
            String ret = (String)futureTask.get(); // 결과
            if("OK".equals(ret)) {// 가입후 화면이동
//Log.d("등록","등록");
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
    예약 등록
     */
    public static void writeNewReserve(FirebaseDatabase mDatabase, String nm,  String bigo,String fkey,String sortkey ) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously

        String key = mDatabase.getReference(fkey ).child("reserve").push().getKey();
        ReserveVo vo = new ReserveVo();
        vo.userNm = nm;
        vo.bigo = bigo;
        vo.sortkey = sortkey+fkey;
        Map<String, Object> postValues = vo.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put(fkey + key, postValues);
        childUpdates.put(key, postValues);

        mDatabase.getReference(fkey).child("reserve").updateChildren(childUpdates);
        // 삭제 mDatabase.getReference(fkey).removeValue();
    }
    /*
    결제 등록 (6개월 5천원)
     */
    public static void writeNewPayment(FirebaseDatabase mDatabase,  String fkey ) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.getReference(fkey ).child("payment").push().getKey();

        Map<String, Object> postValues = new HashMap<String, Object>();
        postValues.put("payment","Y");
        String payday = Util.chkDay();
        postValues.put("payment_day",payday); // yyyy0601 , yyyy1201
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(fkey + key, postValues);
        mDatabase.getReference(fkey).child("payment").updateChildren(childUpdates);
        // 결제 등록한다.
        SetPay setMem = new SetPay("shop" + key, payday);
        FutureTask futureTask = new FutureTask(setMem);
        Thread thread = new Thread(futureTask);
        thread.start();
        try {
            String ret = (String)futureTask.get(); // 결과
            if("OK".equals(ret)) {// 가입후 화면이동
//Log.d("등록","등록");
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //2020101617220302147483647GMT+09:00_0.49708799179938523   ,  yyyyMMddHHmmssSSSSSSSSSSSzzz
    public static  String getUid() {
        Date currentDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSSSSSSSSSS");
        //java.security.SecureRandom random = new java.security.SecureRandom();
        //Double randomDouble = new Double(random.nextDouble());
        return simpleDateFormat.format(currentDate); //+"_"+randomDouble.toString()
    }
/*
오늘날짜
 */
    public static String toDay(){
        SimpleDateFormat format1 = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");

        Date time = new Date();

        String today = format1.format(time);
        return today;

    }
    public static String chkDay(){
        SimpleDateFormat format1 = new SimpleDateFormat( "yyyyMMdd");
        String payday = "";
        Date time = new Date();

        String chkday = format1.format(time);
        String mm = chkday.substring(4,6);
        int im = Integer.parseInt(mm);
        Log.d("mm",mm);
        if( im >= 1 && im <=6 ){
            payday = chkday.substring(0,4)+"0601";
        }else{
            payday = chkday.substring(0,4)+"1201";
        }

        return payday;

    }
}
