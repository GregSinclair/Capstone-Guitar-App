package com.Group17;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.support.*;
//annotation.Nullable;
//import android.support.v7.app.AppCompatActivity;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Parcel;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.UUID;



public class BluetoothConnection extends AppCompatActivity {

    public static final int REQUEST_ENABLE_BT=1;
    ListView lv_paired_devices;
    Set<BluetoothDevice> set_pairedDevices;
    ArrayAdapter adapter_paired_devices;
    BluetoothAdapter bluetoothAdapter;

    public static UUID MY_UUID;
    public static final int MESSAGE_READ=0;
    public static final int MESSAGE_WRITE=1;
    public static final int CONNECTING=2;
    public static final int CONNECTED=3;
    public static final int NO_SOCKET_FOUND=4;

    ConnectedThread mConnectedThread;

    String bluetooth_message="00";

    private static final String TAG = "MainActivity";






    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        String zeros = "000000";
        Random rnd = new Random();
        String s = Integer.toString(rnd.nextInt(0X1000000), 16);
        s = zeros.substring(s.length()) + s;
        MY_UUID = UUID.fromString( "12345601-0000-1000-8000-008051234567");
        //MY_UUID = UUID.fromString( s + "01-0000-1000-8000-008051234567");
        //Log.d(TAG, "Handler: " + s + "01-0000-1000-8000-00805F9B34FB");
        Log.d(TAG, "Handler: ON_CREATE");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_layout);
        initialize_layout();
        initialize_bluetooth();
        start_accepting_connection();
        initialize_clicks();


        Button button1 = (Button) findViewById(R.id.button1);
        Button button2 = (Button) findViewById(R.id.button2);
        Button button_done = (Button) findViewById(R.id.button_done);
        Button button_read = (Button) findViewById(R.id.button_read);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mConnectedThread == null){
                    Toast.makeText(getApplicationContext(),"No connection",Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    bluetooth_message = "button 1 pressed*";
                    mConnectedThread.write(bluetooth_message.getBytes());
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mConnectedThread == null){
                    Toast.makeText(getApplicationContext(),"No connection",Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    bluetooth_message = "very different message than the other button*";
                    mConnectedThread.write(bluetooth_message.getBytes());
                }
            }
        });

        button_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mConnectedThread == null){
                    Toast.makeText(getApplicationContext(),"No connection, no sense exiting",Toast.LENGTH_SHORT).show();
                }
                if(mConnectedThread != null) {
                    Intent intent = new Intent(BluetoothConnection.this, MainActivity.class);
                    intent.putExtra("bluetoothConnection", mConnectedThread);
                    setResult(RESULT_OK,intent);
                    finish();
                }
            }
        });
        button_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mConnectedThread == null){
                    Toast.makeText(getApplicationContext(),"No connection",Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    Toast.makeText(getApplicationContext(),mConnectedThread.getLastMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void start_accepting_connection()
    {
        Log.d(TAG, "Handler: start_accepting_connection");
        //call this on button click as suited by you

        AcceptThread acceptThread = new AcceptThread();
        acceptThread.start();
        //Toast.makeText(getApplicationContext(),"accepting",Toast.LENGTH_SHORT).show();
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

                ConnectThread connectThread = new ConnectThread(device);
                connectThread.start();

                Toast.makeText(getApplicationContext(),"device choosen "+device.getName(),Toast.LENGTH_SHORT).show();
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


    public class AcceptThread extends Thread
    {
        private final BluetoothServerSocket serverSocket;

        public AcceptThread() {
            Log.d(TAG, "Accept Thread: OnCreate");
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("NAME",MY_UUID);
            } catch (IOException e) { Log.d(TAG, "Accept Thread: OnCreate Catch"); }
            serverSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "Accept Thread: Run");
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    Log.d(TAG, "Accept Thread: Run Loop Try1");
                    socket = serverSocket.accept();
                    Log.d(TAG, "Accept Thread: Run Loop Try2");
                } catch (IOException e) {
                    Log.d(TAG, "Accept Thread: Run Loop Break");
                    break;
                }

                // If a connection was accepted
                if (socket != null)
                {
                    // Do work to manage the connection (in a separate thread)
                    Log.d(TAG, "onReceive: STATE OFF");

                    connected(socket);
                    mConnectedThread.write("accept thread completed*");
                    socket=null;
                }
            }
        }
    }


    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            Log.d(TAG, "Connect Thread: OnCreate");
            BluetoothSocket tmp = null;
            mmDevice = device;
            Method m = null;
            try {m = device.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});}
            catch (NoSuchMethodException ex){Log.d(TAG, "Connect Thread: NoMethod");}
            try {
                int bt_port_to_connect = 5;

                // MY_UUID is the app's UUID string, also used by the server code
                //tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                tmp = (BluetoothSocket) m.invoke(device, bt_port_to_connect);
            }
            catch (IllegalAccessException e1) {Log.d(TAG, "Connect Thread: IllegalAccessException");}
            catch (InvocationTargetException e2) {Log.d(TAG, "Connect Thread: TargetException");}
            mmSocket = tmp;

        /*
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            Log.d(TAG, "Connect Thread: OnCreate");
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;

         */
        }

        public void run() {
            Log.d(TAG, "Connect Thread: Run");
            // Cancel discovery because it will slow down the connection
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception


                mmSocket.connect();
                if(mmSocket != null){
                    //connected(mmSocket);
                    Log.d(TAG, "Connect Thread: Connected Start");
                    // Do work to manage the connection (in a separate thread)
                    bluetooth_message = "Initial message*and the second line*";
                    connected(mmSocket);
                    mConnectedThread.write("connect thread completed*");

                }
                else{Log.d(TAG, "Connect Thread: Connected Socket Null");}
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                Log.d(TAG, "Connect Thread: Cannot Start");
                return;
            }





        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }


    private void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "connect method");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }



}