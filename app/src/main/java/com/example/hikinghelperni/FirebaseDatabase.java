package com.example.hikinghelperni;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
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

    public Task<QuerySnapshot> getLoggedHikes(String userId) {

        return db.collection("Users").document(userId).collection("Logs").get()
                .addOnSuccessListener(aVoid -> Log.d(this.getClass().toString(), "Logged Hikes Retrieved Successfully"))
                .addOnFailureListener(e -> Log.w(this.getClass().toString(), "Error retrieving logged hikes for user", e));
    }
}
