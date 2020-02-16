package com.Group17;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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



}
