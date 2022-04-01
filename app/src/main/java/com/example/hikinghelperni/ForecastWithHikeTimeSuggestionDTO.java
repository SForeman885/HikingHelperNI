package com.example.hikinghelperni;

import lombok.Value;

@Value
public class ForecastWithHikeTimeSuggestionDTO {

    ForecastObject suggestionForecast;
    TrailHikeTimeSuggestionDTO hikeTimeSuggestion;
}
