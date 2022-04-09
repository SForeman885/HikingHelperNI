package com.example.hikinghelperni.ui.trails;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikinghelperni.GetTrailsController;
import com.example.hikinghelperni.TrailListDTO;
import com.example.hikinghelperni.TrailsAdapter;
import com.example.hikinghelperni.ViewLogsAdapter;
import com.example.hikinghelperni.databinding.FragmentTrailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TrailsFragment extends Fragment {

    private FragmentTrailsBinding binding;

    private FirebaseFirestore firestore;
    private FirebaseAuth mFirebaseAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TrailsViewModel trailsViewModel = new ViewModelProvider(this).get(TrailsViewModel.class);

        binding = FragmentTrailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        setHasOptionsMenu(true);
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setTitle("View Trails");

        firestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        RecyclerView rvTrailsList = binding.listTrailsRecyclerView;

        CollectionReference getTrails = firestore.collection("Trails");
        getTrails.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> retrievedDocuments = task.getResult().getDocuments();
                if (!retrievedDocuments.isEmpty()) {
                    sortTrailsIfSignedInAndSetAdapter(rvTrailsList, retrievedDocuments);
                } else {
                    Log.d(this.getClass().toString(), "No Trails Found");
                }
            } else {
                Log.d(this.getClass().toString(), "getting trails failed with ", task.getException());
            }
        });
        rvTrailsList.setLayoutManager(new LinearLayoutManager(this.getContext()));
        return root;
    }

    private void sortTrailsIfSignedInAndSetAdapter(RecyclerView rvTrailsList, List<DocumentSnapshot> retrievedDocuments) {
        GetTrailsController getTrailsController = new GetTrailsController();
        List<TrailListDTO> retrievedTrails = getTrailsController.getTrailsListItemsFromDocuments(retrievedDocuments);
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if(user != null) {
            DocumentReference userRef = firestore.collection("Users").document(user.getUid());
            userRef.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    if(!task.getResult().get("hometown").equals("hometown")) {
                        List<TrailListDTO> orderedTrails = getTrailsController.getOrderedTrailList(retrievedTrails, task.getResult().get("hometown").toString());
                        TrailsAdapter adapter = new TrailsAdapter(this.getContext(), getParentFragmentManager(), orderedTrails, "View Trails Fragment");
                        rvTrailsList.setAdapter(adapter);
                    }
                    else {
                        TrailsAdapter adapter = new TrailsAdapter(this.getContext(), getParentFragmentManager(), retrievedTrails, "View Trails Fragment");
                        rvTrailsList.setAdapter(adapter);
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}