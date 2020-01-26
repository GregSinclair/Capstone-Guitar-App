package com.Group17;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    private String lastMessage="";
    private JSONObject lastJSONObject=null; //the json will be sent over as text and then converted

    private static final String TAG = "ConnectedThread";



    public ConnectedThread(BluetoothSocket socket) {
        Log.d(TAG, "Create");
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
            Log.d(TAG, "Connected Thread: Initialization Success");
        } catch (IOException e) { Log.d(TAG, "Connected Thread: Initialization Failure"); }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

        KeepingAlive ohOhOhOh = new KeepingAlive(mmSocket, mmInStream, mmOutStream);
        ohOhOhOh.start();

    }

    public void run() {
        Log.d(TAG, "Connected Thread: Run");
        byte[] buffer = new byte[1];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs

        String message="";

        while (true) {
            Log.d(TAG, "Connected Thread: Looping");
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                String oneChar= new String(buffer);
                Log.d(TAG, "Connected Thread: got character: "+oneChar);
                if(oneChar.contains("*")){
                    Log.d(TAG, "Connected Thread: message recieved: "+message);
                    lastMessage=message;
                    //Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                    message="";
                }
                else{
                    message+=oneChar;
                }
                // Send the obtained bytes to the UI activity
                //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();

                Log.d(TAG, "Connected Thread: Sent Read");
            } catch (IOException e) {
                Log.d(TAG, "Connected Thread: Loop Escape");
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        Log.d(TAG, "Connect Thread: Write");
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { Log.d(TAG, "Connected Thread: Write Error"); }
    }

    public void write(String newMessage){
        Log.d(TAG, "Connect Thread: WriteString");
        Log.d(TAG, newMessage);
        byte[] bytes = newMessage.getBytes(); //verify that this works, might need to choose utf8 or something
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { Log.d(TAG, "Connected Thread: Write Error"); }
    }

    //make another one for json
    public String getLastMessage(){
        return this.lastMessage;
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }

    public JSONObject getLastJSONMessage(){
        return lastJSONObject;
    }


    private class KeepingAlive extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean running = true;
        public KeepingAlive(BluetoothSocket socket, InputStream input, OutputStream output){
            mmSocket=socket;
            mmInStream=input;
            mmOutStream=output;
        }
        public void run() {
            while (running) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                write();
            }
        }
        public void terminate(){ //really ought to call this somewhere
            running = false;
        }
        private void write(){
            Log.d(TAG, "Oh Oh Oh Oh STAYIN ALIVE");
            JSONObject json = new JSONObject();
            try {
                JSONArray beat = new JSONArray();
                beat.put(0);
                beat.put(0);
                beat.put(2);
                beat.put(2);
                beat.put(2);
                beat.put(0);
                json.put("type", 2);
                json.put("sequence", 0);
                json.put("timeStamp", 0);
                json.put("values", beat);
                json.put("duration", 500);
            } catch (JSONException e) {
            e.printStackTrace();
            }

            byte[] bytes = json.toString().getBytes(); //verify that this works, might need to choose utf8 or something
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { Log.d(TAG, "KeepingAlive Thread: Write Error"); }
        }
    }
}