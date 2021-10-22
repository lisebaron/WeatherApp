package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private JSONObject Weather = null;
    private JSONObject Day = null;
    private JSONArray forecastDaily = null;
    private JSONArray forecastHourly = null;
    private String city = "creteil";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvCity = findViewById(R.id.city);
        TextView tvTemp = findViewById(R.id.temp);
        TextView tvTempDesc = findViewById(R.id.tempDesc);
        TextView tvPrevision = findViewById(R.id.forcastText);
        TextView tvHumidity = findViewById(R.id.humidity);
        TextView tvHumidityValue = findViewById(R.id.humidityValue);
        TextView tvPressure = findViewById(R.id.pressure);
        TextView tvPressureValue = findViewById(R.id.pressureValue);
        TextView tvWindDirection = findViewById(R.id.windDirection);
        TextView tvWindValue = findViewById(R.id.windValue);
        ImageView ivWeatherIcon = findViewById(R.id.weatherIcon);
        LinearLayout LLDailyForecast = findViewById(R.id.dailyForecast);
        LinearLayout LLHourlyForecast = findViewById(R.id.hourlyForecast);
        Button goToSavedCity = findViewById(R.id.go_to_saved_city_btn);

        goToSavedCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SavedCityListActivity.class));
            }
        });

        SharedPreferences prefs = getSharedPreferences("SAVED_CITY_LIST", MODE_PRIVATE);
        city = prefs.getString("cityName", "creteil");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    ApiManager apiManager = new ApiManager();
                    String getResponse = null;
                    Bitmap bitmap = null;
                    double lon;
                    double lat;
                    JSONObject max_min = null;
                    ArrayList<Bitmap> forcastHourlyIcons = new ArrayList<Bitmap>();
                    ArrayList<Bitmap> forcastDailyIcons = new ArrayList<Bitmap>();

                    try {
                        getResponse = apiManager.doGetRequest("https://api.openweathermap.org/data/2.5/weather?q="+ city +"&lang=fr&appid=0f52941411f16c948832d46ec4c8ef6d");
                        Weather = (new JSONObject(getResponse));
                        lon = getWeather().getJSONObject("coord").getDouble("lon");
                        lat = getWeather().getJSONObject("coord").getDouble("lat");

                        getResponse = apiManager.doGetRequest("https://api.openweathermap.org/data/2.5/onecall?lat="+ lat +"&lon="+ lon +"&lang=fr&exclude=minutely&appid=0c77ff9e35a8448a81904dcd2a83a99d");
                        setDay(new JSONObject(getResponse).getJSONObject("current"));
                        setForecastDaily(new JSONObject(getResponse).getJSONArray("daily"));
                        setForecastHourly(new JSONObject(getResponse).getJSONArray("hourly"));

                        getResponse = apiManager.doGetRequest("https://api.openweathermap.org/data/2.5/forecast?q="+ city +"&appid=0c77ff9e35a8448a81904dcd2a83a99d");
                        max_min = getMaxMin(new JSONObject(getResponse).getJSONArray("list"));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    int tempCelsius = (int) (getWeather().getJSONObject("main").getInt("temp") - 273.15);
                    int tempFah = (int) ((getWeather().getJSONObject("main").getInt("temp")) - 273.15) * 9/5 + 32;
                    int temp_min = (int) (max_min.getInt("min") - 273.15);
                    int temp_max = (int) (max_min.getInt("max") - 273.15);
                    double windSpeed = (double) (getWeather().getJSONObject("wind").getDouble("speed") * 3.6);
                    String windDirection = getWindDirection(getWeather().getJSONObject("wind").getDouble("deg"));
                    String city = getWeather().getString("name");
                    String humidity = getWeather().getJSONObject("main").getString("humidity");
                    String pressure = getWeather().getJSONObject("main").getString("pressure");
                    String weather = getWeather().getJSONArray("weather").getJSONObject(0).getString("description");
                    weather = weather.substring(0, 1).toUpperCase() + weather.substring(1);

                    try ( InputStream is = new URL( "https://openweathermap.org/img/wn/"+ getWeather().getJSONArray("weather").getJSONObject(0).getString("icon") +"@2x.png" ).openStream() ) {
                        bitmap = BitmapFactory.decodeStream( is );
                    }

                    for(int i = 0; i < 24; i++) {
                        try {
                            String icon = getForecastHourly().getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("icon");
                            try ( InputStream is = new URL( "https://openweathermap.org/img/wn/"+ icon +"@2x.png" ).openStream() ) {
                                forcastHourlyIcons.add(BitmapFactory.decodeStream( is ));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    for(int i = 0; i < 6; i++) {
                        try {
                            String icon = getForecastDaily().getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("icon");
                            try ( InputStream is = new URL( "https://openweathermap.org/img/wn/"+ icon +"@2x.png" ).openStream() ) {
                                forcastDailyIcons.add(BitmapFactory.decodeStream( is ));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    Bitmap finalBitmap = bitmap;
                    String finalWeather = weather;

                    runOnUiThread(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.R)
                        @Override
                        public void run() {
                            setTextViewText(city, tvCity);
                            setTextViewText(tempCelsius + " °C / " + tempFah + " °F", tvTemp);
                            setTextViewText(finalWeather + "    " + temp_max + " / " + temp_min + " °C", tvTempDesc);
                            ivWeatherIcon.setImageBitmap(finalBitmap);
                            setTextViewText("Prévision", tvPrevision);
                            setTextViewText("Humidité", tvHumidity);
                            setTextViewText(humidity + "%", tvHumidityValue);
                            setTextViewText("Pression", tvPressure);
                            setTextViewText(pressure + " hPa", tvPressureValue);
                            setTextViewText(String.format("%.2f", windSpeed) + " km/h", tvWindValue);
                            setTextViewText(windDirection, tvWindDirection);

                            for(int i = 0; i < 24; i++) {
                                try {
                                    addHourlyForecast(LLHourlyForecast, getForecastHourly().getJSONObject(i),forcastHourlyIcons.get(i));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            for(int i = 1; i < 6; i++) {
                                try {
                                    addDailyForecast(LLDailyForecast, getForecastDaily().getJSONObject(i),forcastDailyIcons.get(i));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meteo,menu);

        MenuItem menuItem = menu.findItem(R.id.go_to_search);
        Button button = (Button) menuItem.getActionView();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private String getWindDirection(double degree){
        if (degree>337.5) return "Vent du nord";
        if (degree>292.5) return "Vent du nord-ouest";
        if(degree>247.5) return "Vent de l'ouest";
        if(degree>202.5) return "Vent du sud-ouest";
        if(degree>157.5) return "Vent du sud";
        if(degree>122.5) return "Vent du sud-est";
        if(degree>67.5) return "Vent de l'est";
        if(degree>22.5) return "Vent du nord-est";
        return "Vent du Nord";
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void addHourlyForecast(LinearLayout ll, JSONObject forecast, Bitmap bm) {
        LinearLayout newLL = new LinearLayout(this);
        newLL.setOrientation(LinearLayout.VERTICAL);
        newLL.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        newLL.setPadding(0,0,20,0);

        Date date = null;
        String HourStr = null;
        try {
            date = new Date((forecast.getInt("dt") + getWeather().getInt("timezone")) * 1000L);
            HourStr = new SimpleDateFormat("h", Locale.FRANCE).format(date);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView tvHour = new TextView(this);
        tvHour.setText(String.format("%s:00", HourStr));
        tvHour.setGravity(Gravity.CENTER);
        tvHour.setTextSize(15);
        newLL.addView(tvHour);

        //Imageview
        ImageView ivForecastIcon = new ImageView(this);
        ivForecastIcon.setImageBitmap(bm);
        int size = getDisplay().getWidth() / 5 - 42;
        int width = size;
        int height = size;
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width, height);
        ivForecastIcon.setLayoutParams(parms);
        newLL.addView(ivForecastIcon);

        int tempCelsius = 0;
        try {
            tempCelsius = (int) (forecast.getInt("temp") - 273.15);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView tvForecastTemp = new TextView(this);
        tvForecastTemp.setText(String.format("%s °C", tempCelsius));
        tvForecastTemp.setGravity(Gravity.CENTER);
        tvForecastTemp.setTextSize(15);
        newLL.addView(tvForecastTemp);

        ll.addView(newLL);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void addDailyForecast(LinearLayout ll, JSONObject forecast, Bitmap bm) {

        LinearLayout newLL = new LinearLayout(this);
        newLL.setOrientation(LinearLayout.VERTICAL);
        newLL.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        newLL.setPadding(0,0,20,0);

        Date date = null;
        String DayStr = null;
        String MonthStr = null;
        String DayMonth = null;
        try {
            date = new Date((forecast.getInt("dt") + getWeather().getInt("timezone")) * 1000L);
            DayStr = new SimpleDateFormat("EE", Locale.FRANCE).format(date);
            DayStr = DayStr.substring(0, 1).toUpperCase() + DayStr.substring(1);
            MonthStr = new SimpleDateFormat("MMM", Locale.FRANCE).format(date);
            MonthStr = MonthStr.substring(0, 1).toUpperCase() + MonthStr.substring(1);
            DayMonth = new SimpleDateFormat("d", Locale.FRANCE).format(date);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView tvDay = new TextView(this);
        tvDay.setText(DayStr);
        tvDay.setGravity(Gravity.CENTER);
        tvDay.setTextSize(15);
        newLL.addView(tvDay);

        TextView tvMonth = new TextView(this);
        tvMonth.setText(String.format("%s %s", MonthStr, DayMonth));
        tvMonth.setGravity(Gravity.CENTER);
        tvMonth.setTextSize(15);
        newLL.addView(tvMonth);

        //Imageview
        ImageView ivForecastIcon = new ImageView(this);
        ivForecastIcon.setImageBitmap(bm);
        int size = getDisplay().getWidth() / 5 - 42;
        int width = size;
        int height = size;
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width, height);
        ivForecastIcon.setLayoutParams(parms);
        newLL.addView(ivForecastIcon);

        String weather = null;
        try {
            weather = forecast.getJSONArray("weather").getJSONObject(0).getString("description");
            weather = weather.substring(0, 1).toUpperCase() + weather.substring(1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TextView tvWeather = new TextView(this);
        tvWeather.setText(weather);
        tvWeather.setGravity(Gravity.CENTER);
        tvWeather.setTextSize(15);
        tvWeather.setFocusable(true);
        tvWeather.setFocusableInTouchMode(true);
        tvWeather.setSingleLine(true);
        tvWeather.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tvWeather.setMarqueeRepeatLimit(-1);
        tvWeather.setSelected(true);
        newLL.addView(tvWeather);

        ll.addView(newLL);
    }


        private JSONObject getMaxMin(JSONArray forecast) {
        JSONObject max_min = new JSONObject();

        try {
            for (int i = 0; i < forecast.length(); i++) {
                double tmpMax = forecast.getJSONObject(i).getJSONObject("main").getDouble("temp_max");
                double tmpMin = forecast.getJSONObject(i).getJSONObject("main").getDouble("temp_min");

                if(!max_min.has("max") || max_min.getDouble("max") < tmpMax)
                    max_min.put("max", tmpMax);

                if(!max_min.has("min") || max_min.getDouble("min") > tmpMin)
                    max_min.put("min", tmpMin);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return max_min;
    }

    private void StartActivity(Class activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    public void setTextViewText(String str, TextView tv) {
        tv.setText(str);
    }

    public JSONObject getWeather() {
        return Weather;
    }

    public void setWeather(JSONObject weather) {
        this.Weather = weather;
    }

    public JSONObject getDay() {
        return Day;
    }

    public void setDay(JSONObject day) {
        this.Day = day;
    }

    public JSONArray getForecastDaily() {
        return forecastDaily;
    }

    public void setForecastDaily(JSONArray forecastDaily) {
        this.forecastDaily = forecastDaily;
    }

    public JSONArray getForecastHourly() {
        return forecastHourly;
    }

    public void setForecastHourly(JSONArray forecastHourly) {
        this.forecastHourly = forecastHourly;
    }
}