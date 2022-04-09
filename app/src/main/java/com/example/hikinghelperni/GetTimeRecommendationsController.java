package com.example.hikinghelperni;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.stream.Collectors;

public class GetTimeRecommendationsController {

    public List<TrailHikeTimeSuggestionDTO> mapTrailsListItemsFromDocuments(List<DocumentSnapshot> retrievedDocuments) {
        return retrievedDocuments.stream()
                                 .map(doc -> new TrailHikeTimeSuggestionDTO(
                                         doc.getLong("dateTime"),
                                         doc.getDouble("userTimeEstimate"),
                                         doc.get("trailId").toString(),
                                         doc.getLong("earliestHikeTime"),
                                         doc.getLong("latestHikeTime"),
                                         Double.parseDouble(doc.get("latitude").toString()),
                                         Double.parseDouble(doc.get("longitude").toString())))
                                 .collect(Collectors.toList());
    }
}
