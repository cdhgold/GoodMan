package com.cdhgold.reserve.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.cdhgold.reserve.util.Util;
import com.cdhgold.reserve.vo.SanghoVo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

/*
등록된 미용실목록이 나온다.
 */
public class SanghoListFragm extends Fragment implements View.OnClickListener {
    private View view;
    private EditText mEdtSangho;
    private TextView tjuso;
    private TextView tsangho;
    private TextView ttime;

    private FirebaseDatabase mFirebaseDatabase;
    private Spinner mSpinner;
    private SanghoAdapter mAdapter;
    private ListView mListView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.list_activity, container, false);
        view.findViewById(R.id.btn_send).setOnClickListener(this);// 버튼클릭
        return view;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle("춘천미용실-내미용실등록(상호/연락처)");
        setHasOptionsMenu(true);
        mEdtSangho = view.findViewById(R.id.sangho);

        mSpinner = view.findViewById(R.id.spinner);
        String[] models = getResources().getStringArray(R.array.sizes);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item, models);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
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
                    mAdapter.notifyDataSetChanged();

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
            SanghoVo vo = mAdapter.getItem(position);
 Log.d("list click ",vo.toMap().toString());
            Map map = vo.toMap();
            String uid = (String)map.get("UID");
            String sangho = (String)map.get("sangho");// 미용실 명
            String fkey = (String)map.get("fkey");// 미용실 명
            Bundle bundle = new Bundle();
            bundle.putString("fkey", fkey);
            bundle.putString("sangho", sangho);
            ReserveListFragm frg = new ReserveListFragm();
            frg.setArguments(bundle); // param pass
            ((MainActivity)getContext()).replaceFragment(frg);    // 새로 불러올 Fragment의 Instance를 Main으로 전달

        }

    };


/*
글쓰기: path (상품명 으로 채팅방을 만든다 )
 */
    @Override
    public void onClick(View v) {
        String sangho = mEdtSangho.getText().toString();
        String juso = mSpinner.getSelectedItem().toString();
        mEdtSangho.setText("");
        mSpinner.setSelected(false);
        if("".equals(sangho) || "".equals(juso)  ){
            Util.showAlim("주소, 미용실명(전호번호)을 입력하세요!",getContext() );
        }else{
            Util.writeNewPost(mFirebaseDatabase,juso,sangho);
            SanghoListFragm frg = new SanghoListFragm();
            ((MainActivity)getContext()).replaceFragment(frg);
        }
    }
}