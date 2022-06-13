package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    //creating objects based on the tags I created in the activity_main.xml
    private TextInputEditText EditCityField;
    private TextView CityNameTitle;
    private TextView CurrentTemperature;
    private TextView WindSpeed;
    private TextView Pressure;
    private TextView Humidity;
    private RelativeLayout HomePage;
    private ImageView ConditionImage;

    private ArrayList<WeatherScrollableDetails> HourlyWeather;
    private ArrayList<WeatherScrollableDetails> DailyWeather;

    private WeatherScrollableView WeatherHourlyScrollView;
    private WeatherScrollableView WeatherDailyScrollView;

    private long timezone_offset;
    private final int HOURMODE = 0;
    private final int DAYMODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        //get the id from the activity_main.xml and assign it to the corresponding objects
        setContentView(R.layout.activity_main);
        EditCityField = findViewById(R.id.EditCityField);
        CityNameTitle = findViewById(R.id.CityNameTitle);
        CurrentTemperature = findViewById(R.id.CurrentTemperature);
        WindSpeed = findViewById(R.id.WindSpeed);
        Pressure = findViewById(R.id.Pressure);
        Humidity = findViewById(R.id.Humidity);
        HomePage = findViewById(R.id.HomePage);
        ConditionImage = findViewById(R.id.ConditionImage);

        //creating a recyclerview for the scrollable design for the hourly and daily weather
        RecyclerView weatherHourlyScrollable = findViewById(R.id.WeatherHourlyScrollable);
        HourlyWeather = new ArrayList<>();
        WeatherHourlyScrollView = new WeatherScrollableView(this, HourlyWeather);
        weatherHourlyScrollable.setAdapter(WeatherHourlyScrollView);

        RecyclerView weatherDailyScrollable = findViewById(R.id.WeatherDailyScrollable);
        DailyWeather = new ArrayList<>();
        WeatherDailyScrollView = new WeatherScrollableView(this, DailyWeather);
        weatherDailyScrollable.setAdapter(WeatherDailyScrollView);

        //found a background gradient on unsplash to use for my app
        ImageView BackgroundImage = findViewById(R.id.BackgroundImage);
        String backgroundPath = "https://images.unsplash.com/photo-1557682233-43e671455dfa?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=882&q=80";
        Picasso.get().load(backgroundPath).into(BackgroundImage);

        //boots up the app with austin as the first input to fetch its data
        String city_name = "austin";
        try {
            getWeatherData(city_name);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        //makes the search icon clickable and grabs weather data from whatever city is inputted in the text field
        ImageView SearchIcon = findViewById(R.id.SearchIcon);
        SearchIcon.setOnClickListener(view -> {
            String city = EditCityField.getText().toString();
            if (city.isEmpty()){
                Toast.makeText(MainActivity.this, "City field cannot be empty", Toast.LENGTH_SHORT).show();
            }else{
                try {
                    getWeatherData(city);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        EditCityField.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String city = EditCityField.getText().toString();
                if (city.isEmpty()){
                    Toast.makeText(MainActivity.this, "City field cannot be empty", Toast.LENGTH_SHORT).show();
                }else{
                    try {
                        getWeatherData(city);
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
            return false;
        });
    }

    /**
     * grabs weather data from openweathermap by making an api call to obtain coordinates
     * and an api call to obtain weather data at the location marked by the coordinates
     * @param cityName user-inputted String that indicates which city's weather should be displayed
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void getWeatherData (String cityName) throws ExecutionException, InterruptedException {

        //creates a volley queue to process the string requests
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        //uses the input city (city name) to build api url
        String geo_url = "https://api.openweathermap.org/geo/1.0/direct?appid=f79799ec1cafaac7263de4eb24618fb6" + "&q=" + cityName + "&limit=1";

        //updates the longitude and latitude globals then performs the onecall api call
        StringRequest coordinateFind = new StringRequest(Request.Method.GET, geo_url, geoResponse -> {
            try {
                JSONArray jsonResponse = new JSONArray(geoResponse);
                JSONObject jsonChooseLocation = jsonResponse.getJSONObject(0);
                String name = jsonChooseLocation.getString("name");
                String country = jsonChooseLocation.getString("country");
                String state = "";
                if ("US".equals(country)) {
                    state = jsonChooseLocation.getString("state");
                    name = name + ", " + state;
                }
                name = name + ", " + country;
                CityNameTitle.setText(name);
                double lat = jsonChooseLocation.getDouble("lat");
                double lon = jsonChooseLocation.getDouble("lon");
                String onecall_url = "https://api.openweathermap.org/data/2.5/onecall?appid=f79799ec1cafaac7263de4eb24618fb6"
                        + "&lat=" + lat
                        + "&lon=" + lon
                        + "&exclude=minutely,alerts"
                        + "&units=imperial";

                StringRequest weatherFind = new StringRequest(Request.Method.GET, onecall_url, weatherResponse -> {
                    HomePage.setVisibility(View.VISIBLE);
                    HourlyWeather.clear();
                    DailyWeather.clear();

                    try {
                        //grabs a JSON object from the response
                        JSONObject jsonResponse2 = new JSONObject(weatherResponse);
                        DecimalFormat df = new DecimalFormat("###.#");

                        //grabs the timezone offset from the JSON object (since the time is given in unix UTC
                        timezone_offset = jsonResponse2.getLong("timezone_offset");

                        /*
                         * gets current weather data
                         * (i.e., temperature, humidity, wind speed, pressure, and condition image)
                         */
                        JSONObject jsonCurrent = jsonResponse2.getJSONObject("current");
                        int ctemp = (int) jsonCurrent.getDouble("temp");
                        String current_temp = String.valueOf(ctemp);
                        CurrentTemperature.setText(current_temp + "°F");

                        double chumidity = jsonCurrent.getDouble("humidity");
                        String current_humidity = String.valueOf(chumidity);
                        Humidity.setText("Humidity: " + current_humidity + "%");

                        double cwindspeed = jsonCurrent.getDouble("wind_speed");
                        String current_windspeed = String.valueOf(cwindspeed);
                        WindSpeed.setText("Wind: " + current_windspeed + " mph");

                        double cpressure = jsonCurrent.getDouble("pressure");
                        String current_pressure = String.valueOf(cpressure);
                        Pressure.setText("Pressure: " + current_pressure + " hPa");

                        String conditionIconCode = jsonCurrent.getJSONArray("weather").getJSONObject(0).getString("icon");
                        String conditionIconUrl = "https://openweathermap.org/img/wn/" + conditionIconCode + "@2x.png";
                        Picasso.get().load(conditionIconUrl).into(ConditionImage);

                        /*
                         * gets hourly weather data (24 hours)
                         * (i.e., time, temperature, percent of precipitation, and condition icon)
                         */
                        JSONArray jsonHourly = jsonResponse2.getJSONArray("hourly");
                        for (int i = 1; i < 24; i++){
                            JSONObject jsonHourData = jsonHourly.getJSONObject(i);
                            long hourly_time = jsonHourData.getLong("dt");  //removed timeoffset
                            int htemp = (int) jsonHourData.getDouble("temp");
                            double hprecip = jsonHourData.getDouble("pop");

                            String hourly_temp = String.valueOf(htemp);
                            hourly_temp += " °F";
                            String himgcode = jsonHourData.getJSONArray("weather").getJSONObject(0).getString("icon");
                            String hourly_image = "https://openweathermap.org/img/wn/" + himgcode + "@2x.png";
                            String hourly_precip = df.format(hprecip*100) + "% rain";

                            //adds a scrollable view card for each hour data
                            HourlyWeather.add(new WeatherScrollableDetails(hourly_time, hourly_temp, hourly_image, hourly_precip, timezone_offset, HOURMODE));
                        }
                        WeatherHourlyScrollView.notifyDataSetChanged();

                        /*
                         * gets daily weather data (7 days)
                         * (i.e., day, high and low temperature, percent of precipitation, and conition icon)
                         */
                        JSONArray jsonDaily = jsonResponse2.getJSONArray("daily");
                        for (int i = 1; i < jsonDaily.length(); i++){
                            JSONObject jsonDayData = jsonDaily.getJSONObject(i);
                            long daily_time = jsonDayData.getLong("dt");
                            int dlotemp = (int) jsonDayData.getJSONObject("temp").getDouble("min");
                            int dhitemp = (int) jsonDayData.getJSONObject("temp").getDouble("max");
                            double dprecip = jsonDayData.getDouble("pop");

                            String daily_lo_temp = String.valueOf(dlotemp);
                            String daily_hi_temp = String.valueOf(dhitemp);
                            String daily_temps = daily_hi_temp + "↑ " + daily_lo_temp + "↓" + " °F";
                            String dimgcode = jsonDayData.getJSONArray("weather").getJSONObject(0).getString("icon");
                            String daily_image = "https://openweathermap.org/img/wn/" + dimgcode + "@2x.png";
                            String daily_precip = df.format(dprecip*100) + "% rain";

                            //adds scrollable view card for each hour data
                            DailyWeather.add(new WeatherScrollableDetails(daily_time, daily_temps, daily_image, daily_precip, timezone_offset, DAYMODE));
                        }
                        WeatherDailyScrollView.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }, error -> Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show());
                requestQueue.add(weatherFind);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Toast.makeText(MainActivity.this, error.toString().trim(), Toast.LENGTH_SHORT).show());

        requestQueue.add(coordinateFind);
    }
}