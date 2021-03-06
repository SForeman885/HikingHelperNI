package com.example.hikinghelperni.forecast;

import java.util.List;

import lombok.Value;

@Value
public class ForecastApiResponse {

    List<ForecastObject> daily;

    @Override
    public String toString() {
        return "ForecastApiResponse [data=" + daily + "]";
    }
}
