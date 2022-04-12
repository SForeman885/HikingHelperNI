package com.example.hikinghelperni;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface ForecastService {

    @GET("data/2.5/onecall")
    Call<ForecastApiResponse> getForecast(@QueryMap Map<String, String> parameters);
}
