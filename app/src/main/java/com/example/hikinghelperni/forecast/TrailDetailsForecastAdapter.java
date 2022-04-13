package com.example.hikinghelperni.forecast;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.hikinghelperni.R;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class TrailDetailsForecastAdapter extends RecyclerView.Adapter<TrailDetailsForecastAdapter.ViewHolder> {

    private List<ForecastObject> mDaysForecasted;

    public TrailDetailsForecastAdapter(List<ForecastObject> daysForecasted) {mDaysForecasted = daysForecasted;}

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView dayTextView, temperatureTextView, descriptionTextView;
        public ImageView iconImageView;

        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            dayTextView = itemView.findViewById(R.id.forecast_day_name);
            temperatureTextView = itemView.findViewById(R.id.forecast_temperature);
            descriptionTextView = itemView.findViewById(R.id.forecast_description);
            iconImageView = itemView.findViewById(R.id.day_forecast_icon_image);
        }
    }

    @Override
    public TrailDetailsForecastAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View forecastView = inflater.inflate(R.layout.forecast_day_item, parent, false);
        return new ViewHolder(forecastView);
    }

    // This populates data into each item through holder
    @Override
    public void onBindViewHolder(TrailDetailsForecastAdapter.ViewHolder holder, int position) {
        ForecastObject dayForecast = mDaysForecasted.get(position);
        DayOfWeek dayForecasted = LocalDateTime.ofEpochSecond(dayForecast.getDt(), 0, ZoneOffset.UTC).getDayOfWeek();

        TextView dayTextView = holder.dayTextView;
        dayTextView.setText(dayForecasted.getDisplayName(TextStyle.FULL, Locale.UK));
        TextView descriptionTextView = holder.descriptionTextView;
        descriptionTextView.setText(dayForecast.getWeather().get(0).getDescription());
        ImageView iconView = holder.iconImageView;
        String iconURI = String.format("%s:drawable/weather_icon_%s", holder.itemView.getContext().getPackageName(), dayForecast.getWeather().get(0).getIcon());
        int imageResource = holder.itemView.getContext().getResources().getIdentifier(iconURI, null, null);
        iconView.setImageDrawable(holder.itemView.getContext().getResources().getDrawable(imageResource, null));
        TextView temperatureTextView = holder.temperatureTextView;
        temperatureTextView.setText(String.format("%s°C", dayForecast.getTemp().getDay()));
    }

    @Override
    public int getItemCount() {
        return mDaysForecasted.size();
    }
}
