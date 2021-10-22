package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SavedCityListActivity extends AppCompatActivity {
    private EditText cityNameInput;
    private SharedPreferences prefs;
    private String cityname = "";
    private ArrayList<String> cities = new ArrayList<String>();

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_city_list);
        LinearLayout ll = findViewById(R.id.cityList);

        prefs = getSharedPreferences("SAVED_CITY_LIST", MODE_PRIVATE);
        String currentName = (prefs.getString("cityName", ""));
        String currentList = (prefs.getString("cityList", ""));

        if (!currentName.equals(currentList) && !currentList.equals("")) {
            String[] test = currentList.substring(1, currentList.length() - 1).split(", ");
            Collections.addAll(cities, test);
        } else {
            cities.add(currentList);
        }

        System.out.println("Current : " + cities);
        cities.forEach((city) -> addCityLine(ll, city));
        cityNameInput = (EditText) findViewById(R.id.cityNameInput);
    }

    public void saveData(View view) {
        String currentList = prefs.getString("cityList", "");
        String newCityName = cityNameInput.getText().toString();
        System.out.println(currentList);

        SharedPreferences.Editor editor = prefs.edit();
        if (currentList.equals("")) {
            editor.putString("cityList", newCityName);
            editor.apply();
            setCityname(cityNameInput.getText().toString());
            selectCity(view);
        } else {
            if (!newCityName.equals("")) {
                if(!cities.contains(newCityName)) {
                    cities.add(newCityName);
                    currentList = cities.toString();
                    editor.putString("cityList", currentList);
                    editor.apply();
                    setCityname(cityNameInput.getText().toString());
                    selectCity(view);
                }
            }
        }
    }

    private void addCityLine(LinearLayout ll, String cityName) {
        System.out.println("hello " + cityName);
        TextView tvCityName = new TextView(this);
        tvCityName.setHeight(100);
        tvCityName.setText(cityName);
        tvCityName.setGravity(Gravity.CENTER_VERTICAL);
        tvCityName.setTextSize(20);
        tvCityName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCity(view);
            }
        });
        ll.addView(tvCityName);
    }

    public void selectCity(View view) {
        String name = getCityname();
        if(getCityname().equals("")) {
            TextView tview = (TextView) view;
            name = (String) tview.getText();
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("cityName", name);
        editor.apply();

        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    public String getCityname() {
        return cityname;
    }

    public void setCityname(String cityname) {
        this.cityname = cityname;
    }
}