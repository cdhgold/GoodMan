package com.cdhgold.anabada.util;

import android.app.AlertDialog;
import android.content.Context;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;

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
        builder.setTitle("GoodMan!");
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
    P01 신발 ,P02 시계  ,P03 반지  ,P04 목걸이 ,P05 자동차 ,P06 책 ,P07 봉사활동  ,P08 채식 ,P09 대중교통이용
    Shoes.
    Wristwatch
    Jewelry
    Necklace
    Car
    Book
    Volunteer activity
    Vegetable
    using public transportation
     */
    public static String getItemNm(String tmp){
        String ret = "";
        if("P01".equals(tmp)){
            ret = "Shoes";
        }
        else if("P02".equals(tmp)){
            ret = "Wristwatch";
        }
        else if("P03".equals(tmp)){
            ret = "Jewelry";
        }
        else if("P04".equals(tmp)){
            ret = "Necklace";
        }
        else if("P05".equals(tmp)){
            ret = "Car";
        }
        else if("P06".equals(tmp)){
            ret = "Book";
        }
        else if("P07".equals(tmp)){
            ret = "Volunteer";
        }
        else if("P08".equals(tmp)){
            ret = "Vegetable";
        }
        else if("P09".equals(tmp)){
            ret = "using public transportation";
        }

        return ret;
    }
}
