package com.example.hikinghelperni.ui.trails;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikinghelperni.GetTrailsController;
import com.example.hikinghelperni.TrailListDTO;
import com.example.hikinghelperni.TrailsAdapter;
import com.example.hikinghelperni.databinding.FragmentTrailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class TrailsFragment extends Fragment {

    private FragmentTrailsBinding binding;
    private TrailsAdapter adapter;
    private Boolean hasSearchBeenPreviouslySubmitted = false;

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

        Query getTrails = firestore.collection("Trails");
        SetTrailsList(getTrails, rvTrailsList);
        SetupSearch(rvTrailsList);
        return root;
    }

    private void SetTrailsList(Query getTrails, RecyclerView rvTrailsList) {
        getTrails.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> retrievedDocuments = task.getResult().getDocuments();
                if (!retrievedDocuments.isEmpty()) {
                    SortTrailsIfSignedInAndSetAdapter(rvTrailsList, retrievedDocuments);
                } else {
                    Log.d(this.getClass().toString(), "No Trails Found");
                    Toast.makeText(getContext(), "No Trails Found", Toast.LENGTH_SHORT).show();
                    List<TrailListDTO> emptyTrailsList = new ArrayList<>();
                    TrailsAdapter adapter = new TrailsAdapter(getContext(), getParentFragmentManager(), emptyTrailsList, "Trails Fragment");
                    rvTrailsList.setAdapter(adapter);
                }
            } else {
                Log.d(this.getClass().toString(), "getting trails failed with ", task.getException());
            }
        });
        rvTrailsList.setLayoutManager(new LinearLayoutManager(this.getContext()));
    }

    private void SortTrailsIfSignedInAndSetAdapter(RecyclerView rvTrailsList, List<DocumentSnapshot> retrievedDocuments) {
        GetTrailsController getTrailsController = new GetTrailsController();
        List<TrailListDTO> retrievedTrails = getTrailsController.getTrailsListItemsFromDocuments(retrievedDocuments);
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if(user != null) {
            DocumentReference userRef = firestore.collection("Users").document(user.getUid());
            userRef.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    if(!task.getResult().get("hometown").equals("hometown")) {
                        List<TrailListDTO> orderedTrails = getTrailsController.getOrderedTrailList(retrievedTrails, task.getResult().get("hometown").toString());
                        adapter = new TrailsAdapter(this.getContext(), getParentFragmentManager(), orderedTrails, "View Trails Fragment");
                    }
                    else {
                        adapter = new TrailsAdapter(this.getContext(), getParentFragmentManager(), retrievedTrails, "View Trails Fragment");
                    }
                    rvTrailsList.setAdapter(adapter);
                }
            });
        }
        else {
            adapter = new TrailsAdapter(this.getContext(), getParentFragmentManager(), retrievedTrails, "View Trails Fragment");
            rvTrailsList.setAdapter(adapter);
        }
    }

    private void SetupSearch(RecyclerView rvTrailsList) {
        binding.trailsSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            @Override
            public boolean onQueryTextSubmit(String s) {
                Query getTrailsForSearch = firestore.collection("Trails").whereGreaterThanOrEqualTo("name", s).whereLessThan("name", s + '\uf8ff');
                SetTrailsList(getTrailsForSearch, rvTrailsList);
                hasSearchBeenPreviouslySubmitted = true;
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                if(s.isEmpty() && hasSearchBeenPreviouslySubmitted) {
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    if (Build.VERSION.SDK_INT >= 26) {
                        transaction.setReorderingAllowed(false);
                    }
                    transaction.detach(TrailsFragment.this).commit();
                    transaction = getParentFragmentManager().beginTransaction();
                    transaction.attach(TrailsFragment.this).commit();
                    hasSearchBeenPreviouslySubmitted = false;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}