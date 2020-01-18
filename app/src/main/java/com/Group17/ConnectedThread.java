package com.Group17;

import android.bluetooth.BluetoothSocket;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.lang.Object;
import java.nio.charset.StandardCharsets;

public class ConnectedThread extends Thread implements Parcelable {

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    private String lastMessage="";
    private JSONObject lastJSONObject=null; //the json will be sent over as text and then converted

    private static final String TAG = "ConnectedThread";

    public static final Parcelable.Creator<ConnectedThread> CREATOR
            = new Parcelable.Creator<ConnectedThread>() {
        public ConnectedThread createFromParcel(Parcel in) {
            return new ConnectedThread(in);
        }

        public ConnectedThread[] newArray(int size) {
            return new ConnectedThread[size];
        }
    };

    /*
    private ConnectedThread(Parcel givenParcel){
        //this.mmSocket = givenParcel.readTypedObject(BluetoothSocket.class.getClassLoader());
        this.mmSocket = givenParcel.readParcelable(BluetoothSocket.class.getClassLoader()); //is bts even parcelable? might be errors because of this
        this.mmInStream = givenParcel.readParcelable(InputStream.class.getClassLoader());
        this.mmOutStream = givenParcel.readParcelable(OutputStream.class.getClassLoader());
    }

     */

    private ConnectedThread(Parcel givenParcel){
        mmSocket=null;
        mmInStream=null;
        mmOutStream=null;
    }



    public ConnectedThread(BluetoothSocket socket) {
        Log.d(TAG, "Connected Thread: Connected Start");
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
            Log.d(TAG, "Connected Thread: Initialization Success");
        } catch (IOException e) {
            Log.d(TAG, "Connected Thread: Initialization Failure");

        }

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
        Gson g = new Gson();
        while (true) {
            Log.d(TAG, "Connected Thread: Looping");
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                String oneChar= new String(buffer);
                Log.d(TAG, "Connected Thread: got character: "+oneChar);
                if(oneChar.contains("*")){
                    Log.d(TAG, "Connected Thread: message received: "+message);
                    lastMessage=message;
                    //Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                    message="";
                }
                else if(oneChar.contains("^")){
                    Log.d(TAG, "Connected Thread: json received: "+message);
                    //JSONObject newJSON = g.toJson(message);
                    InputStream stream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
                    JsonReader reader = new JsonReader(new InputStreamReader(stream));
                    //JsonReader reader = Json.createReader(stream);
                    // Get the JsonObject structure from JsonReader.
                    //JsonObject newJsonObject = reader.readObject(); //looks like this method isnt supposed to be used ever

                    int messageType=0;
                    int sequence=0;
                    int timeStamp=0;
                    int[] results = new int[6];

                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name = reader.nextName();
                        if (name.equals("type")) {
                            messageType = reader.nextInt();
                        } else if (name.equals("sequence")) {
                            sequence = reader.nextInt();
                        } else if (name.equals("timeStamp")) {
                            timeStamp = reader.nextInt();
                        } else if (name.equals("values")){
                            reader.beginArray();
                            int i=0;
                            while (reader.hasNext()&&i<6) {
                                results[i]=reader.nextInt();
                                i++;
                            }
                            reader.endArray();
                        } else if (name.equals("first")) {
                            results[0] = reader.nextInt();
                        }
                        else if (name.equals("second")) {
                            results[1] = reader.nextInt();
                        }
                        else if (name.equals("third")) {
                            results[2] = reader.nextInt();
                        }
                        else if (name.equals("fourth")) {
                            results[3] = reader.nextInt();
                        }
                        else if (name.equals("fifth")) {
                            results[4] = reader.nextInt();
                        }
                        else if (name.equals("sixth")) {
                            results[5] = reader.nextInt();
                        }else {
                            reader.skipValue();
                        }
                    }
                    reader.endObject();

                    JSONObject json = new JSONObject();
                    json.put("type", messageType);
                    json.put("sequence", sequence);
                    json.put("timeStamp", timeStamp);
                    json.put("values", results);

                    reader.close();

                    //message types:
                    //1:ping
                    //2:phone to rpi
                    //3:results
                    //4:error

                    if (messageType==3){
                        lastJSONObject=json;
                    }

                    //Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                    message="";
                }
                else{
                    message+=oneChar;
                }
                // Send the obtained bytes to the UI activity
                //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();

                Log.d(TAG, "Connected Thread: Sent Read");
            } catch (IOException | JSONException e) {
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



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    /*
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mmSocket,0);
        dest.writeParcelable(mmInStream,0);
        dest.writeParcelable(mmOutStream,0);

    }

     */
    public void writeToParcel(Parcel dest, int flags) {
        //dest.writeParcelable(this,0);
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
            byte[] bytes = "Keeping alive".getBytes(); //verify that this works, might need to choose utf8 or something
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { Log.d(TAG, "KeepingAlive Thread: Write Error"); }
        }
    }
}