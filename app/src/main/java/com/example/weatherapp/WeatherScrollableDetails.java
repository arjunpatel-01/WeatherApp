package com.example.weatherapp;

/**
 * class that holds the data for the hourly or daily weather
 * used for more convenient scrollable feature creation
 */
public class WeatherScrollableDetails {

    private final long time;
    private final String temperature;
    private final String icon;
    private final String precipitation;
    private final long timeZoneOffset;
    private final int timeMode;

    /**
     * WeatherScrollableDetails constructor that sets input values to corresponding private variables
     *
     * @param time time in unix UTC
     * @param temperature temperature recorded as a string
     * @param icon condition icon url
     * @param precipitation percent of precipitation as a string
     * @param timeZoneOffset timezone offset from GMT in seconds
     * @param timeMode flag that determines if the data is regarding hourly or daily weather
     */
    public WeatherScrollableDetails(long time, String temperature, String icon, String precipitation, long timeZoneOffset, int timeMode) {
        this.time = time;
        this.temperature = temperature;
        this.icon = icon;
        this.precipitation = precipitation;
        this.timeZoneOffset = timeZoneOffset;
        this.timeMode = timeMode;
    }

    public long getTime() {
        return time;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getIcon() {
        return icon;
    }

    public String getPrecipitation() {
        return precipitation;
    }

    public long getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public int getTimeMode() {
        return timeMode;
    }
}
