package com.cdhgold.reserve.ui;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cdhgold.reserve.MainActivity;
import com.cdhgold.reserve.R;
import com.cdhgold.reserve.util.BillingManager;
import com.cdhgold.reserve.util.GetMember;
import com.cdhgold.reserve.util.SetMember;
import com.cdhgold.reserve.util.Util;
import com.cdhgold.reserve.vo.SanghoVo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/*
결제화면 : 6개월 5천원
 */
public class PaymentFragm extends Fragment implements View.OnClickListener {
    private View view;
    private EditText mEdtSangho;
    private TextView tjuso;
    private TextView tsangho;
    private TextView ttime;

    private FirebaseDatabase mFirebaseDatabase;
    private Spinner mSpinner;
    private SanghoAdapter mAdapter;
    private ListView mListView;
    private String sfilter = "";
    private String mykey = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.payment_list, container, false);
        view.findViewById(R.id.btn_send).setOnClickListener(this);// 버튼클릭 : 결제
        return view;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle("춘천미용실-결제(6개월5천원)");
        setHasOptionsMenu(true);

        initViews();
        initFirebaseDatabase();
    }
    private void initViews() {
        mListView = (ListView) view.findViewById(R.id.list_message);
        mAdapter = new SanghoAdapter(getContext(), R.layout.sangho_list);

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(listClicklistener);

        tjuso = (TextView) view.findViewById(R.id.txt_juso);
        tsangho = (TextView) view.findViewById(R.id.txt_sangho);
        ttime = (TextView) view.findViewById(R.id.txt_time);

        view.findViewById(R.id.btn_send).setOnClickListener(this);
    }
    /*
     중요: fragment에선 resume에서 사용해야함.
     */
    public void onResume() {
        super.onResume();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        mykey =  pref.getString("fkey" , "" );

    }
    private void initFirebaseDatabase() {
        //

          DatabaseReference ref = FirebaseDatabase.getInstance().getReference( ) ;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        // path 목록을 가져온다
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()){
                    String skey = data.getKey();
                    SanghoVo vo = data.getValue(SanghoVo.class);
                    vo.fkey = skey ;
                    if(!"".equals(mykey) && skey.equals(mykey)) {
                        mAdapter.clear();
                        mAdapter.notifyDataSetChanged();
                        mAdapter.add(vo);
                        break;
                    }else{
                        mAdapter.add(vo);
                    }
                    //mAdapter.notifyDataSetChanged();
                    sfilter = skey;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //ref.child(sfilter).equalTo(sfilter); // filter
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mAdapter.notifyDataSetChanged();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mAdapter.notifyDataSetChanged();
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
    /*
    선택시 해당 미용실로 이동( 예약화면 )
     */
    AdapterView.OnItemClickListener listClicklistener= new AdapterView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SanghoVo vo = mAdapter.getItem(position);
            Map map = vo.toMap();
            mykey = (String)map.get("fkey");// 미용실 명
            for (int i = 0; i < mListView.getChildCount(); i++) {
                if(position == i ){
                    mListView.getChildAt(i).setBackgroundColor(Color.YELLOW);
                }else{
                    mListView.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }

    };


/*
결제창을 띄우고, 결과를 등록
Util.writeNewReserve(mFirebaseDatabase,nm,bigo,fkey);
 */
    @Override
    public void onClick(View v) {
        //Util.showAlim("결제",getContext());
        //결제여부 확인하고 , 결제창을 띄운다 , 결제는 6월, 12월 체크한다.
        /*
        1-6 : 6월체크, 7-12: 12월체크
         */
Log.d("pay",mykey);
        if("".equals(mykey)){
            return;
        }
        String chkday = Util.chkDay(); // yyyyMMdd
        // 결제여부확인 chkday
        GetMember getMem = new GetMember(mykey, chkday);
        FutureTask futureTask = new FutureTask(getMem);
        Thread thread = new Thread(futureTask);
        thread.start();
        try {
            String ret = (String)futureTask.get(); // 결과
//Log.d("getPayChk ","ret");
            if("0".equals(ret)) {// 결제등록이 없으면 , 결제실행
                BillingManager bill = new BillingManager(getActivity(),mFirebaseDatabase,mykey);
                bill.setProd("p_member");
            }else{
                Util.showAlim("결제됐습니다.",getContext());
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}