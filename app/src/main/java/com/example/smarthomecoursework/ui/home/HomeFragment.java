package com.example.smarthomecoursework.ui.home;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.smarthomecoursework.MainActivity;
import com.example.smarthomecoursework.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.cloud.exceptions.ParticleLoginException;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.ui.Toaster;

import static java.net.Proxy.Type.HTTP;

public class HomeFragment extends Fragment {

    private ImageView img;
    private ViewGroup rootLayout;
    private View root;
    private int _xDelta;
    private int _yDelta;
    ParticleDevice particleDevice;

    AsyncTask<Void, Void, String> runningTask;
    String status = "false";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        rootLayout = (ViewGroup) root.findViewById(R.id.view_root);
        Log.i("device size", " " + MainActivity.devices.size());
        for (int i = 0; i < MainActivity.devices.size(); i++)
        {
            DrawDevice(MainActivity.devices.get(i).id,MainActivity.devices.get(i).name,MainActivity.devices.get(i).width,MainActivity.devices.get(i).height,MainActivity.devices.get(i).leftMargin,MainActivity.devices.get(i).topMargin,MainActivity.devices.get(i).status);
        }
        return root;
    }


    public void DrawDevice(int id, String name, int width, int height, int leftMargin, int topMargin, String status){
        //move to class?
        // Initialize a new ImageView widget
        ImageView iv = new ImageView(MainActivity.myapp);
        iv.setId(id);
        // Set an image for ImageView
        if(status == "true"){
            iv.setImageDrawable(MainActivity.myapp.getDrawable(R.drawable.ic_menu_camera));
        } else{
            iv.setImageDrawable(MainActivity.myapp.getDrawable(R.drawable.ic_menu_gallery));
        }
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.leftMargin = leftMargin;
        layoutParams.topMargin = topMargin;
        layoutParams.rightMargin = -250;
        layoutParams.bottomMargin = -250;
        iv.setLayoutParams(layoutParams);
        iv.setOnTouchListener(new ChoiceTouchListener());
        Log.d("button", "id: " + rootLayout);

        //add the ImageView to layout
        rootLayout.addView(iv);
    }

    private final class ChoiceTouchListener implements View.OnTouchListener {
        private boolean isDrag = false;
        @SuppressLint("ResourceType")
        public boolean onTouch(View view, MotionEvent event) {
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    _xDelta = X - lParams.leftMargin;
                    _yDelta = Y - lParams.topMargin;
                    break;
                case MotionEvent.ACTION_UP:
                    if (!isDrag) {
                        //TODO: move this separate method
                        Log.d("image", "id: " + view.getId());
                        Log.d("image", "name: " + MainActivity.devices.get(view.getId()).name);

                        ImageView imgView = root.findViewById(view.getId());
                        //TODO: set image resource according to type AND status
                        if(MainActivity.devices.get(view.getId()).status == "true"){
                            imgView.setImageResource(R.drawable.ic_menu_gallery);
                            MainActivity.devices.get(view.getId()).status = "false";
//
//                            if (runningTask != null)
//                                runningTask.cancel(true);
//                            runningTask = new LongOperation();
//                            runningTask.execute();
                            new LongOperation().execute(MainActivity.devices.get(view.getId()).id);

                        } else{
                            imgView.setImageResource(R.drawable.ic_menu_camera);
                            MainActivity.devices.get(view.getId()).status = "true";

//                            if (runningTask != null)
//                                runningTask.cancel(true);
//                            runningTask = new LongOperation();
//                            runningTask.execute();
                            new LongOperation().execute(MainActivity.devices.get(view.getId()).id);

                        }
                        //TODO: publish status to cloud
                    }
                    isDrag = false; // reset the flag
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
                case MotionEvent.ACTION_MOVE:
                    isDrag = true;
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    layoutParams.leftMargin = X - _xDelta;
                    layoutParams.topMargin = Y - _yDelta;

                    MainActivity.devices.get(view.getId()).leftMargin = layoutParams.leftMargin;
                    MainActivity.devices.get(view.getId()).topMargin = layoutParams.topMargin;

                    layoutParams.rightMargin = -250;
                    layoutParams.bottomMargin = -250;
                    view.setLayoutParams(layoutParams);

                    //TODO: save device margins in Device class
                    break;
            }
            rootLayout.invalidate();
            return true;
        }
    }

    private final class LongOperation extends AsyncTask<Integer, Integer, String> {
        @Override
        protected String doInBackground(Integer... params) {
            try {
                ParticleCloudSDK.getCloud().logIn("asd@asd.com", "asd");
            } catch (ParticleLoginException e) {
                e.printStackTrace();
            }
            try {
                particleDevice = ParticleCloudSDK.getCloud().getDevice("asd");
            } catch (ParticleCloudException e) {
                e.printStackTrace();
            }
            try {
                List<String> someList = new ArrayList<String>();
                someList.add(MainActivity.devices.get(params[0]).type);
                someList.add(MainActivity.devices.get(params[0]).pin);
                someList.add(MainActivity.devices.get(params[0]).status);
                particleDevice.callFunction("brew", someList);
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
            // You might want to change "executed" for the returned string
            // passed into onPostExecute(), but that is up to you
        }
    }

}