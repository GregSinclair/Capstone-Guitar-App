package com.Group17;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MemoryInterface { //all currently untested

    //final static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/guitargame/readwrite/" ;
    final static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "guitarGame" ;
    final static String TAG = "Memory Interface";

    private final static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private final static int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    public static boolean checkIfFileExists(String fileName){
        File file = new File(path+ File.separator+ fileName);
        if(file.exists()){
            Log.d(TAG, "file exists");
            return true;
        }
        Log.d(TAG, "file does not exist");
        return false;
    }

    public static void initProgression(Context context){ //fire this and the settings one on launch
        String fileName ="progression.txt";
        if (!(checkIfFileExists(fileName))){
            MemoryInterface.writeFile(getDefaultProgression(context, fileName), fileName);
        }
        else{
            try { //yoinked from the settings version, hence the names. all internal so not a problem
                JSONObject original = MemoryInterface.readFile(fileName);
                JSONObject jSettings = getDefaultProgression(context, fileName);
                Iterator<String> settingsIterator=jSettings.keys();
                while(settingsIterator.hasNext()) {
                    String current = settingsIterator.next();
                    if(!original.has(current)){
                        original.put(current, jSettings.get(current));
                    }
                }
                MemoryInterface.writeFile(original, fileName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static JSONObject getDefaultProgression(Context context, String fileName){
        JSONObject initialProgression = new JSONObject();
        try {

            String jtxt = loadJSONFromAsset(context);
            JSONObject json = new JSONObject(jtxt);


            Iterator<String> songIterator=json.keys();

            while(songIterator.hasNext()){
                String currentName = songIterator.next();
                JSONObject currentSong = new JSONObject(); //creating this to put in the result
                JSONArray partNames = json.getJSONObject(currentName).getJSONArray("partNames"); //getting this so I know how many booleans to put in
                currentSong.put(""+0, false);
                for(int i=1;i<partNames.length();i++){
                    currentSong.put(""+i, false); //0 being the full song. Logic is weird because we skip 1, but I think it checks out
                }
                initialProgression.put(currentName,currentSong);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return initialProgression;
    }

    public static JSONObject readFile(String fileName){
        JSONObject result=new JSONObject();
        String line=null;

        try {
            FileInputStream fileInputStream = new FileInputStream (new File(path+ File.separator+ fileName));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while ( (line = bufferedReader.readLine()) != null )
            {
                stringBuilder.append(line + System.getProperty("line.separator"));
            }
            fileInputStream.close();
            line = stringBuilder.toString();

            bufferedReader.close();

            result = new JSONObject(line);
        }
        catch(FileNotFoundException ex) {
            Log.d(TAG, ex.getMessage());
        }
        catch(IOException ex) {
            Log.d(TAG, ex.getMessage());
        }
        catch(JSONException e){
            Log.d(TAG, e.getMessage());
        }
        return result;
    }


    public static void writeFile(JSONObject newData, String fileName){
        //selective write, or just pull the whole file, modify parts, and write it back? Mass rewrite seems easier

        if(!(checkIfFileExists(fileName))) { //calls this twice while initializing, but thats fine
            FileWriter fWriter = null; //seems like it gets in here even if the file exists
            try {
                new File(path+ File.separator).mkdirs();
                File file = new File(path +File.separator+ fileName);
                file.createNewFile();
                fWriter = new FileWriter(file, false);
                fWriter.write(newData.toString());
                fWriter.close();
                Log.d(TAG, "new file created");
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        else{ //if file does exist
            try {

                JSONObject original = readFile(fileName);
                JSONObject result = new JSONObject();
                List<String> keyNames = new ArrayList<String>();
                List<String> originalNames = new ArrayList<String>();
                List<String> newNames = new ArrayList<String>();
                Iterator<String> originalIterator=original.keys();
                Iterator<String> newIterator=newData.keys();
                while(originalIterator.hasNext()){
                    String current = originalIterator.next();
                    keyNames.add(current);
                    originalNames.add(current);
                }
                while(newIterator.hasNext()){
                    String current = newIterator.next();
                    keyNames.add(current);
                    newNames.add(current);
                }
                for(int i=0; i<keyNames.size();i++){
                    String key = keyNames.get(i);
                    if(newNames.contains(key)){
                        result.put(key,newData.get(key));
                    }
                    else if(originalNames.contains(key)){
                        result.put(key,original.get(key));
                    }
                }

                //now overwrite the file

                FileWriter fWriter = new FileWriter(new File(path+ File.separator+ fileName), false);
                fWriter.write(result.toString());
                fWriter.close();

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean checkReadPermission(Activity theActivity){

        if (ContextCompat.checkSelfPermission(theActivity, "android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {

           ActivityCompat.requestPermissions(theActivity, new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
        } else {
            // Permission has already been granted
            return true;
        }
        return false;
    }

    public static boolean checkWritePermission(Activity theActivity){

        if (ContextCompat.checkSelfPermission(theActivity, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(theActivity, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } else {
            // Permission has already been granted
            return true;
        }
        return false;
    }

    public static String loadJSONFromAsset(Context context) { //in theory I could modify this so that it's called by all classes statically. Better not to for now
        String json = null;
        try {

            InputStream is=context.getAssets().open("songs.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }

}
