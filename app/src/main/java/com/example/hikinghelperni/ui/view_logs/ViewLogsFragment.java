package com.example.hikinghelperni.ui.view_logs;

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
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikinghelperni.FirebaseUIActivity;
import com.example.hikinghelperni.services.GetLoggedHikesController;
import com.example.hikinghelperni.R;
import com.example.hikinghelperni.adapter.ViewLogsAdapter;
import com.example.hikinghelperni.databinding.FragmentViewLogsBinding;
import com.example.hikinghelperni.ui.log_hikes.LogHikesFragment;
import com.example.hikinghelperni.ui.trails.TrailsViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ViewLogsFragment extends Fragment {

    private FragmentViewLogsBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseAuth mFirebaseAuth;
    private TrailsViewModel trailsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        trailsViewModel =
                new ViewModelProvider((FragmentActivity) this.getContext()).get(TrailsViewModel.class);
        //clear trailid to ensure a the user can navigate to create a custom log from this page
        trailsViewModel.setMTrailId("");
        binding = FragmentViewLogsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setHasOptionsMenu(true);

        mFirebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();

        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setTitle("Your Logs");
        if (user != null) {
            RecyclerView rvLoggedHikes = binding.viewLogsRecyclerView;
            GetLoggedHikesController getLoggedHikesController = new GetLoggedHikesController();
            CollectionReference getLogs = firestore.collection("Users")
                                                   .document(user.getUid()).collection("Logs");
            //get data from db and when call is complete handle results in adapter
            getLogs.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> retrievedDocuments = task.getResult().getDocuments();
                    if (!retrievedDocuments.isEmpty()) {
                        ViewLogsAdapter adapter = new
                                ViewLogsAdapter(getLoggedHikesController.getLoggedHikesFromDocuments(retrievedDocuments));
                        rvLoggedHikes.setAdapter(adapter);
                    } else {
                        Log.d(this.getClass().toString(), "No Logs Found");
                        binding.noLoggedHikesMessage.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.d(this.getClass().toString(), "getting logs failed with ",
                            task.getException());
                }
            });
            rvLoggedHikes.setLayoutManager(new LinearLayoutManager(this.getContext()));

            setUpAddButton();
        } else {
            binding.addLogFab.setVisibility(View.GONE);
            binding.viewLogsScrollView.setVisibility(View.GONE);
            binding.loginPromptViewLogs.getRoot().setVisibility(View.VISIBLE);
            binding.loginPromptViewLogs.messageSignIn.setText(R.string.sign_in_message_view_logs);
            binding.loginPromptViewLogs.buttonSignIn.setOnClickListener((v) -> {
                Intent intent = new Intent(this.getActivity(), FirebaseUIActivity.class);
                startActivity(intent);
            });
        }
        return root;
    }

    private void setUpAddButton() {
        binding.addLogFab.setOnClickListener((v) -> {
            LogHikesFragment nextFragment = new LogHikesFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction()
                           .replace(R.id.nav_host_fragment_activity_main, nextFragment)
                           .addToBackStack("ViewLogsFragment")
                           .commit();
        });
    }
}
