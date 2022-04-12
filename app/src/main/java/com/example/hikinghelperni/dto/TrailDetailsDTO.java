package com.example.hikinghelperni;

import lombok.Value;

@Value
public class TrailDetailsDTO {
    String id;
    String trailName;
    String imageLink;
    String mapLink;
    String description;
    String location;
    double latitude;
    double longitude;
    double length;
    int elevation;
    int averageTime;
    String difficulty;
}
