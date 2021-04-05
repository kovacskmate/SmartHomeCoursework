package com.example.smarthomecoursework.ui.devices;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.smarthomecoursework.MainActivity;
import com.example.smarthomecoursework.R;
import com.example.smarthomecoursework.SaveManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.cloud.exceptions.ParticleLoginException;

public class DevicesFragment extends Fragment {
    ArrayAdapter<String> adapter;
    ArrayList<String> listItems=new ArrayList<String>();
    ListView listv;
    ParticleDevice particleDevice;

    //TODO: rename these
    private String m_Text1 = "";
    private String m_Text2 = "";
    private String m_Text3 = "";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_devices, container, false);

        listv = (ListView) root.findViewById(R.id.list);
        adapter = new ArrayAdapter<String>(MainActivity.myapp, android.R.layout.simple_list_item_1, listItems);
        listv.setAdapter(adapter);

        for(int i = 0; i < SaveManager.devices.size(); i++){
            addItems(listv, SaveManager.devices.get(i));
        }

        final Button addDevice = root.findViewById(R.id.addDevice);
        addDevice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                builder.setTitle("Create new device");
                View builderView = inflater.inflate(R.layout.device_dialog, null);

                final EditText deviceName = (EditText)builderView.findViewById(R.id.device_name);
                final EditText deviceType = (EditText)builderView.findViewById(R.id.device_type);
                final EditText devicePin = (EditText)builderView.findViewById(R.id.device_pin);

                builder.setView(builderView);

                builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //EditText mEdit = (EditText)findViewById(R.id.device_name);
                        m_Text1 = deviceName.getText().toString();
                        m_Text2 = deviceType.getText().toString();
                        m_Text3 = devicePin.getText().toString();
                        Log.i("user input", " " + m_Text1);
                        Log.i("user input", " " + m_Text2);
                        Log.i("user input", " " + m_Text3);
                        SaveManager.Device device = new SaveManager.Device(SaveManager.devices.size(), m_Text1, m_Text2, m_Text3, 150, 150, 500, 500, "false") ;
                        SaveManager.devices.add(device);
                        SaveManager.getInstance().SavePreferences(MainActivity.myapp);
                        new DevicesFragment.SendDeviceToCLoud().execute(SaveManager.devices.size());
                        addItems(listv, device);
                        //TODO: publish device to cloud database?
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        final Button deleteDevices = root.findViewById(R.id.deleteDevices);
        deleteDevices.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("button", "delete devices pressed");
                SaveManager.getInstance().ClearPreferences(MainActivity.myapp);
                //TODO: alert dialog "this will delete every device"?
                listItems.clear();
                adapter.notifyDataSetChanged();
            }
        });
        return root;
    }

    //TODO: why pass View?
    public void addItems(View v, SaveManager.Device device) {
        listItems.add("Name: " + device.name + " Status: " + device.status);
        adapter.notifyDataSetChanged();
    }

    private final class SendDeviceToCLoud extends AsyncTask<Integer, Integer, String> {
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
                someList.add(SaveManager.devices.get(params[0]-1).type);
                someList.add(SaveManager.devices.get(params[0]-1).pin);
                someList.add(SaveManager.devices.get(params[0]-1).status);
                particleDevice.callFunction("recieveDevice", someList);

            } catch (ParticleCloudException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParticleDevice.FunctionDoesNotExistException e) {
                e.printStackTrace();
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            //...
        }
    }
}