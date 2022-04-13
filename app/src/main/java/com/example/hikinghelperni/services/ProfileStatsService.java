package com.example.hikinghelperni.services;

import com.example.hikinghelperni.dto.CustomLoggedHikeDTO;
import com.example.hikinghelperni.dto.ProfileStatsDTO;

import java.util.List;

public class ProfileStatsService {

    public ProfileStatsDTO getUserStats(List<CustomLoggedHikeDTO> loggedHikes) {
        double totalDistance = loggedHikes.stream().mapToDouble(CustomLoggedHikeDTO::getLength).sum();
        return new ProfileStatsDTO(loggedHikes.size(), totalDistance);
    }
}
