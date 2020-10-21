package com.cdhgold.reserve.vo;

import com.cdhgold.reserve.util.Util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/*
미용실 정보 : 주소, 상호명 ,uid
 */
public class SanghoVo {
    public String uid; // Firebase Realtime Database 에 등록된 Key 값
    public String juso; // 사용자 이름
    public String sangho; // 내용
    public String Today; // 시간
    public String fkey; // path key


    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("sangho",sangho);
        map.put("juso",juso);
        map.put("fkey",fkey);
        map.put("UID", Util.getUid() );
        map.put("Today", Util.toDay() );
        return map;
    }
}
