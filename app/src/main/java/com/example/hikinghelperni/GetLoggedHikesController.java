package com.example.hikinghelperni;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.stream.Collectors;

public class GetLoggedHikesController {

    //use the list of documents to each fill a CustomLoggedHike object
    //which can be used by the application
    public List<CustomLoggedHikeDTO> getLoggedHikesFromDocuments(List<DocumentSnapshot> retrievedDocuments) {
        return retrievedDocuments.stream()
                                 .map(doc -> new CustomLoggedHikeDTO(doc.get("trailName").toString(), doc.getLong("date"), Double.parseDouble(doc.get("length").toString()), Double.parseDouble(doc.get("elevation").toString()), Integer.parseInt(doc.get("timeTaken").toString()), doc.get("difficulty").toString()))
                                 .collect(Collectors.toList());
    }
}
