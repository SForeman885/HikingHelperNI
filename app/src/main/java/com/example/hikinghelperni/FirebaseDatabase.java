package com.example.hikinghelperni;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirebaseDatabase {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addNewUser(String userId) {
        Map<String, Object> defaultUserDetails = new HashMap<>();
        defaultUserDetails.put("hometown", "");
        db.collection("Users").document(userId).set(defaultUserDetails);
    }
}
