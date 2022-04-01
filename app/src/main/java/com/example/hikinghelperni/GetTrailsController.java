package com.example.hikinghelperni;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.stream.Collectors;

public class GetTrailsController {

    public List<TrailListDTO> getTrailsListItemsFromDocuments(List<DocumentSnapshot> retrievedDocuments) {
        return retrievedDocuments.stream()
                                 .map(doc -> new TrailListDTO(doc.getId(),
                                         doc.get("name").toString(),
                                         doc.get("imageLink").toString(),
                                         doc.get("locationName").toString(),
                                         Double.parseDouble(doc.get("length").toString()),
                                         doc.get("difficulty").toString()))
                                 .collect(Collectors.toList());
    }

    public TrailDetailsDTO getTrailDetailsFromDocument(DocumentSnapshot retrievedDocument) {
        return new TrailDetailsDTO(retrievedDocument.getId(),
                retrievedDocument.get("name").toString(),
                retrievedDocument.get("imageLink").toString(),
                retrievedDocument.get("mapLink").toString(),
                retrievedDocument.get("description").toString(),
                retrievedDocument.get("locationName").toString(),
                Double.parseDouble(retrievedDocument.get("latitude").toString()),
                Double.parseDouble(retrievedDocument.get("longitude").toString()),
                Double.parseDouble(retrievedDocument.get("length").toString()),
                Integer.parseInt(retrievedDocument.get("elevation").toString()),
                Integer.parseInt(retrievedDocument.get("averageTime").toString()),
                retrievedDocument.get("difficulty").toString());
    }
}
