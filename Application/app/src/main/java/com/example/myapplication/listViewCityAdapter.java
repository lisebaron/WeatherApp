package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class listViewCityAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> cityList = new ArrayList<String>();

    public listViewCityAdapter(Context context, ArrayList<String> cityList) {
        this.context = context;
        this.cityList = cityList;
    }

    @Override
    public int getCount() {
        return cityList.size();
    }

    @Override
    public Object getItem(int i) {
        return cityList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.city_list_view_row, viewGroup, false);
        }

        String city = (String) getItem(i);

        TextView cityNameListLabel = view.findViewById(R.id.cityNameListLabel);
        FloatingActionButton removeCityButton = view.findViewById(R.id.removeCity_btn);

        cityNameListLabel.setText(city);
        removeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        return view;
    }
}
