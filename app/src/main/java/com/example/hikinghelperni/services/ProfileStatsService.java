package com.example.hikinghelperni;

import java.util.List;

public class ProfileStatsService {

    public ProfileStatsDTO getUserStats(List<CustomLoggedHikeDTO> loggedHikes) {
        double totalDistance = loggedHikes.stream().mapToDouble(CustomLoggedHikeDTO::getLength).sum();
        return new ProfileStatsDTO(loggedHikes.size(), totalDistance);
    }
}
