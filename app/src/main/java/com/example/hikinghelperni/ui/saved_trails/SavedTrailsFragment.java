package com.example.hikinghelperni.ui.saved_trails;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikinghelperni.FirebaseUIActivity;
import com.example.hikinghelperni.GetTrailsController;
import com.example.hikinghelperni.R;
import com.example.hikinghelperni.TrailListDTO;
import com.example.hikinghelperni.TrailsAdapter;
import com.example.hikinghelperni.databinding.FragmentSavedTrailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Comparator;
import java.util.List;

public class SavedTrailsFragment extends Fragment {

    private FragmentSavedTrailsBinding binding;

    private FirebaseFirestore firestore;
    private FirebaseAuth mFirebaseAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSavedTrailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView rvSavedTrailsList = binding.listSavedTrailsRecyclerView;
        firestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            CollectionReference getSavedTrails = firestore.collection("Users").document(user.getUid()).collection("Saved Trails");
            getSavedTrails.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> retrievedDocuments = task.getResult().getDocuments();
                    if (!retrievedDocuments.isEmpty()) {
                        sortSavedTrailsAndSetAdapter(rvSavedTrailsList, retrievedDocuments);
                    } else {
                        Log.d(this.getClass().toString(), "No Trails Found");
                        binding.listSavedTrailsScrollView.setVisibility(View.GONE);
                        binding.noSavedTrailsMessage.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.d(this.getClass().toString(), "getting trails failed with ", task.getException());
                }
            });
            rvSavedTrailsList.setLayoutManager(new LinearLayoutManager(this.getContext()));
        } else {
            binding.listSavedTrailsScrollView.setVisibility(View.GONE);
            binding.loginPromptSavedLogs.getRoot().setVisibility(View.VISIBLE);
            binding.loginPromptSavedLogs.messageSignIn.setText(R.string.sign_in_message_saved_trails);
            binding.loginPromptSavedLogs.buttonSignIn.setOnClickListener((v) -> {
                Intent intent = new Intent(this.getActivity(), FirebaseUIActivity.class);
                startActivity(intent);
            });
        }

        return root;
    }

    private void sortSavedTrailsAndSetAdapter(RecyclerView rvSavedTrailsList, List<DocumentSnapshot> retrievedDocuments) {
        GetTrailsController getTrailsController = new GetTrailsController();
        List<TrailListDTO> retrievedTrails = getTrailsController.getSavedTrailsListItemsFromDocuments(retrievedDocuments);
        retrievedTrails.sort(Comparator.comparing(TrailListDTO::getName));
        TrailsAdapter adapter = new TrailsAdapter(this.getContext(), getParentFragment().getParentFragmentManager(), retrievedTrails, "Saved Trails");
        rvSavedTrailsList.setAdapter(adapter);
    }
}
