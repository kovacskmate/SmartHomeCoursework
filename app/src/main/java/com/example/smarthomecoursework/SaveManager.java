package com.example.smarthomecoursework;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.Integer.parseInt;

public class SaveManager {

    private static SaveManager Instace = null;

    public static List<Device> devices = new ArrayList<Device>();

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

        public Device(int id, String name, String type, String pin, int width, int height, int leftMargin, int topMargin, String status) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.pin = pin;
            this.width = width;
            this.height = height;
            this.leftMargin = leftMargin;
            this.topMargin = topMargin;
            this.status = status;
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
                outputWriter.write(devices.get(i).id + ";" + devices.get(i).name + ";" + devices.get(i).type + ";" + devices.get(i).pin +  ";" + devices.get(i).width + ";" + devices.get(i).height + ";" + devices.get(i).leftMargin + ";" + devices.get(i).topMargin + ";" + devices.get(i).status + "\n");
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

            char[] inputBuffer= new char[100];

            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                        line[8])
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
                outputWriter.write(devices.get(i).id + ";" + devices.get(i).name + ";" + devices.get(i).type + ";" + devices.get(i).pin +  ";" + devices.get(i).width + ";" + devices.get(i).height + ";" + devices.get(i).leftMargin + ";" + devices.get(i).topMargin + ";" + devices.get(i).status + "\n");
            }
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SavePreferences(MainActivity.myapp);

    }
}
