package com.example.hikinghelperni;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.hikinghelperni.ui.trail_details.TrailDetailsFragment;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirebaseDatabase {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addNewUser(String userId) {
        Map<String, Object> defaultUserDetails = new HashMap<>();
        defaultUserDetails.put("hometown", "hometown");
        db.collection("Users").document(userId).set(defaultUserDetails)
                .addOnSuccessListener(aVoid -> Log.d(this.getClass().toString(), "Sign Up Successful!"))
                .addOnFailureListener(e -> Log.w(this.getClass().toString(), "Error Signing Up", e));
    }

    public void addNewCustomLog(Map<String, Object> loggedHike, String userId, Context context) {
        db.collection("Users").document(userId).collection("Logs").document().set(loggedHike)
          .addOnSuccessListener(aVoid -> {
              Log.d(this.getClass().toString(), "Hike Logged Successfully");
              Toast.makeText(context, "Hike Logged Successfully!", Toast.LENGTH_SHORT).show();
          })
          .addOnFailureListener(e -> {
              Log.w(this.getClass().toString(), "Error Logging Hike, please try again", e);
              Toast.makeText(context, "Error Logging Hike, please try again", Toast.LENGTH_SHORT).show();
          });
    }

    public void addNewSavedTrail(Map<String, Object> savedTrail, String userId, Context context) {
        db.collection("Users").document(userId).collection("Saved Trails").document().set(savedTrail)
          .addOnSuccessListener(aVoid -> {
              Log.d(this.getClass().toString(), "Saved Trail Successfully!");
              Toast.makeText(context, "Trail Saved Successfully", Toast.LENGTH_SHORT).show();
          })
          .addOnFailureListener(e -> {
              Log.w(this.getClass().toString(), "Error Saving trail, please try again", e);
              Toast.makeText(context, "Error Saving trail, please try again", Toast.LENGTH_SHORT).show();
          });
    }

    public void deleteSavedTrail(String savedTrailId, String userId, Context context) {
        db.collection("Users").document(userId).collection("Saved Trails").document(savedTrailId).delete()
          .addOnSuccessListener(aVoid -> {
              Log.d(this.getClass().toString(), "Deleted Trail Successfully!");
              Toast.makeText(context, "Trail Unsaved Successfully", Toast.LENGTH_SHORT).show();
          })
          .addOnFailureListener(e -> {
              Log.w(this.getClass().toString(), "Error Deleting trail, please try again", e);
              Toast.makeText(context, "Error unsaving trail, please try again", Toast.LENGTH_SHORT).show();
          });
    }

    public void deleteSavedTrailFromList(String savedTrailId, String userId, Context context, TrailsAdapter adapter, int position) {
        db.collection("Users").document(userId).collection("Saved Trails").document(savedTrailId).delete()
          .addOnSuccessListener(aVoid -> {
              Log.d(this.getClass().toString(), "Deleted Trail Successfully!");
              adapter.mTrails.remove(position);
              adapter.notifyItemRemoved(position);
              Toast.makeText(context, "Trail Unsaved Successfully", Toast.LENGTH_SHORT).show();
          })
          .addOnFailureListener(e -> {
              Log.w(this.getClass().toString(), "Error Deleting trail, please try again", e);
              Toast.makeText(context, "Error unsaving trail, please try again", Toast.LENGTH_SHORT).show();
          });
    }

    public void addNewSavedTime(Map<String, Object> savedTrail, String userId, Context context) {
        db.collection("Users").document(userId).collection("Saved Times").document().set(savedTrail)
          .addOnSuccessListener(aVoid -> {
              Log.d(this.getClass().toString(), "Saved Time Successfully!");
              Toast.makeText(context, "Recommendation Saved Successfully", Toast.LENGTH_SHORT).show();
          })
          .addOnFailureListener(e -> {
              Log.w(this.getClass().toString(), "Error Saving time, please try again", e);
              Toast.makeText(context, "Error Saving Recommendation, please try again", Toast.LENGTH_SHORT).show();
          });
    }

    public void deleteSavedTime(String savedTrailId, String userId, Context context) {
        db.collection("Users").document(userId).collection("Saved Times").document(savedTrailId).delete()
          .addOnSuccessListener(aVoid -> {
              Log.d(this.getClass().toString(), "Deleted Time Successfully!");
              Toast.makeText(context, "Recommendation Unsaved Successfully", Toast.LENGTH_SHORT).show();
          })
          .addOnFailureListener(e -> {
              Log.w(this.getClass().toString(), "Error Deleting Time, please try again", e);
              Toast.makeText(context, "Error unsaving recommendation, please try again", Toast.LENGTH_SHORT).show();
          });
    }

    public void deleteSavedTimeFromList(String savedTimeId, String userId, Context context, TimeRecommendationsAdapter adapter, int position) {
        db.collection("Users").document(userId).collection("Saved Times").document(savedTimeId).delete()
          .addOnSuccessListener(aVoid -> {
              Log.d(this.getClass().toString(), "Deleted Time Successfully!");
              adapter.mRecommendedTimes.remove(position);
              adapter.notifyItemRemoved(position);
              Toast.makeText(context, "Recommendation Unsaved Successfully", Toast.LENGTH_SHORT).show();
          })
          .addOnFailureListener(e -> {
              Log.w(this.getClass().toString(), "Error Deleting time, please try again", e);
              Toast.makeText(context, "Error unsaving recommendation, please try again", Toast.LENGTH_SHORT).show();
          });
    }

    public void updateUserHometown(String userId, String hometown, Context context) {
        db.collection("Users").document(userId).update("hometown", hometown)
          .addOnSuccessListener(aVoid -> {
              Log.d(this.getClass().toString(), "Hometown Updated Successfully");
              Toast.makeText(context, "Hometown Successfully", Toast.LENGTH_SHORT).show();
          })
          .addOnFailureListener(e -> {
              Log.w(this.getClass().toString(), "Error Updating Hometown, please try again", e);
              Toast.makeText(context, "Error Updating Hometown, please try again", Toast.LENGTH_SHORT).show();
          });
    }
}
