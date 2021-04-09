package com.example.smarthomecoursework.ui.settings;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.content.CursorLoader;

import com.example.smarthomecoursework.MainActivity;
import com.example.smarthomecoursework.R;
import com.example.smarthomecoursework.SaveManager;
import com.example.smarthomecoursework.ui.devices.DevicesFragment;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.cloud.exceptions.ParticleLoginException;

public class SettingsFragment extends Fragment {

    private EditText range_interval;
    private EditText temp_interval;
    private EditText light_interval;
    private Button settings_browse;
    private Button settings_save;
    private String floorPlanBase64 = "";
    private ParticleDevice particleDevice;

    private static final int GALLERY_REQUEST_CODE = 123;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        range_interval = (EditText) root.findViewById(R.id.range_interval);
        range_interval.setText(Integer.toString(SaveManager.rangeInterval));
        range_interval.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                Log.i("range interval changed", " " + range_interval.getText());
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        temp_interval = (EditText) root.findViewById(R.id.temp_interval);
        temp_interval.setText(Integer.toString(SaveManager.tempInterval));
        temp_interval.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                Log.i("temp interval changed", " " + temp_interval.getText());
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        light_interval = (EditText) root.findViewById(R.id.light_interval);
        light_interval.setText(Integer.toString(SaveManager.lightInterval));
        light_interval.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                Log.i("temp interval changed", " " + light_interval.getText());
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        settings_browse = (Button) root.findViewById(R.id.settings_browse);
        settings_browse.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(Intent.createChooser(intent, "Pick an image"), GALLERY_REQUEST_CODE);
            }
        });

        settings_save = (Button) root.findViewById(R.id.settings_save);
        settings_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!floorPlanBase64.equals("")){
                    SaveManager.getInstance().SaveFloorplan(MainActivity.myapp, floorPlanBase64);
                }
                if(!temp_interval.getText().toString().equals("")){
                    SaveManager.getInstance().SaveTempInterval(MainActivity.myapp, Integer.parseInt(temp_interval.getText().toString()));
                }
                if(!range_interval.getText().toString().equals("")){
                    SaveManager.getInstance().SaveRangeInterval(MainActivity.myapp, Integer.parseInt(range_interval.getText().toString()));
                }
                if(!light_interval.getText().toString().equals("")){
                    SaveManager.getInstance().SaveLightInterval(MainActivity.myapp, Integer.parseInt(light_interval.getText().toString()));
                }
                //TODO: login to particle then send intervals
                new SetIntervalsOnArgon().execute();
            }
        });
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null){
            Uri imageUri = data.getData();
            InputStream imageStream = null;
            try {
                imageStream = MainActivity.myapp.getContentResolver().openInputStream(imageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
            floorPlanBase64 = encodeImage(selectedImage);
            //Log.i("base64", "" + floorPlanBase64);
        }
    }

    private String encodeImage(Bitmap bm)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encImage;
    }

    private final class SetIntervalsOnArgon extends AsyncTask<Integer, Integer, String> {
        @Override
        protected String doInBackground(Integer... params) {
            try {
                ParticleCloudSDK.getCloud().logIn("asd@gmail.com", "asd");
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
                someList.add(Integer.toString(SaveManager.tempInterval));
                someList.add(Integer.toString(SaveManager.rangeInterval));
                someList.add(Integer.toString(SaveManager.lightInterval));
                particleDevice.callFunction("recieveSetInterval", someList);
            } catch (ParticleCloudException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParticleDevice.FunctionDoesNotExistException e) {
                e.printStackTrace();
            }
            Log.i("async", "login finished");
            return "Executed";
        }
        @Override
        protected void onPostExecute(String result) {
            //...
        }
    }
}