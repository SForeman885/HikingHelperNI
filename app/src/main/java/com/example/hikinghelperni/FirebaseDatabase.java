package com.example.hikinghelperni;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirebaseDatabase {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addNewUser(String userId) {
        Map<String, Object> defaultUserDetails = new HashMap<>();
        defaultUserDetails.put("hometown", "");
        db.collection("Users").document(userId).set(defaultUserDetails)
                .addOnSuccessListener(aVoid -> Log.d(this.getClass().toString(), "Sign Up Successful!"))
                .addOnFailureListener(e -> Log.w(this.getClass().toString(), "Error Signing Up", e));
    }

    public void addNewCustomLog(Map<String, Object> loggedHike, String userId) {
        db.collection("Users").document(userId).collection("Logs").document().set(loggedHike)
                .addOnSuccessListener(aVoid -> Log.d(this.getClass().toString(), "Logged Successfully"))
                .addOnFailureListener(e -> Log.w(this.getClass().toString(), "Error making log, please try again", e));
    }
}
