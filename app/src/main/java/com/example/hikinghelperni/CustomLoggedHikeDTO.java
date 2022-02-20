package com.example.hikinghelperni;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Value;

@Value
public class CustomLoggedHikeDTO {

    String trailName;
    Long date;
    double length;
    int timeTaken;
    String difficulty;

    public Map<String, Object> LogMapper() {
        return Arrays.stream(this.getClass().getDeclaredFields()).collect(Collectors.toMap(field -> field.getName(),
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