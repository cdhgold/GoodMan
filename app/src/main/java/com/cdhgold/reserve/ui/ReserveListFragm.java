package com.cdhgold.reserve.ui;

import android.os.Bundle;
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

import com.cdhgold.reserve.R;
import com.cdhgold.reserve.util.Util;
import com.cdhgold.reserve.vo.ReserveVo;
import com.cdhgold.reserve.vo.SanghoVo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

/*
미용실 예약 화면
 */
public class ReserveListFragm extends Fragment implements View.OnClickListener {
    private View view;
    private EditText mUserNm;
    private EditText mUserTel;
    private EditText mbigo;

    private TextView userNm;
    private TextView userTel;
    private TextView bigo;
    private TextView ttime;
    private TextView sanghoNm;
    private String  fkey = "", sangho = "";
    private FirebaseDatabase mFirebaseDatabase;
    private ReserveAdapter mAdapter;
    private ListView mListView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fkey = this.getArguments().getString("fkey"); // 호출 param
        sangho = this.getArguments().getString("sangho"); // 호출 param

        view = inflater.inflate(R.layout.reserve_activity, container, false);
        view.findViewById(R.id.btn_send).setOnClickListener(this);// 예약 버튼클릭
        return view;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUserNm = view.findViewById(R.id.userNm);
        mbigo = view.findViewById(R.id.bigo);
        sanghoNm = view.findViewById(R.id.sangho);
        sanghoNm.setText(sangho);
        getActivity().setTitle("춘천미용실-예약하기");
        initViews();
        initFirebaseDatabase();
    }
    private void initViews() {
        mListView = (ListView) view.findViewById(R.id.list_reserve);
        mAdapter = new ReserveAdapter(getContext(), R.layout.reserve);

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(listClicklistener);

        userNm = (TextView) view.findViewById(R.id.userNm);
        userTel = (TextView) view.findViewById(R.id.userTel);
        bigo = (TextView) view.findViewById(R.id.bigo);
        ttime = (TextView) view.findViewById(R.id.txt_time);

        view.findViewById(R.id.btn_send).setOnClickListener(this);
    }
    private void initFirebaseDatabase() {
        //DatabaseReference upvotesRef = ref.child("server/saving-data/fireblog/posts/-JRHTHaIs-jNPLXOQivY/upvotes");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(fkey  ).child("예약") ;
 Log.d("cdhgold","Database ref obtained."+ref);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        // path 목록을 가져온다
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()){
                    ReserveVo vo = data.getValue(ReserveVo.class);
                    mAdapter.add(vo);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
            ReserveVo vo = mAdapter.getItem(position);
 Log.d("list click ",vo.toMap().toString());
            Map map = vo.toMap();


        }

    };


/*
글쓰기: path (상품명 으로 채팅방을 만든다 )
 */
    @Override
    public void onClick(View v) {
        String nm = mUserNm.getText().toString();
        String bigo = mbigo.getText().toString();
        mUserNm.setText("");
        mbigo.setText("");

        if("".equals(nm) || "".equals(bigo)  ){
            Util.showAlim("예약자명, 남길말씀을 입력하세요!",getContext() );
        }else{
            Util.writeNewReserve(mFirebaseDatabase,nm,bigo,fkey);
        }
    }
}