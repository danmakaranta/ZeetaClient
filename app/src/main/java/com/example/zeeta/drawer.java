package com.example.zeeta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class drawer extends AppCompatActivity {
    private static final String TAG = "RequestPage";
    public ArrayList<String> selectedServices;//
    public ArrayList<CheckBox> selectedCheckboxes;
    Button requestbtn;

    @Override
    protected void onResume() {
        super.onResume();
        selectedCheckboxes.clear();
        selectedServices.clear();
        Log.d("tesst", "OnResume: testing");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        selectedServices = new ArrayList<String>();
        selectedCheckboxes = new ArrayList<CheckBox>();
        selectedCheckboxes.clear();
        selectedServices.clear();
        requestbtn = (Button) findViewById(R.id.request_btn);

        requestbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedServices != null) {
                    requestService();
                } else {
                    Toast.makeText(drawer.this, "You have not selected any service", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onCheckboxClicked(View view) {

        String temp = ((CheckBox) view).getText().toString();
        CheckBox taxi = findViewById(R.id.taxi);// for taxi's
        CheckBox trycycle = findViewById(R.id.trycycle);
        CheckBox tempCb = (CheckBox) view;// for getting the selected checkbox

        if (temp.equals("Taxi") && !selectedServices.contains(temp) && !selectedServices.contains("Trycycle(Keke)")) {

            selectedServices.clear();
            if (selectedCheckboxes != null) {
                for (int i = 0; i <= selectedCheckboxes.size() - 1; i++) {
                    selectedCheckboxes.get(i).setChecked(false);
                }
            }
            selectedServices.add("Taxi");
            ((CheckBox) view).setChecked(false);
            CheckBox taxi2 = findViewById(R.id.taxi);
            taxi2.setChecked(true);
        } else if (temp.equals("Taxi") && trycycle.isChecked()) {
            ((CheckBox) view).setChecked(false);
            taxi.setChecked(false);
            selectedServices.remove("Taxi");

        } else if (temp.equals("Taxi") && selectedServices.contains("Taxi")) {
            selectedServices.remove("Taxi");

            taxi.setChecked(false);
        } else if (!temp.equals("Taxi") && selectedServices.contains("Taxi")) {
            Toast.makeText(drawer.this, "You can't combine Taxi service with other services", Toast.LENGTH_SHORT).show();
            ((CheckBox) view).setChecked(false);
            taxi.setChecked(true);
        } else if (temp.equals("Trycycle(Keke)") && !selectedServices.contains(temp)) {

            selectedServices.clear();
            if (selectedCheckboxes != null) {
                for (int i = 0; i <= selectedCheckboxes.size() - 1; i++) {
                    selectedCheckboxes.get(i).setChecked(false);
                }
            }
            selectedServices.add("Trycycle(Keke)");
            ((CheckBox) view).setChecked(false);

            trycycle.setChecked(true);
        } else if (temp.equals("Trycycle(Keke)") && selectedServices.contains("Trycycle(Keke)")) {
            selectedServices.remove("Trycycle(Keke)");

            trycycle.setChecked(false);
        } else if (!temp.equals("Trycycle(Keke)") && selectedServices.contains("Trycycle(Keke)")) {
            Toast.makeText(drawer.this, "You can't combine Trycycle(Keke) service with other services", Toast.LENGTH_SHORT).show();
            ((CheckBox) view).setChecked(false);

            trycycle.setChecked(true);
        } else {
            taxi.setChecked(false);
            trycycle.setChecked(false);
            selectedCheckboxes.add(tempCb);
            if (selectedServices.contains(temp) && selectedServices != null) {
                selectedServices.remove(temp);
            } else {
                selectedServices.add(temp);
            }
        }

    }

    public void requestService() {

        //setup the intent for the map page passing all the selected services into the activity
        Intent newIntent = new Intent(this, MapActivity.class);

        if (selectedServices.size() <= 0) {
            Toast.makeText(drawer.this, "You have not selected any service", Toast.LENGTH_SHORT).show();
        } else {

            newIntent.putExtra("RequestedServices", selectedServices);
            startActivity(newIntent);
        }

    }

}