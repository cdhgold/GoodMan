package com.cdhgold.reserve.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cdhgold.reserve.R;
import com.cdhgold.reserve.vo.ReserveVo;
import com.cdhgold.reserve.vo.SanghoVo;

import java.text.SimpleDateFormat;
import java.util.Locale;
/*
미용실 예약 adapter
 */
public class ReserveAdapter extends ArrayAdapter<ReserveVo> {
    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("a h:mm", Locale.getDefault());

    public ReserveAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.reserve, null);

            viewHolder = new ViewHolder();
            viewHolder.userNm = (TextView) convertView.findViewById(R.id.userNm);
            viewHolder.userTel = (TextView) convertView.findViewById(R.id.userTel);
            viewHolder.bigo = (TextView) convertView.findViewById(R.id.bigo);
            viewHolder.txttime = (TextView) convertView.findViewById(R.id.txt_time);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ReserveVo vo = getItem(position);
        viewHolder.userNm.setText(vo.userNm);
        viewHolder.userTel.setText(vo.userTel);
        viewHolder.bigo.setText(vo.bigo);
        viewHolder.txttime.setText(vo.Today);

        return convertView;
    }

    private class ViewHolder {
        private TextView userNm;
        private TextView userTel;
        private TextView bigo;
        private TextView txttime;
    }
}
