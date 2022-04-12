package com.example.hikinghelperni;

import com.example.hikinghelperni.forecast.ForecastObject;

import lombok.Value;

@Value
public class ForecastWithHikeTimeSuggestionDTO {

    ForecastObject suggestionForecast;
    TrailHikeTimeSuggestionDTO hikeTimeSuggestion;
}
