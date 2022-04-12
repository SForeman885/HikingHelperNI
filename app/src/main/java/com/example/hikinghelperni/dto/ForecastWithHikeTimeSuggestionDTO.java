package com.example.hikinghelperni.dto;

import com.example.hikinghelperni.dto.TrailHikeTimeSuggestionDTO;
import com.example.hikinghelperni.forecast.ForecastObject;

import lombok.Value;

@Value
public class ForecastWithHikeTimeSuggestionDTO {

    ForecastObject suggestionForecast;
    TrailHikeTimeSuggestionDTO hikeTimeSuggestion;
}
