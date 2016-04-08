package com.soxfmr.realtemp.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.soxfmr.realtemp.R;
import com.soxfmr.realtemp.utils.GattHelper;

import java.util.List;

/**
 * Created by Soxfmr@gmail.com on 2016/4/9.
 */
public class BluetoothDeviceAdapter extends BaseAdapter {

    private Context mContext;
    private List<BluetoothDevice> mBluetoothDeviceList;

    public BluetoothDeviceAdapter(Context context, List<BluetoothDevice> bluetoothDeviceList) {
        this.mContext = context;
        this.mBluetoothDeviceList = bluetoothDeviceList;
    }

    @Override
    public int getCount() {
        if (mBluetoothDeviceList == null)
            return 0;

        return mBluetoothDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        if (mBluetoothDeviceList == null)
            return null;

        return mBluetoothDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.device_info, null);

            viewHolder = new ViewHolder();
            viewHolder.tvBrand = (TextView) convertView.findViewById(R.id.device_info_brand);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.device_info_tv_name);
            viewHolder.tvAddress = (TextView) convertView.findViewById(R.id.device_info_tv_mac);
            viewHolder.tvStatus = (TextView) convertView.findViewById(R.id.device_info_tv_status);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device = mBluetoothDeviceList.get(position);
        viewHolder.tvBrand.setText(TextUtils.substring(device.getName().toUpperCase(), 0, 1));
        viewHolder.tvName.setText(device.getName());
        viewHolder.tvAddress.setText(device.getAddress());
        viewHolder.tvStatus.setText(GattHelper.getStatusText(device.getBondState()));

        return convertView;
    }

    private final class ViewHolder {
        public TextView tvBrand;
        public TextView tvName;
        public TextView tvAddress;
        public TextView tvStatus;
    }
}
