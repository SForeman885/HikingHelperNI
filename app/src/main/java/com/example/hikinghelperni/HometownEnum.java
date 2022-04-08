package com.example.hikinghelperni;

import lombok.Getter;

@Getter
public enum HometownEnum {
    ARMAGH("Armagh", 54.3503, 6.6528),
    BELFAST("Belfast", 54.5973, 5.9301),
    DERRY("Derry", 54.9966, 7.3086),
    ENNISKILLEN("Enniskillen", 54.3438, 7.6315),
    NEWRY("Newry", 54.1751, 6.3402),
    OMAGH("Omagh", 54.5977, 7.3100);

    private final String name;
    private final Double latitude;
    private final Double longitude;

    HometownEnum(String name, Double latitude, Double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
