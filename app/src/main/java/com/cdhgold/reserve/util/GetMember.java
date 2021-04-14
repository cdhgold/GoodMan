package com.cdhgold.reserve.util;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Callable;

/*
회원 결제여부확인
 */
public class GetMember implements Callable<String> {
    URL url;
    Document doc = null;
    HttpURLConnection conn;
    InputStreamReader isr;
    String fkey = "";             // firebase shop key
    String payday = "";             // 결제일

    private Context context;
    public GetMember(String fkey, String payday ){
        this.fkey = fkey ;
        this.payday = payday ;

    }
    @Override
    public String call() throws Exception {
        String result = ""  ;

        try {
            url = new URL("http://49.50.167.90/topbd/beauty/getPayChk");

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept-Charset","UTF-8");
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setDoOutput(true);
            conn.setDoInput(true);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(),"UTF8");
            HashMap<String, String> map = new HashMap<>();
            map.put("fkey", fkey);
            map.put("payday", payday);

            StringBuffer sbParams = new StringBuffer();
            boolean isAnd = false;

            for(String key: map.keySet()){
                sbParams.append(key).append("=").append(map.get(key));
                sbParams.append("&");
            }

            wr.write(sbParams.substring(0,sbParams.length()-1));
            wr.flush();
            wr.close();

            StringBuffer json = new StringBuffer();
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine = "";
                while ((inputLine = in.readLine()) != null) {
                    json.append(inputLine);
                }
                in.close();
            }
            String ret = "";
            Log.i("thread","json==========="+json.toString());
            if(!"".equals(json.toString()) && !"null".equals(json.toString())) {
                JSONObject jsonObject = new JSONObject(json.toString());
                ret = jsonObject.getString("ret");
            }

            result = ret;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if(conn != null) {
                conn.disconnect();
            }
        }
        return result;
    }

}
