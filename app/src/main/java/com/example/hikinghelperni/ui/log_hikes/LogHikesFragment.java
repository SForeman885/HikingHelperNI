package com.example.hikinghelperni.ui.log_hikes;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.hikinghelperni.CustomLoggedHikeDTO;
import com.example.hikinghelperni.FirebaseDatabase;
import com.example.hikinghelperni.GetLoggedHikesController;
import com.example.hikinghelperni.LogHikesValidator;
import com.example.hikinghelperni.R;
import com.example.hikinghelperni.databinding.FragmentLogHikesBinding;
import com.example.hikinghelperni.ui.view_logs.ViewLogsFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class LogHikesFragment extends Fragment {

    private FragmentLogHikesBinding binding;
    private FirebaseDatabase db;
    private FirebaseAuth mFirebaseAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LogHikesViewModel logHikesViewModel = new ViewModelProvider(this).get(LogHikesViewModel.class);

        binding = FragmentLogHikesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mFirebaseAuth = FirebaseAuth.getInstance();
        SetUpDateControl();
        SetUpDifficultyDropDown();
        SetUpTrailTypeDropDown();
        SetUpSubmitButton();
        setHasOptionsMenu(true);
        ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        //set back icon and make visible on left of action bar
        ab.setHomeAsUpIndicator(R.drawable.ic_back_arrow_black_24);
        ab.setDisplayHomeAsUpEnabled(true);
        return root;
    }

    private void SetUpDateControl() {
        binding.editTextDateField.setInputType(InputType.TYPE_NULL);
        binding.dateCalendar.setVisibility(View.GONE);
        binding.dateCalendar.setMaxDate(System.currentTimeMillis() - 1000);
        //when the date textbox is clicked show the date control
        binding.editTextDateField.setOnFocusChangeListener((v, hasFocus) -> {
            binding.dateCalendar.setVisibility(hasFocus? View.VISIBLE : View.GONE);
            if(hasFocus) {
                InputMethodManager imm = (InputMethodManager)this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
            }
        });
        //when the user changes the date display it on the above textbox
        binding.dateCalendar.setOnDateChangeListener((v, year, month, day) -> {
            String Year = String.valueOf(year);
            month++;
            String Month = String.valueOf(month).length() > 1? String.valueOf(month) : "0" + month;
            String Day = String.valueOf(day).length() > 1? String.valueOf(day) : "0" + day;
            binding.editTextDateField.setText(String.format("%s/%s/%s", Day, Month, Year));
        });
    }

    private void SetUpDifficultyDropDown() {
        //filling in trail difficulty control with array adapter to allow dropdown
        String[] difficultyItems = new String[]{"Trail Difficulty", "Easy", "Medium", "Challenging"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(), R.layout.custom_dropdown, difficultyItems);
        binding.spinnerDifficulty.setAdapter(adapter);
    }

    private void SetUpTrailTypeDropDown() {
        //filling in trail type control with array adapter to allow dropdown
        String[] trailTypeItems = new String[]{"Type Of Trail", "Mountain", "Hill", "Park/Beach"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(), R.layout.custom_dropdown, trailTypeItems);
        binding.spinnerTrailType.setAdapter(adapter);
    }

    private void SetUpSubmitButton() {
        binding.buttonSubmitLog.setOnClickListener((v) -> {
            db = new FirebaseDatabase();
            FirebaseUser user = mFirebaseAuth.getCurrentUser();
            if(user != null) {
                LogHikesValidator validator = new LogHikesValidator();
                String trailName = binding.editTextTrailName.getText().toString();
                Long date = binding.dateCalendar.getDate();
                String length = binding.editTextNumberTrailLength.getText().toString();
                String hours = binding.editTextNumberHours.getText().toString();
                String minutes = binding.editTextNumberMinutes.getText().toString();
                String difficulty = binding.spinnerDifficulty.getSelectedItem().toString();
                String trailType = binding.spinnerTrailType.getSelectedItem().toString();
                Map<Integer, String> validatorResponse = validator.validateCustomLog(trailName, date, length, hours, minutes);
                if (!validatorResponse.isEmpty()) {
                    for (Integer key : validatorResponse.keySet()) {
                        EditText textView = v.getRootView().findViewById(key);
                        textView.setError(validatorResponse.get(key));
                    }
                } else {
                    int hoursValue = hours.isEmpty() ? 0 : Integer.parseInt(hours); //this will handle an empty hours/minutes input from user
                    int minutesValue = minutes.isEmpty() ? 0 : Integer.parseInt(minutes);
                    int timeTaken = (hoursValue * 60) + minutesValue;
                    //if none selected autoselect medium as this option will not affect the speed calculation
                    if (difficulty.equals("Trail Difficulty")) {
                        difficulty = "Medium";
                    }
                    double elevation = getAveragedTrailElevation(trailType);

                    CustomLoggedHikeDTO log = new CustomLoggedHikeDTO(trailName, date, Double.parseDouble(length), elevation, timeTaken, difficulty);
                    LogHikeAfterCheckingForExistingLogInDB(log, user, v);
                }
            }
            else {
                Snackbar snackbar = Snackbar
                        .make(v, "Please Sign In to Log a Hike", Snackbar.LENGTH_LONG);
                snackbar.show();
            }

        });
    }

    public void LogHikeAfterCheckingForExistingLogInDB(CustomLoggedHikeDTO userLog, FirebaseUser user, View v) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference getLogs = firestore.collection("Users").document(user.getUid()).collection("Logs");
        GetLoggedHikesController getLoggedHikesController = new GetLoggedHikesController();
        getLogs.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> retrievedDocuments = task.getResult().getDocuments();
                if (!retrievedDocuments.isEmpty()) {
                    //Check if the user already has a log saved with the same name
                    List<CustomLoggedHikeDTO> customLoggedHikeDTOList = getLoggedHikesController.getLoggedHikesFromDocuments(retrievedDocuments);
                    if(!customLoggedHikeDTOList.stream().anyMatch(log -> log.getTrailName().equals(userLog.getTrailName()))) {
                        //logMapper() is used to convert the object into a Map that Firebase will accept
                        db.addNewCustomLog(userLog.LogMapper(), user.getUid());
                        ViewLogsFragment nextFragment = new ViewLogsFragment();
                        FragmentManager fragmentManager = getParentFragmentManager();
                        fragmentManager.popBackStack();
                        fragmentManager.beginTransaction()
                                .replace(R.id.nav_host_fragment_activity_main, nextFragment)
                                .commit();
                    }
                    else {
                        EditText textView = v.getRootView().findViewById(R.id.editTextTrailName);
                        textView.setError("Please choose a unique name for the trail");
                    }
                }
            } else {
                Log.d(this.getClass().toString(), "getting logs failed with ", task.getException());
            }
        });
    }

    //when a user enters the type of trail we use this to save the average elevation of trails
    //of this type to use in calculations
    private double getAveragedTrailElevation(String trailType) {
        if(trailType.equals("Mountain")) {
            return 700;
        }
        else if(trailType.equals("Hill")) {
            return 150;
        }
        else {
            return 0;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}