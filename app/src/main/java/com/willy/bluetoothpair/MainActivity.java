package com.willy.bluetoothpair;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.bluetooth_state)
    TextView bluetoothStateText;
    @Bind(R.id.listview_bluetooth_device)
    ListView listviewBluetoothDevice;
    @Bind(R.id.button_discovery)
    Button buttonDiscovery;
    @Bind(R.id.button_enable_bluetooth)
    Button buttonEnableBluetooth;

    private BluetoothDeviceAdapter bluetoothDeviceAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private ProgressDialog scanningProgressDialog;
    private ProgressDialog pairingProgressDialog;

    // Create a BroadcastReceiver for Bluetooth
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    final int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                    if (bluetoothState == BluetoothAdapter.STATE_ON) {
                        bluetoothStateText.setText("Bluetooth is on");
                        buttonEnableBluetooth.setText("DISABLE BLUETOOTH");
                        buttonDiscovery.setEnabled(true);
                    } else {
                        bluetoothStateText.setText("Bluetooth is off");
                        buttonEnableBluetooth.setText("ENABLE BLUETOOTH");
                        buttonDiscovery.setEnabled(false);

                        bluetoothDeviceAdapter.cleanDeviceList();
                        bluetoothDeviceAdapter.notifyDataSetChanged();
                    }

                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    bluetoothDeviceAdapter.cleanDeviceList();
                    scanningProgressDialog.show();
                    break;

                // When discovery finds a device
                case BluetoothDevice.ACTION_FOUND:
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    // Add the device to an array to show in a ListView
                    bluetoothDeviceAdapter.addDevice(device);

                    bluetoothDeviceAdapter.notifyDataSetChanged();
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    scanningProgressDialog.dismiss();

                    if (bluetoothDeviceAdapter.getListBluetoothDevice().size() <= 0) {
                        Toast.makeText(MainActivity.this, "no device found", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                    if (state == BluetoothDevice.BOND_BONDING) {
                        pairingProgressDialog.show();
                    } else {
                        pairingProgressDialog.dismiss();

                        bluetoothDeviceAdapter.notifyDataSetChanged();

                        if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                            Toast.makeText(MainActivity.this, "Paired", Toast.LENGTH_SHORT).show();
                        } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {

                            Toast.makeText(MainActivity.this, "Unpaired", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        InitObject();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        unregisterReceiver(mReceiver);
    }

    private void InitObject() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, "bluetooth is unsupported by this device", Toast.LENGTH_SHORT).show();
        }

        scanningProgressDialog = new ProgressDialog(this);
        scanningProgressDialog.setMessage("Scanning ...");
        scanningProgressDialog.setCancelable(false);
        scanningProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mBluetoothAdapter.cancelDiscovery();
            }
        });

        pairingProgressDialog = new ProgressDialog(this);
        pairingProgressDialog.setMessage("Pairing ...");
        pairingProgressDialog.setCancelable(false);

        bluetoothDeviceAdapter = new BluetoothDeviceAdapter(this);

        listviewBluetoothDevice.setAdapter(bluetoothDeviceAdapter);

        buttonDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBluetoothAdapter.startDiscovery();
            }
        });

        buttonEnableBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mBluetoothAdapter.isEnabled()) {
                    startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
                } else {
                    mBluetoothAdapter.disable();
                }
            }
        });

        if (mBluetoothAdapter.isEnabled()) {
            bluetoothStateText.setText("Bluetooth is on");
            buttonEnableBluetooth.setText("DISABLE BLUETOOTH");
            buttonDiscovery.setEnabled(true);
        }

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        registerReceiver(mReceiver, filter);
    }
}
