package com.example.hikinghelperni.forecast;

import java.util.List;

import lombok.Value;

@Value
public class ForecastObject {

    Long dt;
    Long sunrise;
    Long sunset;
    TemperatureObject temp;
    Double wind_speed;
    List<WeatherObject> weather;

    @Override
    public String toString() {
        return "Forecast [date=" + dt + ", "
                + "sunrise time=" + sunrise + ", "
                + "sunset time=" + sunset + ", "
                + "temperature=" + temp.getDay() + ", "
                + "wind speed=" + wind_speed + ", "
                + "weather=" + weather.get(0).getMain() + ", "
                + "weather description=" + weather.get(0).getDescription() + ", "
                + "icon=" + weather.get(0).getIcon() + "]";
    }
}
