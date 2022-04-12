package com.example.hikinghelperni.services;

import com.example.hikinghelperni.dto.ForecastWithHikeTimeSuggestionDTO;
import com.example.hikinghelperni.dto.TrailDetailsDTO;
import com.example.hikinghelperni.dto.TrailHikeTimeSuggestionDTO;
import com.example.hikinghelperni.forecast.ForecastApiResponse;
import com.example.hikinghelperni.forecast.ForecastObject;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TrailTimeEstimationService {

    private List<String> weatherOrderOfPreference = Arrays.asList("Clear", "Clouds", "Drizzle",
            "Rain", "Thunderstorm", "Snow", "Mist", "Smoke", "Haze", "Dust", "Sand", "Fog",
            "Ash", "Squall", "Tornado");

    public ForecastWithHikeTimeSuggestionDTO getDateSuggestionForTrail(TrailDetailsDTO trailDetails,
                                                                       Double averageSpeed,
                                                                       ForecastApiResponse forecastResponse) {
        double timeEstimate = getTimeEstimate(trailDetails, averageSpeed);
        double timeEstimateRounded = ((double) Math.round(timeEstimate * 100)) / 100; //rounding
        // to 2dp to remove unnecessary accuracy
        ForecastObject forecastDayRecommended = getDayWithBestWeather(forecastResponse);
        LocalDateTime latestHikeTime = getLatestTimeForHike(timeEstimateRounded,
                forecastDayRecommended);
        //create suggestion object, times are multiplied by 1000 for consistency with other areas of
        //db which used epochofmilli
        TrailHikeTimeSuggestionDTO suggestion =
                new TrailHikeTimeSuggestionDTO(forecastDayRecommended.getDt() * 1000,
                        timeEstimateRounded,
                        trailDetails.getId(), forecastDayRecommended.getSunrise() * 1000,
                        latestHikeTime.toEpochSecond(ZoneOffset.UTC) * 1000,
                        trailDetails.getLatitude(), trailDetails.getLongitude());
        return new ForecastWithHikeTimeSuggestionDTO(forecastDayRecommended, suggestion);
    }

    public ForecastWithHikeTimeSuggestionDTO getUncustomizedDateSuggestionForTrail(TrailDetailsDTO trailDetails,
                                                                                   ForecastApiResponse forecastResponse) {
        double timeEstimate = trailDetails.getAverageTime();
        ForecastObject forecastDayRecommended = getDayWithBestWeather(forecastResponse);
        LocalDateTime latestHikeTime = getLatestTimeForHike(timeEstimate, forecastDayRecommended);
        TrailHikeTimeSuggestionDTO suggestion =
                new TrailHikeTimeSuggestionDTO(forecastDayRecommended.getDt() * 1000, timeEstimate,
                        trailDetails.getId(), forecastDayRecommended.getSunrise() * 1000,
                        latestHikeTime.toEpochSecond(ZoneOffset.UTC) * 1000,
                        trailDetails.getLatitude(), trailDetails.getLongitude());
        return new ForecastWithHikeTimeSuggestionDTO(forecastDayRecommended, suggestion);
    }

    private double getTimeEstimate(TrailDetailsDTO trailDetails, Double averageSpeed) {
        // elevation calculation based on Naismiths Rule (see documentation)
        double elevationModifierValue = ((trailDetails.getElevation() * 3.28) / 1000) * 30;
        double difficultyValue = getDifficultyNumericValue(trailDetails.getDifficulty());
        double baseTimeEstimate = trailDetails.getLength() / averageSpeed;
        // change the time estimate into minutes then add the value to represent elevation and
        // multiply by value representing overall difficulty of trail
        return ((baseTimeEstimate * 60) + elevationModifierValue) * difficultyValue;
    }

    private double getDifficultyNumericValue(String difficulty) {
        if (difficulty.equals("Easy")) {
            return 0.75;
        } else if (difficulty.equals("Challenging")) {
            return 1.25;
        } else {
            return 1;
        }
    }

    private ForecastObject getDayWithBestWeather(ForecastApiResponse forecastResponse) {
        // sort the weather data ordered by the best weather conditions then get all days with
        // the best condition
        List<ForecastObject> orderedWeatherListOfAllConditions = forecastResponse
                .getDaily().stream().sorted(Comparator.comparing((forecast ->
                        weatherOrderOfPreference.indexOf(forecast.getWeather().get(0).getMain()))))
                .collect(Collectors.toList());
        List<ForecastObject> orderedWeatherListOfBestCondition =
                orderedWeatherListOfAllConditions.stream()
                                .filter(weather ->
                                        weather.getWeather().get(0).getMain().equals(
                                                orderedWeatherListOfAllConditions.get(0).getWeather().get(0).getMain()))
                                .collect(Collectors.toList());
        if (orderedWeatherListOfBestCondition.size() > 1) { // filter out any duplicates if low
            // temp or high wind is present
            orderedWeatherListOfBestCondition =
                    orderedWeatherListOfBestCondition.stream()
                                                     .filter(weather ->
                                                             weather.getTemp().getDay() > 0 || weather.getWind_speed() > 10)
                                                     .collect(Collectors.toList());
        }
        return orderedWeatherListOfBestCondition.size() < 1 ? forecastResponse.getDaily().get(0)
                : orderedWeatherListOfBestCondition.get(0);
    }

    private LocalDateTime getLatestTimeForHike(double timeEstimate, ForecastObject forecast) {
        LocalDateTime sunsetTime = LocalDateTime.ofEpochSecond(forecast.getSunset(), 0,
                ZoneOffset.UTC);
        return sunsetTime.minus((long) timeEstimate, ChronoUnit.MINUTES);
    }
}
