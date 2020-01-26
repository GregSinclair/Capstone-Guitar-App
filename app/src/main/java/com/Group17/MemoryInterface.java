package com.Group17;

import android.os.Environment;
import android.util.Log;

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


public class MemoryInterface {

    private final static String fileName = "userData.txt";
    final static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/guitargame/readwrite/" ;
    final static String TAG = "Memory Interface";

    public static JSONObject readFile(){
        JSONObject result=null;
        String line=null;

        try {
            FileInputStream fileInputStream = new FileInputStream (new File(path + fileName));
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

    public static void writeFile(JSONObject newData){
        //selective write, or just pull the whole file, modify parts, and write it back? Mass rewrite seems easier
        try {

            JSONObject original = readFile();
            JSONObject result = new JSONObject();
            List<String> keyNames = new ArrayList<String>();
            List<String> originalNames = new ArrayList<String>();
            List<String> newNames = new ArrayList<String>();
            Iterator<String> originalIterator=original.keys();
            Iterator<String> newIterator=original.keys();
            while(originalIterator.hasNext()){
                keyNames.add(originalIterator.next());
                originalNames.add(originalIterator.next());
            }
            while(newIterator.hasNext()){
                keyNames.add(newIterator.next());
                newNames.add(newIterator.next());
            }
            for(int i=0; i<keyNames.size();i++){
                String key = keyNames.get(i);
                if(newNames.contains(key)){

                    result.put(key,newData.getInt(key)); //assuming its always Int values we store, reasonable for now

                }
                else if(originalNames.contains(key)){
                    result.put(key,original.getInt(key));
                }
            }

            //now overwrite the file

            FileWriter fWriter = new FileWriter(new File(path + fileName), false);
            fWriter.write(result.toString());
            fWriter.close();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
