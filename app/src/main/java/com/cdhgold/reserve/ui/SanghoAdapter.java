package com.cdhgold.reserve.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cdhgold.reserve.R;
import com.cdhgold.reserve.vo.SanghoVo;

import java.text.SimpleDateFormat;
import java.util.Locale;
/*
미용실 예약 adapter
 */
public class SanghoAdapter extends ArrayAdapter<SanghoVo> {
    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("a h:mm", Locale.getDefault());

    public SanghoAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.sangho_list, null);

            viewHolder = new ViewHolder();
            viewHolder.txtjuso = (TextView) convertView.findViewById(R.id.txt_juso);
            viewHolder.txtsangho = (TextView) convertView.findViewById(R.id.txt_sangho);
            viewHolder.txttime = (TextView) convertView.findViewById(R.id.txt_time);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        SanghoVo sangho = getItem(position);
        viewHolder.txtsangho.setText(sangho.sangho);
        viewHolder.txtjuso.setText(sangho.juso);
        viewHolder.txttime.setText(sangho.Today);

        return convertView;
    }

    private class ViewHolder {
        private TextView txtsangho;
        private TextView txtjuso;
        private TextView txttime;
    }
}
