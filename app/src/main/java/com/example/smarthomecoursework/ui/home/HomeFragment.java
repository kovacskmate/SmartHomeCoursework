package com.example.smarthomecoursework.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.example.smarthomecoursework.SaveManager;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

import static android.content.Context.MODE_PRIVATE;
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
        Log.i("device size", " " + SaveManager.devices.size());
        for (int i = 0; i < SaveManager.devices.size(); i++)
        {
            DrawDevice(SaveManager.devices.get(i).id,
                    SaveManager.devices.get(i).name,
                    SaveManager.devices.get(i).width,
                    SaveManager.devices.get(i).height,
                    SaveManager.devices.get(i).leftMargin,
                    SaveManager.devices.get(i).topMargin,
                    SaveManager.devices.get(i).status
            );
        }
        return root;
    }

    public void DrawDevice(int id, String name, int width, int height, int leftMargin, int topMargin, String status){
        ImageView iv = new ImageView(MainActivity.myapp);
        iv.setId(id);
        Log.i("status", "" + status);
        if(status.equals("true") ){
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
                        //TODO: move this separate method?
                        Log.d("image", "id: " + view.getId());
                        Log.d("image", "name: " + SaveManager.devices.get(view.getId()).name);
                        ImageView imgView = root.findViewById(view.getId());
                        //TODO: set image resource according to type AND status
                        if(SaveManager.devices.get(view.getId()).status.equals("true")){
                            imgView.setImageResource(R.drawable.ic_menu_gallery);
                            SaveManager.devices.get(view.getId()).status = "false";
                        } else{
                            imgView.setImageResource(R.drawable.ic_menu_camera);
                            SaveManager.devices.get(view.getId()).status = "true";
                        }
                        new LongOperation().execute(SaveManager.devices.get(view.getId()).id);
                        SaveManager.getInstance().SavePreferences(MainActivity.myapp);
                    }
                    isDrag = false;
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
                    SaveManager.devices.get(view.getId()).leftMargin = layoutParams.leftMargin;
                    SaveManager.devices.get(view.getId()).topMargin = layoutParams.topMargin;
                    layoutParams.rightMargin = -250;
                    layoutParams.bottomMargin = -250;
                    view.setLayoutParams(layoutParams);
                    SaveManager.getInstance().SavePreferences(MainActivity.myapp);
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
                ParticleCloudSDK.getCloud().logIn("kovacskmate@gmail.com", "smartHomeCW12");
            } catch (ParticleLoginException e) {
                e.printStackTrace();
            }
            try {
                particleDevice = ParticleCloudSDK.getCloud().getDevice("e00fce688b18465fa09104e9");
            } catch (ParticleCloudException e) {
                e.printStackTrace();
            }
            try {
                List<String> someList = new ArrayList<String>();
                someList.add(SaveManager.devices.get(params[0]).type);
                someList.add(SaveManager.devices.get(params[0]).pin);
                someList.add(SaveManager.devices.get(params[0]).status);
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
}