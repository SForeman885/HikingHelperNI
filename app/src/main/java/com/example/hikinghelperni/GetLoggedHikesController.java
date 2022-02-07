package com.example.hikinghelperni;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.stream.Collectors;

public class GetLoggedHikesController {

    public List<CustomLoggedHike> getLoggedHikes(List<DocumentSnapshot> retrievedDocuments) {
        return retrievedDocuments.stream()
                .map(doc -> new CustomLoggedHike(doc.get("trailName").toString(), doc.getLong("date"), Double.parseDouble(doc.get("length").toString()), Integer.parseInt(doc.get("timeTaken").toString()), doc.get("difficulty").toString()))
                .collect(Collectors.toList());
    }
}
