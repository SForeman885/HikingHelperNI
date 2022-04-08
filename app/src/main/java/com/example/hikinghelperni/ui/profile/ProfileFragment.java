package com.example.hikinghelperni.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.hikinghelperni.CustomLoggedHikeDTO;
import com.example.hikinghelperni.FirebaseDatabase;
import com.example.hikinghelperni.FirebaseUIActivity;
import com.example.hikinghelperni.GetLoggedHikesController;
import com.example.hikinghelperni.ProfileStatsDTO;
import com.example.hikinghelperni.ProfileStatsService;
import com.example.hikinghelperni.R;
import com.example.hikinghelperni.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    private FirebaseFirestore firestore;
    private FirebaseAuth mFirebaseAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        firestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setHasOptionsMenu(true);
        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        //set back icon and make visible on left of action bar
        ab.setHomeAsUpIndicator(R.drawable.ic_back_arrow_black_24);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Profile Menu");

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            SetUpHometownDropDown(user);
            SetUpSaveButton();
            SetupStatsTimeframeDropdown(user);
            SetUserStats(user);
            binding.userName.setText(user.getDisplayName());

        } else {
            binding.profileSection.setVisibility(View.GONE);
            binding.loginPromptProfile.getRoot().setVisibility(View.VISIBLE);
            binding.loginPromptProfile.messageSignIn.setText(R.string.sign_in_message_profile);
            binding.loginPromptProfile.buttonSignIn.setOnClickListener((v) -> {
                Intent intent = new Intent(this.getActivity(), FirebaseUIActivity.class);
                startActivity(intent);
            });
        }

        return root;
    }

    private void SetUpHometownDropDown(FirebaseUser user) {
        //filling in hometown control with array adapter to allow dropdown
        String[] townItems = new String[]{"Select A Town:", "Armagh", "Belfast", "Derry",
                "Enniskillen", "Newry", "Omagh"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(),
                R.layout.custom_dropdown, townItems);
        binding.spinnerHometown.setAdapter(adapter);
        firestore.collection("Users").document(user.getUid()).get()
                 .addOnCompleteListener(task -> {
                     if (task.isSuccessful()) {
                         String hometown = task.getResult().get("hometown").toString();
                         int hometownIndex = Arrays.asList(townItems).indexOf(hometown);
                         if (hometownIndex > 0) {
                             binding.spinnerHometown.setSelection(hometownIndex);
                         }
                     }
                 });
    }

    private void SetupStatsTimeframeDropdown(FirebaseUser user) {
        //filling in hometown control with array adapter to allow dropdown
        String[] timeframeItems = new String[]{"Past 7 Days", "Past 30 Days", "Past Year", "All Time"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(),
                R.layout.custom_dropdown, timeframeItems);
        binding.spinnerStatsTimeframe.setAdapter(adapter);
        binding.spinnerStatsTimeframe.setSelection(3);
        binding.spinnerStatsTimeframe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(
                    AdapterView<?> parent, View view, int position, long id) {
                SetUserStats(user);
            }

            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });
    }

    private void SetUserStats(FirebaseUser user) {
        String selectedTimeframe =
                binding.spinnerStatsTimeframe.getSelectedItem().toString();
        Query userLogs = getQueryForTimeFrame(selectedTimeframe, user);
        TextView numberOfHikes = binding.userNumberOfHikes;
        TextView totalDistance = binding.userTotalDistance;
        userLogs.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                GetLoggedHikesController getLoggedHikesController = new GetLoggedHikesController();
                List<CustomLoggedHikeDTO> loggedHikes = getLoggedHikesController.getLoggedHikesFromDocuments(task.getResult().getDocuments());
                ProfileStatsService profileStatsService = new ProfileStatsService();
                ProfileStatsDTO profileStats = profileStatsService.getUserStats(loggedHikes);
                numberOfHikes.setText(String.format("%s Hikes", profileStats.getNumberOfHikes()));
                totalDistance.setText(String.format("%skm", profileStats.getTotalDistance()));
            }
        });
    }

    private void SetUpSaveButton() {
        binding.buttonSaveProfile.setOnClickListener((v) -> {
            String selectedHometown = binding.spinnerHometown.getSelectedItem().toString();
            if (!selectedHometown.equals("")) {
                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                FirebaseDatabase database = new FirebaseDatabase();
                database.updateUserHometown(user.getUid(), selectedHometown, getContext());
            }
        });
    }

    private Query getQueryForTimeFrame(String selectedTimeframe, FirebaseUser user) {
        LocalDateTime currentDate = LocalDateTime.now();
        switch (selectedTimeframe) {
            case "Past 7 Days":
                return firestore.collection("Users").document(user.getUid())
                                 .collection("Logs")
                                 .whereGreaterThan("date", currentDate.minusDays(7)
                                                                      .toInstant(ZoneOffset.UTC)
                                                                      .toEpochMilli());
            case "Past 30 Days":
                return firestore.collection("Users").document(user.getUid())
                                 .collection("Logs")
                                 .whereGreaterThan("date", currentDate.minusDays(30)
                                                                      .toInstant(ZoneOffset.UTC)
                                                                      .toEpochMilli());
            case "Past Year":
                return firestore.collection("Users").document(user.getUid())
                                 .collection("Logs")
                                 .whereGreaterThan("date", currentDate.minusYears(1)
                                                                      .toInstant(ZoneOffset.UTC)
                                                                      .toEpochMilli());
            default:
                return firestore.collection("Users").document(user.getUid())
                                 .collection("Logs");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
