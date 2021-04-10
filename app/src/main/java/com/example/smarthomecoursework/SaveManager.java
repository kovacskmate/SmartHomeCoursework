package com.example.smarthomecoursework;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.Integer.parseInt;

public class SaveManager {

    private static SaveManager Instace = null;

    public static List<Device> devices = new ArrayList<Device>();

    public static String floorPlan;

    public static int tempInterval = 3000;
    public static int rangeInterval = 500;
    public static int lightInterval = 3000;

    public static boolean tempNoti = false;
    public static boolean rangeNoti = false;
    public static boolean lightNoti = false;
    public static boolean automateLights = false;

    public static long subId;

    public static String email = "asd@gmail.com";
    public static String passw = "asd";
    public static String devid = "asd";

    public static class Device{
        public int id;
        public String name;
        public String type;
        public String pin;
        public int width;
        public int height;
        public int leftMargin;
        public int topMargin;
        public String status;
        public String attachedLED;
        public String triggerPin;

        public Device(int id, String name, String type, String pin, int width, int height, int leftMargin, int topMargin, String status, String attachedLED, String triggerPin) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.pin = pin;
            this.width = width;
            this.height = height;
            this.leftMargin = leftMargin;
            this.topMargin = topMargin;
            this.status = status;
            this.attachedLED = attachedLED;
            this.triggerPin = triggerPin;
        }
    }

    private SaveManager() {

    }

    public static SaveManager getInstance() {
        if (Instace == null) {
            Instace = new SaveManager();
        }
        return(Instace);
    }

    public void SavePreferences(Context ctx){
        try {
            FileOutputStream fileout = ctx.openFileOutput("preferences.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);

            for(int i = 0; i < devices.size(); i++){
                outputWriter.write(devices.get(i).id + ";" + devices.get(i).name + ";" + devices.get(i).type + ";" + devices.get(i).pin +  ";" + devices.get(i).width + ";" + devices.get(i).height + ";" + devices.get(i).leftMargin + ";" + devices.get(i).topMargin + ";" + devices.get(i).status + ";" + devices.get(i).attachedLED + ";" + devices.get(i).triggerPin + "\n");
            }
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ReadPreferencesFile(Context ctx){
        String s="";
        try {
            FileInputStream fileIn = ctx.openFileInput("preferences.txt");
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            char[] inputBuffer= new char[500];

            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i("trying to read device", "" + s);

        String[] lines = s.split("\n");
        String[] line;
        for(int i = 0; i < lines.length; i++){
            line = lines[i].split(";");
            if(line[0] != ""){
                devices.add(new Device(
                        parseInt(line[0]),
                        line[1],
                        line[2],
                        line[3],
                        parseInt(line[4]),
                        parseInt(line[5]),
                        parseInt(line[6]),
                        parseInt(line[7]),
                        line[8],
                        line[9],
                        line[10])
                );
            }
        }
    }

    public void ClearPreferences(Context ctx){
        devices.clear();
        try {
            FileOutputStream fileout = ctx.openFileOutput("preferences.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);

            for(int i = 0; i < devices.size(); i++){
                outputWriter.write(devices.get(i).id + ";" + devices.get(i).name + ";" + devices.get(i).type + ";" + devices.get(i).pin +  ";" + devices.get(i).width + ";" + devices.get(i).height + ";" + devices.get(i).leftMargin + ";" + devices.get(i).topMargin + ";" + devices.get(i).status + ";" + devices.get(i).attachedLED + ";" + devices.get(i).triggerPin +"\n");
            }
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SavePreferences(MainActivity.myapp);
    }

    public void SaveFloorplan(Context ctx, String base64){
        final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        //prefs.edit().putBoolean("showStats", value).apply();
        Log.i("SaveFloorplan", "saving" + base64);
        prefs.edit().putString("floorPlan", base64).apply();
        floorPlan = base64;
    }

    public void ReadFloorplan(Context ctx){
        SharedPreferences prefs = ctx.getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        Log.i("floorPlan", prefs.getString("floorPlan", "stuff"));
        try{
            floorPlan = prefs.getString("floorPlan", null);
        }catch (Exception e){

        }
    }

    public void SaveTempInterval(Context ctx, int interval){
        final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt("tempInterval", interval).apply();
        //prefs.edit().putString("floorPlan", base64).apply();
        tempInterval = interval;
    }

    public void ReadTempInterval(Context ctx){
        SharedPreferences prefs = ctx.getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        try{
            tempInterval = prefs.getInt("tempInterval", 500);
        }catch (Exception e){

        }
    }

    public void SaveRangeInterval(Context ctx, int interval){
        final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt("rangeInterval", interval).apply();
        rangeInterval = interval;
    }

    public void ReadRangeInterval(Context ctx){
        SharedPreferences prefs = ctx.getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        try{
            rangeInterval = prefs.getInt("rangeInterval", 500);
        }catch (Exception e){

        }
    }

    public void SaveLightInterval(Context ctx, int interval){
        final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt("lightInterval", interval).apply();
        lightInterval = interval;
    }

    public void ReadLightInterval(Context ctx){
        SharedPreferences prefs = ctx.getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        try{
            lightInterval = prefs.getInt("lightInterval", 500);
        }catch (Exception e){

        }
    }

    public void SaveLightNoti(Context ctx, boolean noti){
        final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean("lightNoti", noti).apply();
        lightNoti = noti;
    }

    public void ReadLightNoti(Context ctx){
        SharedPreferences prefs = ctx.getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        try{
            lightNoti = prefs.getBoolean("lightNoti", true);
        }catch (Exception e){

        }
    }

    public void SaveRangeNoti(Context ctx, boolean noti){
        final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean("rangeNoti", noti).apply();
        rangeNoti = noti;
    }

    public void ReadRangeNoti(Context ctx){
        SharedPreferences prefs = ctx.getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        try{
            rangeNoti = prefs.getBoolean("rangeNoti", true);
        }catch (Exception e){

        }
    }

    public void SaveTempNoti(Context ctx, boolean noti){
        final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean("tempNoti", noti).apply();
        tempNoti = noti;
    }

    public void ReadTempNoti(Context ctx){
        SharedPreferences prefs = ctx.getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        try{
            tempNoti = prefs.getBoolean("tempNoti", true);
        }catch (Exception e){

        }
    }

    public void SaveAutomateLights(Context ctx, boolean automate){
        final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean("automateLights", automate).apply();
        automateLights = automate;
    }

    public void ReadAutomateLights(Context ctx){
        SharedPreferences prefs = ctx.getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        try{
            automateLights = prefs.getBoolean("automateLights", true);
        }catch (Exception e){

        }
    }
}
