package com.example.smarthomecoursework;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

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
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.cloud.exceptions.ParticleLoginException;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    public static Context myapp;

    public static List<Device> devices = new ArrayList<Device>();

    public ParticleDevice particleDevice;

    AsyncTask<Void, Void, String> runningTask;

    public static class Device{
        public int id;
        public String name;
        public String type;
        public String pin;
        public int width;
        public int height;
        public int leftMargin;
        public int topMargin;
        public boolean status;

        public Device(int id, String name, String type, String pin, int width, int height, int leftMargin, int topMargin, boolean status) {
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ParticleCloudSDK.init(MainActivity.this);
        setContentView(R.layout.activity_main);
        MainActivity.myapp = getApplicationContext();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //TODO: populate list from cloud?
        devices.add(new Device(0, "first", "light","D7",350,350, 250, 250, true));
        devices.add(new Device(1, "second", "door","D3",350,350, 250, 650, true));

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

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



//        if (runningTask != null)
//            runningTask.cancel(true);
//        runningTask = new LongOperation();
//        runningTask.execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
        if (runningTask != null)
            runningTask.cancel(true);
    }

    private final class LongOperation extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                ParticleCloudSDK.getCloud().logIn("kovacskmate@gmail.com", "F12f85995");
            } catch (ParticleLoginException e) {
                e.printStackTrace();
            }
            try {
                particleDevice = ParticleCloudSDK.getCloud().getDevice("e00fce688b18465fa09104e9");
            } catch (ParticleCloudException e) {
                e.printStackTrace();
            }
            try {
                particleDevice.callFunction("brew", Collections.singletonList("coffee"));
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