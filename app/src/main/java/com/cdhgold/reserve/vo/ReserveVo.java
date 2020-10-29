package com.cdhgold.reserve.vo;

import com.cdhgold.reserve.util.Util;

import java.util.HashMap;
import java.util.Map;

/*
미용실 정보 : 주소, 상호명 ,uid
 */
public class ReserveVo {
    public String uid;      // Firebase Realtime Database 에 등록된 Key 값
    public String userNm;   //
    public String userTel;  //
    public String bigo;     //
    public String Today;    // 시간
    public String fkey;    //
    public String sortkey;    // 999,998 ...

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userNm",userNm);
        map.put("userTel",userTel);
        map.put("bigo",bigo);
        map.put("fkey",fkey);
        map.put("sortkey",sortkey);
        map.put("UID", Util.getUid() );
        map.put("Today", Util.toDay() );
        return map;
    }
}
