package com.Group17;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;



public class BluetoothConnection extends AppCompatActivity {

    public static final int REQUEST_ENABLE_BT=1;
    ListView lv_paired_devices;
    Set<BluetoothDevice> set_pairedDevices;
    ArrayAdapter adapter_paired_devices;
    BluetoothAdapter bluetoothAdapter;
    String bluetooth_message="00";
    private static final String TAG = "BluetoothConnection";

    private BluetoothService myService;
    private boolean isServiceBound;
    private ServiceConnection serviceConnection;
    private  Intent serviceIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Handler: ON_CREATE");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_layout);

        serviceIntent=new Intent(getApplicationContext(),BluetoothService.class);

        //BluetoothServiceInterface myBSI = new BluetoothServiceInterface(serviceIntent);
        //myBSI.run();

        //gotta get the results out of that

        bindService();


        Button button_done = (Button) findViewById(R.id.button_done);



        button_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!(isServiceBound && myService != null && myService.isRunning())){
                    Toast.makeText(getApplicationContext(),"No connection, no sense exiting",Toast.LENGTH_SHORT).show();
                }
                else {
                    unbindService();
                    finish();
                }
            }
        });


    }


    public void initialize_clicks()
    {
        Log.d(TAG, "Handler: initialize_clicks");
        lv_paired_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Object[] objects = set_pairedDevices.toArray();
                BluetoothDevice device = (BluetoothDevice) objects[position];

                if(isServiceBound && myService != null){
                    Toast.makeText(getApplicationContext(),"device chosen "+device.getName(),Toast.LENGTH_SHORT).show();
                    myService.connect(device);
                }
            }
        });
    }

    public void initialize_layout()
    {
        Log.d(TAG, "Handler: initialize_layout");
        lv_paired_devices = (ListView)findViewById(R.id.lv_paired_devices);
        adapter_paired_devices = new ArrayAdapter(getApplicationContext(),R.layout.support_simple_spinner_dropdown_item);
        lv_paired_devices.setAdapter(adapter_paired_devices);
    }

    public void initialize_bluetooth()
    {
        Log.d(TAG, "Handler: initialize_bluetooth");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(getApplicationContext(),"Your Device doesn't support bluetooth. you can play as Single player",Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        else {
            set_pairedDevices = bluetoothAdapter.getBondedDevices();

            if (set_pairedDevices.size() > 0) {

                for (BluetoothDevice device : set_pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address

                    adapter_paired_devices.add(device.getName() + "\n" + device.getAddress());
                }
            }
        }
    }


    private void bindService(){
        if(serviceConnection==null){
            serviceConnection=new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    BluetoothService.BluetoothServiceBinder myServiceBinder=(BluetoothService.BluetoothServiceBinder)iBinder;
                    myService=myServiceBinder.getService();
                    isServiceBound=true;

                    if(isServiceBound && myService  != null && myService.isRunning())
                    {
                        Log.d(TAG, "MyService Not NULL");
                    }
                    else
                    {
                        Log.d("ACTIVITY SetBluetooth", "NULL");
                    }

                    initialize_layout();
                    initialize_bluetooth();
                    initialize_clicks();
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    isServiceBound=false;
                }
            };
        }

        bindService(serviceIntent,serviceConnection, Context.BIND_AUTO_CREATE);

    }

    private void unbindService(){
        if(isServiceBound){
            unbindService(serviceConnection);
            isServiceBound=false;
        }
    }


}