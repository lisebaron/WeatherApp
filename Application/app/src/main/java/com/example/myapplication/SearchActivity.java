package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLOutput;
import java.util.ArrayList;


public class SearchActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> city = new ArrayList<>();
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        prefs = getSharedPreferences("SAVED_CITY_LIST", MODE_PRIVATE);

        listView = findViewById(R.id.listView);
        getJsonFromAssets();

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,city);
        listView.setAdapter(arrayAdapter);

    }

    public void selectCity(View view) {
        TextView tview = (TextView) view;
        String name = (String) tview.getText();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("cityName", name);
        editor.apply();

        System.out.println("coucou");
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }
    public void getJsonFromAssets() {
        String json;
        try {
            InputStream is = getAssets().open("city.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i<jsonArray.length();i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                city.add(obj.getString("name"));
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String name = s;

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("cityName", name);
                editor.apply();

                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                arrayAdapter.getFilter().filter(s);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}