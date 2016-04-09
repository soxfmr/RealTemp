package com.soxfmr.realtemp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.soxfmr.realtemp.R;
import com.soxfmr.realtemp.model.ScheduleInfo;

import java.util.List;

/**
 * Created by Soxfmr@gmail.com on 2016/4/10.
 */
public class ScheduleAdapter extends BaseAdapter {

    private Context mContext;
    private List<ScheduleInfo> mScheduleInfoList;

    public ScheduleAdapter(Context context, List<ScheduleInfo> scheduleInfoList) {
        this.mContext = context;
        this.mScheduleInfoList = scheduleInfoList;
    }

    @Override
    public int getCount() {
        return mScheduleInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return mScheduleInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ScheduleInfoViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.schedule_info, null);

            viewHolder = new ScheduleInfoViewHolder();
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.schedule_info_title);
            viewHolder.tvTriggeredAt = (TextView) convertView.findViewById(R.id.schedule_info_time);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ScheduleInfoViewHolder) convertView.getTag();
        }

        ScheduleInfo scheduleInfo = mScheduleInfoList.get(position);
        viewHolder.tvTitle.setText(scheduleInfo.getTitle());
        viewHolder.tvTriggeredAt.setText(scheduleInfo.getTriggeredAt());

        return convertView;
    }

    private class ScheduleInfoViewHolder {
        public TextView tvTitle;
        public TextView tvTriggeredAt;
    }

}