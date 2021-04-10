package com.example.smarthomecoursework.ui.devices;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableRow;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.smarthomecoursework.MainActivity;
import com.example.smarthomecoursework.R;
import com.example.smarthomecoursework.SaveManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.cloud.exceptions.ParticleLoginException;

public class DevicesFragment extends Fragment {
    ArrayAdapter<String> adapter;
    ArrayList<String> listItems=new ArrayList<String>();
    ListView listv;
    ParticleDevice particleDevice;

    //TODO: rename these
    private String deviceName = "";
    private String deviceType = "";
    private String devicePin = "";
    private String attachedLed = "";
    private String triggerPin = "";

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
                View builderView = inflater.inflate(R.layout.device_dialog_scroll, null);

                final EditText deviceName = (EditText)builderView.findViewById(R.id.device_name);
                final EditText devicePin = (EditText)builderView.findViewById(R.id.device_pin);
                final EditText attachedLed = (EditText)builderView.findViewById(R.id.attached_led);
                attachedLed.setEnabled(false);
                attachedLed.setInputType(InputType.TYPE_NULL);
                final EditText triggerPin = (EditText)builderView.findViewById(R.id.trigger_pin);
                triggerPin.setEnabled(false);
                triggerPin.setInputType(InputType.TYPE_NULL);

                builder.setView(builderView);

                Spinner spinner = (Spinner) builderView.findViewById(R.id.mySpinner);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MainActivity.myapp, R.array.device_types_array, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        if(spinner.getSelectedItem().toString().equals("Light sensor")){
                            Log.i("item changed", "" + spinner.getSelectedItem());
                            attachedLed.setEnabled(true);
                            attachedLed.setInputType(InputType.TYPE_CLASS_NUMBER);
                            triggerPin.setEnabled(false);
                            triggerPin.setInputType(InputType.TYPE_NULL);
                        }
                        if(spinner.getSelectedItem().toString().equals("Range finder")){
                            triggerPin.setEnabled(true);
                            triggerPin.setInputType(InputType.TYPE_CLASS_NUMBER);
                            attachedLed.setEnabled(false);
                            attachedLed.setInputType(InputType.TYPE_NULL);
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {

                    }
                });

                builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DevicesFragment.this.deviceName = deviceName.getText().toString();
                        DevicesFragment.this.deviceType = spinner.getSelectedItem().toString();
                        DevicesFragment.this.devicePin = devicePin.getText().toString();
                        DevicesFragment.this.attachedLed = attachedLed.getText().toString();
                        DevicesFragment.this.triggerPin = triggerPin.getText().toString();

                        if (DevicesFragment.this.attachedLed.equals("")){
                            DevicesFragment.this.attachedLed = "-";
                        }
                        if (DevicesFragment.this.triggerPin.equals("")){
                            DevicesFragment.this.triggerPin = "-";
                        }

                        SaveManager.Device device = new SaveManager.Device(SaveManager.devices.size(), DevicesFragment.this.deviceName, DevicesFragment.this.deviceType, DevicesFragment.this.devicePin, 150, 150, 500, 500, "false", DevicesFragment.this.attachedLed, DevicesFragment.this.triggerPin) ;
                        Log.i("new device", "" + device.toString());
                        SaveManager.devices.add(device);
                        SaveManager.getInstance().SavePreferences(MainActivity.myapp);
                        new DevicesFragment.SendDeviceToCLoud().execute(SaveManager.devices.size());
                        addItems(listv, device);
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
                new DeleteDevicesOnArgon().execute();
                listItems.clear();
                adapter.notifyDataSetChanged();
            }
        });

        final Button syncDevices = root.findViewById(R.id.syncDevices);
        syncDevices.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("button", "sync devices pressed");
                new SyncDevicesToArgon().execute();
            }
        });

        return root;
    }

    public void addItems(View v, SaveManager.Device device) {
        if(device.type.equals("Light sensor")){
            listItems.add(device.name + " - " + device.type + " - PIN" + device.pin + " Attached light: " + device.attachedLED);
        } else if(device.type.equals("Range finder")){
            listItems.add(device.name + " - " + device.type + " - PIN" + device.pin + " Trigger pin: " + device.triggerPin);
        } else{
            listItems.add(device.name + " - " + device.type + " - PIN" + device.pin);
        }
        adapter.notifyDataSetChanged();
    }

    private final class SendDeviceToCLoud extends AsyncTask<Integer, Integer, String> {
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
                List<String> someList = new ArrayList<String>();
                someList.add(SaveManager.devices.get(params[0]-1).type);
                someList.add(SaveManager.devices.get(params[0]-1).pin);
                someList.add(SaveManager.devices.get(params[0]-1).status);
                someList.add(SaveManager.devices.get(params[0]-1).attachedLED);
                someList.add(SaveManager.devices.get(params[0]-1).triggerPin);
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

    private final class SyncDevicesToArgon extends AsyncTask<Integer, Integer, String> {
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

    private final class DeleteDevicesOnArgon extends AsyncTask<Integer, Integer, String> {
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
                List<String> someList = new ArrayList<String>();
                particleDevice.callFunction("recieveDeleteDevices", someList);
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