package com.example.smarthomecoursework.ui.settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.smarthomecoursework.R;

public class SettingsFragment extends Fragment {

    private EditText intervalET;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        intervalET = (EditText) root.findViewById(R.id.intervalET);

        intervalET.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                Log.i("interval changed", " " + intervalET.getText());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        return root;
    }
}