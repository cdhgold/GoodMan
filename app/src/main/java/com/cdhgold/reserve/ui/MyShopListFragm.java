package com.cdhgold.reserve.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cdhgold.reserve.R;
import com.cdhgold.reserve.vo.SanghoVo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

/*
내 미용실 등록하기 화면
 */
public class MyShopListFragm extends Fragment implements View.OnClickListener {
    private View view;

    private TextView tjuso;
    private TextView tsangho;
    private TextView ttime;

    private FirebaseDatabase mFirebaseDatabase;

    private SanghoAdapter mAdapter;
    private ListView mListView;
    private String sfilter = "";
    private String fkey = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.myshop_list, container, false);
        view.findViewById(R.id.btn_send).setOnClickListener(this);// 버튼클릭 내미용실선택
        view.findViewById(R.id.btn_release).setOnClickListener(this);// 버튼클릭 해제

        return view;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle("춘천미용실-내미용실선택");

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
    private void initFirebaseDatabase() {
        //
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference( ) ;
 Log.d("cdhgold","Database ref obtained."+ref);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        // path 목록을 가져온다
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()){
                    String skey = data.getKey();
                    SanghoVo vo = data.getValue(SanghoVo.class);
                    vo.fkey = skey ;
                    mAdapter.add(vo);
                    //mAdapter.notifyDataSetChanged();
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
            fkey = (String)map.get("fkey");// 미용실 path key
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
    내 미용실 선택 / 해제
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_send: //

                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("fkey", fkey);
                editor.commit();
                break;

            case R.id.btn_release: //
                SanghoVo svo1 = new SanghoVo();
                SharedPreferences pref1 = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor1 = pref1.edit();
                editor1.putString("fkey", "");
                editor1.commit();

                break;

        }

    }

}