package com.example.hikinghelperni;

import android.location.Location;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GetTrailsController {

    public List<TrailListDTO> getSavedTrailsListItemsFromDocuments(List<DocumentSnapshot> retrievedDocuments) {
        return mapTrailsListItemsFromDocuments(retrievedDocuments, true);
    }

    public List<TrailListDTO> getTrailsListItemsFromDocuments(List<DocumentSnapshot> retrievedDocuments) {
        return mapTrailsListItemsFromDocuments(retrievedDocuments, false);
    }

    public List<TrailListDTO> mapTrailsListItemsFromDocuments(List<DocumentSnapshot> retrievedDocuments, Boolean isSavedTrailList) {
        return retrievedDocuments.stream()
                                 .map(doc -> new TrailListDTO(
                                         isSavedTrailList ? doc.get("id").toString() : doc.getId(),
                                         doc.get("name").toString(),
                                         doc.get("imageLink").toString(),
                                         doc.get("locationName").toString(),
                                         Double.parseDouble(doc.get("latitude").toString()),
                                         Double.parseDouble(doc.get("longitude").toString()),
                                         Double.parseDouble(doc.get("length").toString()),
                                         doc.get("difficulty").toString()))
                                 .collect(Collectors.toList());
    }

    public List<TrailListDTO> getOrderedTrailList(List<TrailListDTO> unorderedTrails, String hometown) {
        //Sorts list of trails in increasing distance from hometown
        HometownEnum hometownValue = HometownEnum.valueOf(hometown.toUpperCase());
        Location userLocation = new Location("");
        userLocation.setLatitude(hometownValue.getLatitude());
        userLocation.setLongitude(hometownValue.getLongitude());
        unorderedTrails.sort(Comparator.comparing(trail -> getDistanceFromHometown(trail, userLocation)));
        return unorderedTrails;
    }

    private double getDistanceFromHometown(TrailListDTO trail, Location userLocation) {
        Location trailLocation = new Location("");
        trailLocation.setLatitude(trail.getLatitude());
        trailLocation.setLongitude(trail.getLongitude());
        return  userLocation.distanceTo(trailLocation);
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
