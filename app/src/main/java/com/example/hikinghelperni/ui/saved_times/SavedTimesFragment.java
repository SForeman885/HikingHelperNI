package com.example.hikinghelperni.ui.saved_times;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikinghelperni.FirebaseUIActivity;
import com.example.hikinghelperni.services.GetTimeRecommendationsController;
import com.example.hikinghelperni.R;
import com.example.hikinghelperni.adapter.TimeRecommendationsAdapter;
import com.example.hikinghelperni.dto.TrailHikeTimeSuggestionDTO;
import com.example.hikinghelperni.databinding.FragmentSavedTimesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Comparator;
import java.util.List;

public class SavedTimesFragment extends Fragment {

    FragmentSavedTimesBinding binding;

    private FirebaseFirestore firestore;
    private FirebaseAuth mFirebaseAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSavedTimesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView rvSavedTimesList = binding.listSavedTimesRecyclerView;
        firestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            CollectionReference getSavedTrails = firestore.collection("Users").document(user.getUid()).collection("Saved Times");
            getSavedTrails.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> retrievedDocuments = task.getResult().getDocuments();
                    if (!retrievedDocuments.isEmpty()) {
                        SortSavedTrailsAndSetAdapter(rvSavedTimesList, retrievedDocuments);
                    } else {
                        Log.d(this.getClass().toString(), "No Trails Found");
                        binding.listSavedTimesScrollView.setVisibility(View.GONE);
                        binding.noSavedTimesMessage.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.d(this.getClass().toString(), "getting trails failed with ", task.getException());
                }
            });
            rvSavedTimesList.setLayoutManager(new LinearLayoutManager(this.getContext()));
        } else {
            binding.listSavedTimesScrollView.setVisibility(View.GONE);
            binding.loginPromptSavedTimes.getRoot().setVisibility(View.VISIBLE);
            binding.loginPromptSavedTimes.messageSignIn.setText(R.string.sign_in_message_saved_times);
            binding.loginPromptSavedTimes.buttonSignIn.setOnClickListener((v) -> {
                Intent intent = new Intent(this.getActivity(), FirebaseUIActivity.class);
                startActivity(intent);
            });
        }

        ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setTitle("Your Saved Items");
        return root;
    }

    private void SortSavedTrailsAndSetAdapter(RecyclerView rvSavedTimesList, List<DocumentSnapshot> retrievedDocuments) {
        GetTimeRecommendationsController getTimeRecommendationsController = new GetTimeRecommendationsController();
        List<TrailHikeTimeSuggestionDTO> retrievedTimes = getTimeRecommendationsController.mapTrailsListItemsFromDocuments(retrievedDocuments);
        retrievedTimes.sort(Comparator.comparing(TrailHikeTimeSuggestionDTO::getDateTime).reversed());
        TimeRecommendationsAdapter adapter = new TimeRecommendationsAdapter(this.getContext(), getParentFragment().getParentFragmentManager(), retrievedTimes);
        rvSavedTimesList.setAdapter(adapter);
    }
}
