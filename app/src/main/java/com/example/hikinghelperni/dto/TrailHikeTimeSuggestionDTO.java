package com.example.hikinghelperni.dto;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Value;

@Value
public class TrailHikeTimeSuggestionDTO {
    Long dateTime;
    Double userTimeEstimate;
    String trailId;
    Long earliestHikeTime;
    Long latestHikeTime;
    Double latitude;
    Double longitude;

    public Map<String, Object> LogMapper() {
        return Arrays.stream(this.getClass().getDeclaredFields()).collect(Collectors.toMap(Field::getName,
                field ->
                {
                    try {
                        return field.get(this);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return "";
                    }
                }
                ));
    }
}
