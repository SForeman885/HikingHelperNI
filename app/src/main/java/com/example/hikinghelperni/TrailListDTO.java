package com.example.hikinghelperni;

import lombok.Value;

@Value
public class TrailListDTO {
    String id;
    String trailName;
    String imageLink;
    String location;
    double length;
    String difficulty;
}
