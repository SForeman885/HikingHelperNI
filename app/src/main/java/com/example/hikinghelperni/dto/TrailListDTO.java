package com.example.hikinghelperni.dto;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Value;

@Value
public class TrailListDTO {
    String id;
    String name;
    String imageLink;
    String locationName;
    double latitude;
    double longitude;
    double length;
    String difficulty;

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
