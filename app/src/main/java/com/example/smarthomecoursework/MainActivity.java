package com.example.smarthomecoursework;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.example.smarthomecoursework.ui.home.HomeFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.cloud.exceptions.ParticleLoginException;
import io.particle.android.sdk.utils.Async;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Intent.ACTION_OPEN_DOCUMENT;

public class MainActivity extends AppCompatActivity {

    //TODO: Most particle sdk calls only work if called on a background thread (Async methods).
    private AppBarConfiguration mAppBarConfiguration;
    ParticleDevice particleDevice;

    public static Context myapp;

    AsyncTask<Void, Void, String> runningTask;

    SaveManager sm = SaveManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACTION_OPEN_DOCUMENT ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, INTERNET,},1 );
        }

        ParticleCloudSDK.init(MainActivity.this);
        checkFirstRun();
        createNotificationChannel();
        setContentView(R.layout.activity_main);
        MainActivity.myapp = getApplicationContext();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel running task(s) to avoid memory leaks
        //TODO: running task not used?
        if (runningTask != null)
            runningTask.cancel(true);
    }

    //This code is from https://stackoverflow.com/questions/7217578/check-if-application-is-on-its-first-run by the users Squonk and Suragch
    private void checkFirstRun() {
        final String PREFS_NAME = "MyPrefsFile";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;

        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {
            // This is just a normal run
            sm.ReadPreferencesFile(getApplicationContext());
            sm.ReadFloorplan(getApplicationContext());
            sm.ReadRangeInterval(getApplicationContext());
            sm.ReadTempInterval(getApplicationContext());
            sm.ReadLightInterval(getApplicationContext());
            sm.ReadLightNoti(getApplicationContext());
            sm.ReadRangeNoti(getApplicationContext());
            sm.ReadTempNoti(getApplicationContext());
            sm.ReadAutomateLights(getApplicationContext());
            //TODO: sync device statuses with argon device
            new LongOperation().execute();
            //sm.SavePreferences(getApplicationContext());
            return;
        } else if (savedVersionCode == DOESNT_EXIST) {
            //This is a new install (or the user cleared the shared preferences)
            //sm.ReadPreferencesFile(getApplicationContext());
            sm.SavePreferences(getApplicationContext());
            sm.SaveRangeInterval(getApplicationContext(), 500);
            sm.SaveTempInterval(getApplicationContext(), 3000);
            sm.SaveLightInterval(getApplicationContext(), 3000);
            sm.SaveTempNoti(getApplicationContext(), true);
            sm.SaveRangeNoti(getApplicationContext(), true);
            sm.SaveLightNoti(getApplicationContext(), true);
            sm.SaveAutomateLights(getApplicationContext(), true);
            new LongOperation().execute();
        } else if (currentVersionCode > savedVersionCode) {
            //This is an upgrade, nothing to do here
        }
        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }

    private final class LongOperation extends AsyncTask<Integer, Integer, String> {
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

            try {
                for (int i = 0; i < SaveManager.devices.size(); i++){
                    List<String> someList = new ArrayList<String>();
                    someList.add(SaveManager.devices.get(i).type);
                    someList.add(SaveManager.devices.get(i).pin);
                    someList.add(SaveManager.devices.get(i).status);
                    someList.add(SaveManager.devices.get(i).attachedLED);
                    someList.add(SaveManager.devices.get(i).triggerPin);
                    particleDevice.callFunction("recieveDevice", someList);
                }
            } catch (ParticleCloudException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParticleDevice.FunctionDoesNotExistException e) {
                e.printStackTrace();
            }

            try {
                for (int i = 0; i < SaveManager.devices.size(); i++){
                    List<String> someList = new ArrayList<String>();
                    someList.add(SaveManager.devices.get(i).type);
                    someList.add(SaveManager.devices.get(i).pin);
                    someList.add(SaveManager.devices.get(i).status);
                    someList.add(SaveManager.devices.get(i).attachedLED);
                    someList.add(SaveManager.devices.get(i).triggerPin);
                    particleDevice.callFunction("recieveCommand", someList);
                }
            } catch (ParticleCloudException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParticleDevice.FunctionDoesNotExistException e) {
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

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MyChannel";
            String description = "My description";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("23", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}