package com.example.smarthomecoursework.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.smarthomecoursework.MainActivity;
import com.example.smarthomecoursework.R;
import com.example.smarthomecoursework.SaveManager;
import com.google.android.material.snackbar.Snackbar;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.cloud.exceptions.ParticleLoginException;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.ui.Toaster;

import static android.content.Context.MODE_PRIVATE;
import static java.net.Proxy.Type.HTTP;

public class HomeFragment extends Fragment {

    private ImageView img;
    private ViewGroup rootLayout;
    private View root;
    private int _xDelta;
    private int _yDelta;
    ParticleDevice particleDevice;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        new ParticleLogin().execute();
        root = inflater.inflate(R.layout.fragment_home, container, false);
        rootLayout = (ViewGroup) root.findViewById(R.id.view_root);
        if(SaveManager.floorPlan != null){
            byte[] decodedString = Base64.decode(SaveManager.floorPlan, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            Drawable dr = new BitmapDrawable(decodedByte);
            rootLayout.setBackground(dr);
        }
        for (int i = 0; i < SaveManager.devices.size(); i++)
        {
            DrawDevice(SaveManager.devices.get(i).id,
                    SaveManager.devices.get(i).name,
                    SaveManager.devices.get(i).width,
                    SaveManager.devices.get(i).height,
                    SaveManager.devices.get(i).leftMargin,
                    SaveManager.devices.get(i).topMargin,
                    SaveManager.devices.get(i).status,
                    SaveManager.devices.get(i).type
            );
        }
        return root;
    }

