package com.example.hikinghelperni;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.stream.Collectors;

public class GetTrailsController {

    public List<TrailListDTO> getTrailsListItemsFromDocuments(List<DocumentSnapshot> retrievedDocuments) {
        return retrievedDocuments.stream()
                .map(doc -> new TrailListDTO(doc.get("name").toString(), doc.get("imageLink").toString(), doc.get("locationName").toString(), Double.parseDouble(doc.get("length").toString()), doc.get("difficulty").toString()))
                .collect(Collectors.toList());
    }
}
