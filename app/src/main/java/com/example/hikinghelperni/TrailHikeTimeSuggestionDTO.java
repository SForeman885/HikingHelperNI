package com.example.hikinghelperni;

import lombok.Value;

@Value
public class TrailHikeTimeSuggestionDTO {
    Long dateTime;
    Double userTimeEstimate;
    String trailId;
    Long earliestHikeTime;
    Long latestHikeTime;
}