    public void DrawDevice(int id, String name, int width, int height, int leftMargin, int topMargin, String status, String type){
        ImageView iv = new ImageView(MainActivity.myapp);
        iv.setId(id);
        if(status.equals("true") ){
            if(type.equals("Light")){
                iv.setImageDrawable(MainActivity.myapp.getDrawable(R.drawable.ic_lamp_on));
            } else if (type.equals("Door")){
                iv.setImageDrawable(MainActivity.myapp.getDrawable(R.drawable.ic_door_open));
            } else if (type.equals("Temperature sensor")){
                iv.setImageDrawable(MainActivity.myapp.getDrawable(R.drawable.ic_temperature));
            } else if (type.equals("Range finder")){
                iv.setImageDrawable(MainActivity.myapp.getDrawable(R.drawable.ic_rangefinder));
            } else if (type.equals("Light sensor")){
                iv.setImageDrawable(MainActivity.myapp.getDrawable(R.drawable.ic_light_sensor));
            }else{
                iv.setImageDrawable(MainActivity.myapp.getDrawable(R.drawable.ic_menu_camera));
            }
        } else{
            if(type.equals("Light")){
                iv.setImageDrawable(MainActivity.myapp.getDrawable(R.drawable.ic_lamp_off));
            } else if (type.equals("Door")){
                iv.setImageDrawable(MainActivity.myapp.getDrawable(R.drawable.ic_door_locked));
            } else if (type.equals("Temperature sensor")){
                iv.setImageDrawable(MainActivity.myapp.getDrawable(R.drawable.ic_temperature));
            } else if (type.equals("Range finder")){
                iv.setImageDrawable(MainActivity.myapp.getDrawable(R.drawable.ic_rangefinder));
            } else if (type.equals("Light sensor")){
                iv.setImageDrawable(MainActivity.myapp.getDrawable(R.drawable.ic_light_sensor));
            } else{
                iv.setImageDrawable(MainActivity.myapp.getDrawable(R.drawable.ic_menu_gallery));
            }
        }
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.leftMargin = leftMargin;
        layoutParams.topMargin = topMargin;
        layoutParams.rightMargin = -250;
        layoutParams.bottomMargin = -250;
        iv.setLayoutParams(layoutParams);
        iv.setOnTouchListener(new ChoiceTouchListener());

        TextView tv = new TextView(MainActivity.myapp);
        tv.setText(name);
        tv.setTag(name);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.parseColor("#FF3700B3"));
        tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.TOP);

        RelativeLayout.LayoutParams layoutParamsTv = new RelativeLayout.LayoutParams(350, 350);
        layoutParamsTv.leftMargin = leftMargin;
        layoutParamsTv.topMargin = topMargin - 60;
        layoutParamsTv.rightMargin = -250;
        layoutParamsTv.bottomMargin = -250;
        tv.setLayoutParams(layoutParamsTv);
        rootLayout.addView(iv);
        rootLayout.addView(tv);
    }

    public boolean isDrag = false;

    final Handler handler = new Handler();
    Runnable mLongPressed = new Runnable() {
        public void run() {
            isDrag = true;
            Log.i("", "Long press!");
        }
    };

    private final class ChoiceTouchListener implements View.OnTouchListener {
        @SuppressLint("ResourceType")
        public boolean onTouch(View view, MotionEvent event) {
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    _xDelta = X - lParams.leftMargin;
                    _yDelta = Y - lParams.topMargin;
                    handler.postDelayed(mLongPressed, 90);
                    break;
                case MotionEvent.ACTION_UP:
                    if (!isDrag) {
                        ImageView imgView = root.findViewById(view.getId());
                        if(SaveManager.devices.get(view.getId()).type.equals("Light") || SaveManager.devices.get(view.getId()).type.equals("Door")){
                            if(SaveManager.devices.get(view.getId()).status.equals("true")){
                                if(SaveManager.devices.get(view.getId()).type.equals("Light")){
                                    imgView.setImageResource(R.drawable.ic_lamp_off);
                                } else if (SaveManager.devices.get(view.getId()).type.equals("Door")){
                                    imgView.setImageResource(R.drawable.ic_door_locked);
                                } else{
                                    imgView.setImageResource(R.drawable.ic_menu_gallery);
                                }
                                SaveManager.devices.get(view.getId()).status = "false";
                            } else{
                                if(SaveManager.devices.get(view.getId()).type.equals("Light")){
                                    imgView.setImageResource(R.drawable.ic_lamp_on);
                                } else if (SaveManager.devices.get(view.getId()).type.equals("Door")){
                                    imgView.setImageResource(R.drawable.ic_door_open);
                                } else{
                                    imgView.setImageResource(R.drawable.ic_menu_camera);
                                }
                                SaveManager.devices.get(view.getId()).status = "true";
                            }
                            new LongOperation().execute(SaveManager.devices.get(view.getId()).id);
                        }
                        SaveManager.getInstance().SavePreferences(MainActivity.myapp);
                    }
                    handler.removeCallbacks(mLongPressed);
                    isDrag = false;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
                case MotionEvent.ACTION_MOVE:
                    //isDrag = true;
                    //new WaitForDrag().execute();
                    if (isDrag) {
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        layoutParams.leftMargin = X - _xDelta;
                        layoutParams.topMargin = Y - _yDelta;
                        SaveManager.devices.get(view.getId()).leftMargin = layoutParams.leftMargin;
                        SaveManager.devices.get(view.getId()).topMargin = layoutParams.topMargin;
                        layoutParams.rightMargin = -250;
                        layoutParams.bottomMargin = -250;
                        view.setLayoutParams(layoutParams);

                        RelativeLayout.LayoutParams layoutParamsTv = new RelativeLayout.LayoutParams(350, 350);
                        layoutParamsTv.leftMargin = layoutParams.leftMargin;
                        layoutParamsTv.topMargin = layoutParams.topMargin - 60;
                        layoutParamsTv.rightMargin = -250;
                        layoutParamsTv.bottomMargin = -250;

                        root.findViewWithTag(SaveManager.devices.get(view.getId()).name).setLayoutParams(layoutParamsTv);

                        SaveManager.getInstance().SavePreferences(MainActivity.myapp);
                        handler.removeCallbacks(mLongPressed);
                    }
                    break;
            }
            rootLayout.invalidate();
            return true;
        }
    }



    private final class ParticleLogin extends AsyncTask<Integer, Integer, String> {
        @Override
        protected String doInBackground(Integer... params) {
            try {
                ParticleCloudSDK.getCloud().logIn(SaveManager.email, SaveManager.passw);
            } catch (ParticleLoginException e) {
                e.printStackTrace();
            }

            try {
                particleDevice = ParticleCloudSDK.getCloud().getDevice(SaveManager.devid);
            } catch (ParticleCloudException e) {
                e.printStackTrace();
            }
            Log.i("async", "login finished");
            new SubscribeToTemp().execute();
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            //...
        }
    }

    private final class LongOperation extends AsyncTask<Integer, Integer, String> {
        @Override
        protected String doInBackground(Integer... params) {
            try {
                List<String> someList = new ArrayList<String>();
                someList.add(SaveManager.devices.get(params[0]).type);
                someList.add(SaveManager.devices.get(params[0]).pin);
                someList.add(SaveManager.devices.get(params[0]).status);
                someList.add(SaveManager.devices.get(params[0]).attachedLED);
                someList.add(SaveManager.devices.get(params[0]).triggerPin);
                particleDevice.callFunction("recieveCommand", someList);
            } catch (ParticleCloudException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParticleDevice.FunctionDoesNotExistException e) {
                e.printStackTrace();
            }
            Log.i("async", "finished");
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            //...
        }
    }

    private Map<Integer, Integer> rangeFinderReadings = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> tempSensorReadings = new HashMap<Integer, Integer>();
    private Map<Integer, String> lightSensorReadings = new HashMap<Integer, String>();

    private final class SubscribeToTemp extends AsyncTask<Integer, Integer, String> {
        @Override
        protected String doInBackground(Integer... params) {
            try {
                particleDevice.unsubscribeFromEvents(SaveManager.subId);
            } catch (ParticleCloudException e) {
                e.printStackTrace();
            }

            long subscriptionId = 0;  // save this for later, for unsubscribing
            try {
                subscriptionId = ParticleCloudSDK.getCloud().subscribeToMyDevicesEvents(
                        null,  // the first argument, "eventNamePrefix", is optional
                        new ParticleEventHandler() {
                            public void onEvent(String eventName, ParticleEvent event) {
                                if(eventName.contains("tempSensor")){
                                    String deviceId = eventName.substring(10, eventName.length());
                                    for(int i = 0; i < SaveManager.devices.size(); i++){
                                        if(SaveManager.devices.get(i).pin.equals(deviceId)){
                                            TextView tv = root.findViewWithTag(SaveManager.devices.get(i).name);
                                            if(tv != null){
                                                tv.setText(SaveManager.devices.get(i).name + ": " + Math.round(Double.parseDouble(event.getDataPayload())) + " C");
                                                if(!tempSensorReadings.containsKey(SaveManager.devices.get(i).id)){
                                                    tempSensorReadings.put(SaveManager.devices.get(i).id, (int)Math.round(Double.parseDouble(event.getDataPayload())));
                                                } else{
                                                    if((int)Double.parseDouble(event.getDataPayload()) < 20 && SaveManager.tempNoti && tempSensorReadings.get(SaveManager.devices.get(i).id) > 20){
                                                       String notification =  "Temperature at " + SaveManager.devices.get(i).name + " dropped below 20 C!";
                                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.myapp, "23")
                                                                .setSmallIcon(R.drawable.ic_temperature)
                                                                .setContentTitle("Temperature dropped!")
                                                                .setContentText(notification)
                                                                .setPriority(NotificationCompat.PRIORITY_MAX);
                                                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.myapp);
                                                        notificationManager.notify(4, builder.build());
                                                        tempSensorReadings.put(SaveManager.devices.get(i).id, (int)Math.round(Double.parseDouble(event.getDataPayload())));
                                                    } else{
                                                        tempSensorReadings.put(SaveManager.devices.get(i).id, (int)Math.round(Double.parseDouble(event.getDataPayload())));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if(eventName.contains("rangeFinder")){
                                    String deviceId = eventName.substring(11, eventName.length());
                                    //Log.i("range device id: ", "" + deviceId);
                                    for(int i = 0; i < SaveManager.devices.size(); i++){
                                        if(SaveManager.devices.get(i).pin.equals(deviceId)){
                                            TextView tv = root.findViewWithTag(SaveManager.devices.get(i).name);
                                            if(tv != null && root != null){
                                                tv.setText(SaveManager.devices.get(i).name + ": " + Math.round(Double.parseDouble(event.getDataPayload())) + " In");
                                                if(!rangeFinderReadings.containsKey(SaveManager.devices.get(i).id)){
                                                    rangeFinderReadings.put(SaveManager.devices.get(i).id, (int)Math.round(Double.parseDouble(event.getDataPayload())));
                                                } else{
                                                    int difference = Math.abs(rangeFinderReadings.get(SaveManager.devices.get(i).id)) - (int)Double.parseDouble(event.getDataPayload());
                                                    if(Math.abs(difference) > 4 && SaveManager.rangeNoti){
                                                        String notification = SaveManager.devices.get(i).name + " detected movement!";
                                                        try{
                                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.myapp, "23")
                                                                    .setSmallIcon(R.drawable.ic_rangefinder)
                                                                    .setContentTitle("Movement detected!")
                                                                    .setContentText(notification)
                                                                    .setPriority(NotificationCompat.PRIORITY_MAX);
                                                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.myapp);
                                                            notificationManager.notify(5, builder.build());
                                                        } catch (Exception e){

                                                        }
                                                        rangeFinderReadings.put(SaveManager.devices.get(i).id, (int)Math.round(Double.parseDouble(event.getDataPayload())));
                                                    } else{
                                                        rangeFinderReadings.put(SaveManager.devices.get(i).id, (int)Math.round(Double.parseDouble(event.getDataPayload())));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if(eventName.contains("lightSensor")){
                                    String deviceId = eventName.substring(11, eventName.length());
                                    for(int i = 0; i < SaveManager.devices.size(); i++){
                                        if(SaveManager.devices.get(i).pin.equals(deviceId)){
                                            TextView tv = root.findViewWithTag(SaveManager.devices.get(i).name);
                                            if(tv != null){
                                                String brightness = "";
                                                if(Double.parseDouble(event.getDataPayload()) < 700){
                                                    brightness = ": dark";
                                                } else if(Double.parseDouble(event.getDataPayload()) < 1500){
                                                    brightness = ": dim";
                                                } else{
                                                    brightness = ": bright";
                                                }
                                                tv.setText(SaveManager.devices.get(i).name + brightness);
                                                if(!lightSensorReadings.containsKey(SaveManager.devices.get(i).id)){
                                                    lightSensorReadings.put(SaveManager.devices.get(i).id, brightness);
                                                } else{
                                                    if(!lightSensorReadings.get(SaveManager.devices.get(i).id).equals(brightness) && SaveManager.lightNoti){
                                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.myapp, "23")
                                                                .setSmallIcon(R.drawable.ic_light_sensor)
                                                                .setContentTitle("Brightness changed!")
                                                                .setContentText(SaveManager.devices.get(i).name + brightness)
                                                                .setPriority(NotificationCompat.PRIORITY_MAX);
                                                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.myapp);
                                                        notificationManager.notify(6, builder.build());

                                                        Log.i("brightness", "" + brightness);
                                                        lightSensorReadings.put(SaveManager.devices.get(i).id, brightness);
                                                    } else{
                                                        if(!SaveManager.devices.get(i).attachedLED.equals("-") && SaveManager.automateLights){
                                                            if(brightness.equals(": bright")){
                                                                //turn light on
                                                                int id = -1;
                                                                for (int k = 0; k < SaveManager.devices.size(); k++){
                                                                    if(SaveManager.devices.get(k).pin.equals(SaveManager.devices.get(i).attachedLED)){
                                                                        id = SaveManager.devices.get(k).id;
                                                                    }
                                                                }
                                                                SaveManager.devices.get(id).status = "false";
                                                                ImageView imgView = root.findViewById(id);
                                                                imgView.setImageResource(R.drawable.ic_lamp_off);
                                                                new LongOperation().execute(id);
                                                            }

                                                            if(brightness.equals(": dark")){
                                                                //turn light off
                                                                int id = -1;
                                                                for (int k = 0; k < SaveManager.devices.size(); k++){
                                                                    if(SaveManager.devices.get(k).pin.equals(SaveManager.devices.get(i).attachedLED)){
                                                                        id = SaveManager.devices.get(k).id;
                                                                    }
                                                                }
                                                                SaveManager.devices.get(id).status = "true";
                                                                ImageView imgView = root.findViewById(id);
                                                                imgView.setImageResource(R.drawable.ic_lamp_on);
                                                                new LongOperation().execute(id);
                                                            }
                                                        }
                                                        lightSensorReadings.put(SaveManager.devices.get(i).id, brightness);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                //Log.i("some tag", "Received event with payload: " +  event.getDataPayload());
                            }
                            public void onEventError(Exception e) {
                                Log.e("some tag", "Event error: ", e);
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i("async", "sub to events finished");
            SaveManager.subId = subscriptionId;
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            //...
        }
    }

}