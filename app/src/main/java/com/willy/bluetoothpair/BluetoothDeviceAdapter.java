package com.willy.bluetoothpair;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Willy on 2015/10/3.
 */
public class BluetoothDeviceAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private List<BluetoothDevice> listBluetoothDevice = new ArrayList<>();

    public BluetoothDeviceAdapter(Context context){
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listBluetoothDevice.size();
    }

    @Override
    public Object getItem(int position) {
        return listBluetoothDevice.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(R.layout.list_item, viewGroup, false);

        TextView deviceName = (TextView)view.findViewById(R.id.tv_device_name);
        Switch isPaired = (Switch)view.findViewById(R.id.switch_ispaired);
        isPaired.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                BluetoothDevice device = listBluetoothDevice.get(position);

                if(checked){
                    pairDevice(device);
                }else{
                    unpairDevice(device);
                }

            }
        });

        BluetoothDevice device = listBluetoothDevice.get(position);
        deviceName.setText(device.getName());

        if(device.getBondState() == BluetoothDevice.BOND_BONDED){
            isPaired.setChecked(true);
        }else{
            isPaired.setChecked(false);
        }

        return view;
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cleanDeviceList(){
        listBluetoothDevice.clear();
    }

    public void addDevice(BluetoothDevice device){
        listBluetoothDevice.add(device);
    }

    public List<BluetoothDevice> getListBluetoothDevice() {
        return listBluetoothDevice;
    }
}
