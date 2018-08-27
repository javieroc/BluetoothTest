package com.devandcoffee.bluetoothtest;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private final int REQUEST_ENABLE_BT = 1234;

    private Button mTurnOnBluetoothButton;

    private Button mStartDiscoveryButton;

    private BluetoothAdapter mBluetoothAdapter;

    private RecyclerView mRecyclerView;

    private RecyclerViewAdapter mAdapter;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.i("DISCOVERY START", ": " + action);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i("DISCOVERY FINISHED", ": " + action);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                Log.i("BLUETOOTH DEVICE", ": " + device.getName());
                mAdapter.addItem(device.getName() + "\n" + device.getAddress());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTurnOnBluetoothButton = findViewById(R.id.turn_on_bluetooth);

        mTurnOnBluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    Toast.makeText(getApplicationContext(), "Enabling Bluetooth!", Toast.LENGTH_LONG).show();
                }
            }
        });

        mStartDiscoveryButton = findViewById(R.id.start_discovery);
        mStartDiscoveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDiscovery();
            }
        });

        mRecyclerView = findViewById(R.id.bluetooth_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RecyclerViewAdapter(new ArrayList<String>());
        mRecyclerView.setAdapter(mAdapter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Device Not Support Bluetooth", Toast.LENGTH_LONG).show();
        } else {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    mAdapter.addItem(device.getName() + "\n" + device.getAddress());
                }
            }
        }
    }

    public void btnDiscovery() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();

            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            // Register the BroadcastReceiver
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mReceiver, filter);
        }

        if (!mBluetoothAdapter.isDiscovering()) {
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            // Register the BroadcastReceiver
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mReceiver, filter);
        }
    }

    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            permissionCheck += ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionCheck != 0) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {

            //If the request was successful…//
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has been enabled!", Toast.LENGTH_SHORT).show();
            }

            //If the request was unsuccessful...//
            if(resultCode == RESULT_CANCELED){
                Toast.makeText(this, "An error occurred while attempting to enable Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }
}
