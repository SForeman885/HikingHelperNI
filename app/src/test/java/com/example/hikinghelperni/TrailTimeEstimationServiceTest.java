package com.example.hikinghelperni;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TrailTimeEstimationServiceTest {

    private Double USER_AVERAGE_SPEED = 3.5;
    private long SIX_HOURS_IN_SECONDS = 21600;
    private long TWELVE_HOURS_IN_SECONDS = 43200;
    private long DAY_IN_SECONDS = 86400;

    private TrailTimeEstimationService trailTimeEstimationService;

    @Before
    public void SetUp() {
        trailTimeEstimationService = new TrailTimeEstimationService();
    }

    @Test
    public void getsDateSuggestion() {
        ForecastWithHikeTimeSuggestionDTO result = trailTimeEstimationService.getDateSuggestionForTrail(getTrailDetails(), USER_AVERAGE_SPEED, getForecastResponse());
        assert(result.equals(getExpectedResult()));
    }

    @Test
    public void getsUncustomisedDateSuggestion() {
        ForecastWithHikeTimeSuggestionDTO result = trailTimeEstimationService.getUncustomizedDateSuggestionForTrail(getTrailDetails(), getForecastResponse());
        assert(result.equals(getExpectedResultForUncustomised()));
    }

    private TrailDetailsDTO getTrailDetails() {
        return new TrailDetailsDTO(
                "uJd7lPJDB83M",
                "Trail One",
                "image.jpg",
                "map.png",
                "trail description",
                "Newcastle, Down",
                54.1234,
                6.1234,
                5,
                100,
                120,
                "medium");
    }

    private ForecastApiResponse getForecastResponse() {
        return new ForecastApiResponse(getForecastObjects());
    }

    private List<ForecastObject> getForecastObjects() {
        List<ForecastObject> forecastList = new ArrayList<>();
        forecastList.add(getForecastObject("Rain", 0));
        forecastList.add(getForecastObject("Clear", 1));
        return forecastList;
    }

    private ForecastObject getForecastObject(String weatherType, int dayOffset) {
        return new ForecastObject(
                LocalDateTime.now().getLong(ChronoField.EPOCH_DAY) + (DAY_IN_SECONDS * dayOffset),
                LocalDateTime.now().getLong(ChronoField.EPOCH_DAY) + (DAY_IN_SECONDS * dayOffset) + SIX_HOURS_IN_SECONDS,
                LocalDateTime.now().getLong(ChronoField.EPOCH_DAY) + (DAY_IN_SECONDS * dayOffset) + TWELVE_HOURS_IN_SECONDS,
                getTempObject(),
                (double)12,
                weatherType.equals("Rain") ? getWeatherObjectsRain() : getWeatherObjectsClear()
        );
    }

    private TemperatureObject getTempObject() {
        return new TemperatureObject(5.5);
    }

    private List<WeatherObject> getWeatherObjectsRain() {
        List<WeatherObject> weatherList = new ArrayList<>();
        weatherList.add(new WeatherObject(
                "Rain",
                "light rain",
                "10d"
        ));
        return weatherList;
    }

    private List<WeatherObject> getWeatherObjectsClear() {
        List<WeatherObject> weatherList = new ArrayList<>();
        weatherList.add(new WeatherObject(
                "Clear",
                "clear sky",
                "01d"
        ));
        return weatherList;
    }

    private ForecastWithHikeTimeSuggestionDTO getExpectedResult() {
        ForecastObject forecastObject = getForecastObject("Clear", 1);
        return new ForecastWithHikeTimeSuggestionDTO(
                forecastObject,
                new TrailHikeTimeSuggestionDTO(
                        (LocalDateTime.now().getLong(ChronoField.EPOCH_DAY) + (DAY_IN_SECONDS))*1000,
                        95.55,
                        "uJd7lPJDB83M",
                        forecastObject.getSunrise()*1000,
                        getExpectedLatestTime(forecastObject.getSunset(), 95.55),
                        54.1234,
                        6.1234
                )
        );
    }

    private ForecastWithHikeTimeSuggestionDTO getExpectedResultForUncustomised() {
        ForecastObject forecastObject = getForecastObject("Clear", 1);
        return new ForecastWithHikeTimeSuggestionDTO(
                forecastObject,
                new TrailHikeTimeSuggestionDTO(
                        (LocalDateTime.now().getLong(ChronoField.EPOCH_DAY) + (DAY_IN_SECONDS))*1000,
                        120.0,
                        "uJd7lPJDB83M",
                        forecastObject.getSunrise()*1000,
                        getExpectedLatestTime(forecastObject.getSunset(), 120),
                        54.1234,
                        6.1234
                )
        );
    }

    private long getExpectedLatestTime(long latestTime, double timeEstimate) {
        LocalDateTime sunsetTime = LocalDateTime.ofEpochSecond(latestTime, 0, ZoneOffset.UTC);
        return sunsetTime.minus((long)timeEstimate, ChronoUnit.MINUTES).toEpochSecond(ZoneOffset.UTC)*1000;
    }
}
