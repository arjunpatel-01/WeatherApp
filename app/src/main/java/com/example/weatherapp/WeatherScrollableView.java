package com.example.weatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 */
public class WeatherScrollableView extends RecyclerView.Adapter<WeatherScrollableView.ViewHolder> {

    private final Context context;
    private final ArrayList<WeatherScrollableDetails> WeatherScrollableDetailsArrayList;

    /**
     * Constructor
     *
     * @param context MainActivity context
     * @param WeatherScrollableDetailsArrayList arraylist that has the hourly/daily weather data accumulated
     */
    public WeatherScrollableView(Context context, ArrayList<WeatherScrollableDetails> WeatherScrollableDetailsArrayList) {
        this.context = context;
        this.WeatherScrollableDetailsArrayList = WeatherScrollableDetailsArrayList;
    }

    /**
     * Puts data into the recycler view (he scrollable feature)
     *
     * @return ViewHolder
     */
    @NonNull
    @Override
    public WeatherScrollableView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_card, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Sets the data into the card (holder)
     * Loads temperature string and precipitation string
     * Uses Picasso to grab image data from url
     * Converts unix time to standard time based on location's timezone and time mode and loads it
     *
     * @param holder the Holder type
     * @param position an index into the arraylist
     */
    @Override
    public void onBindViewHolder(@NonNull WeatherScrollableView.ViewHolder holder, int position) {

        WeatherScrollableDetails detail = WeatherScrollableDetailsArrayList.get(position);

        holder.Temperature.setText(detail.getTemperature());
        holder.Precipitation.setText(detail.getPrecipitation());

        Picasso.get().load(detail.getIcon()).into(holder.Condition);

        Date date_unix = new Date((detail.getTime()+detail.getTimeZoneOffset())*1000);
        SimpleDateFormat output;
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        if (detail.getTimeMode() == 0) {
            output = new SimpleDateFormat("h:mm aa");
        } else {
            output = new SimpleDateFormat("EEE, MMM d");
        }
        output.setTimeZone(timeZone);
        holder.Time.setText(output.format(date_unix));
    }

    /**
     * Gets the total size of the arraylist
     *
     * @return ArrayList size
     */
    @Override
    public int getItemCount() {
        return WeatherScrollableDetailsArrayList.size();
    }

    /**
     * Class the find the id of each necessary object from the weather_card.xml
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView Precipitation;
        private final TextView Temperature;
        private final TextView Time;
        private final ImageView Condition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Precipitation = itemView.findViewById(R.id.Precipitation);
            Temperature = itemView.findViewById(R.id.idTVTemperature);
            Time = itemView.findViewById(R.id.Time);
            Condition = itemView.findViewById(R.id.Condition);
        }
    }
}
