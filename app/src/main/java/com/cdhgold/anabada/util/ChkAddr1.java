package com.cdhgold.anabada.util;
/*
 * 주소 시 체크
 *  서울
 부산
 대구
 인천
 광주
 대전
 울산
 세종시
 경기도
 강원도
 충청북도
 충청남도
 전라북도
 전라남도
 경상북도
 경상남도
 제주

 */
public class ChkAddr1 implements CheckAdd {
     
	private String[] sido= {  "서울"
			,"부산"
			,"대구"
			,"인천"
			,"광주"
			,"대전"
			,"울산"
			,"세종시"
			,"경기도"
			,"강원도"
			,"충청북도"
			,"충청남도"
			,"전라북도"
			,"전라남도"
			,"경상북도"
			,"경상남도"
			,"제주"
	};
	@Override
	public int chk(String addr) {
		System.out.println("chk1 "+ addr);
		int ret = 0;
		int i = 0;
		for(String s : sido) {
			System.out.println("chk1 s "+ s);
			
			int ichk = addr.indexOf(s); 
			System.out.println("chk1 ichk "+ ichk);
			
			if(ichk != -1 ) {
				
				ret = i;
				break;
			}
			i++;
		}// end for 
		return ret;
	}
}