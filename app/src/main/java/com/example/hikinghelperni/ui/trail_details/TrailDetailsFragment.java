package com.example.hikinghelperni.ui.trail_details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikinghelperni.BuildConfig;
import com.example.hikinghelperni.FirebaseDatabase;
import com.example.hikinghelperni.forecast.ForecastApiResponse;
import com.example.hikinghelperni.forecast.ForecastService;
import com.example.hikinghelperni.dto.ForecastWithHikeTimeSuggestionDTO;
import com.example.hikinghelperni.services.GetTrailsController;
import com.example.hikinghelperni.GlideApp;
import com.example.hikinghelperni.R;
import com.example.hikinghelperni.dto.TrailDetailsDTO;
import com.example.hikinghelperni.forecast.TrailDetailsForecastAdapter;
import com.example.hikinghelperni.dto.TrailListDTO;
import com.example.hikinghelperni.services.TrailTimeEstimationService;
import com.example.hikinghelperni.databinding.FragmentTrailDetailsBinding;
import com.example.hikinghelperni.ui.log_hikes.LogHikesFragment;
import com.example.hikinghelperni.ui.trails.TrailsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TrailDetailsFragment extends Fragment {

    private FragmentTrailDetailsBinding binding;
    private TrailsViewModel trailsViewModel;

    private FirebaseFirestore firestore;
    private FirebaseAuth mFirebaseAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        trailsViewModel = new ViewModelProvider((FragmentActivity)this.getContext()).get(TrailsViewModel.class);
        String trailId = trailsViewModel.getMTrailId();

        firestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        DocumentReference trailRef = firestore.collection("Trails").document(trailId);
        GetTrailsController getTrailsController = new GetTrailsController();

        trailRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot retrievedDocument = task.getResult();
                TrailDetailsDTO trailDetails =
                        getTrailsController.getTrailDetailsFromDocument(retrievedDocument);
                trailsViewModel.setMSelectedTrailDetails(trailDetails);
                displayTrailDetails(trailDetails);
            }
        });

        binding = FragmentTrailDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setHasOptionsMenu(true);
        ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        //set back icon and make visible on left of action bar
        ab.setHomeAsUpIndicator(R.drawable.ic_back_arrow_black_24);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Trail Details");

        return root;
    }

    private void displayTrailDetails(TrailDetailsDTO trailDetails) {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("trailImages").child(trailDetails.getImageLink());
        GlideApp.with(getContext())
                .load(imageRef)
                .into(binding.trailDetailsImage);
        binding.trailDetailsName.setText(trailDetails.getTrailName());
        TextView difficultyView = binding.trailDetailsDifficulty;
        difficultyView.setText(trailDetails.getDifficulty().toLowerCase());
        if (trailDetails.getDifficulty().equalsIgnoreCase("easy")) {
            difficultyView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.difficulty_icon_easy));
        }
        else if (trailDetails.getDifficulty().equalsIgnoreCase("medium")) {
            difficultyView.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.difficulty_icon_medium));
        }
        else {
            difficultyView.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.difficulty_icon_challenging));
        }
        binding.trailDetailsLocation.setText(trailDetails.getLocation());
        binding.trailDetailsRouteType.setText(trailDetails.getRouteType());
        binding.expandTextView.setText(trailDetails.getDescription());
        binding.trailDetailsLength.setText(String.format("%s", trailDetails.getLength()));
        binding.trailDetailsElevation.setText(String.format("%s", trailDetails.getElevation()));
        StorageReference mapRef = storageRef.child("trailMaps").child(trailDetails.getMapLink());
        GlideApp.with(getContext())
                .load(mapRef)
                .into(binding.trailDetailsRouteMapImage);
        getForecastDataAndHikeRecommendation(user, trailDetails.getLatitude(), trailDetails.getLongitude());
        if(user != null) {
            SetUpOptionsButton(user, trailDetails);
        }
        else {
            binding.trailOptionsButton.setVisibility(View.GONE);
        }
    }

    private void getForecastDataAndHikeRecommendation(FirebaseUser user, Double latitude, Double longitude) {
        RecyclerView rvForecast = binding.trailDetailsForecastRecyclerView;
        rvForecast.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false));
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("lat", latitude.toString());
        parameters.put("lon", longitude.toString());
        parameters.put("units", "metric");
        parameters.put("exclude", "current,minutely,hourly,alerts");
        parameters.put("appid", BuildConfig.FORECAST_API_KEY);
        ForecastService service = retrofit.create(ForecastService.class);

        // Calling weather api for our trail location
        Call<ForecastApiResponse> call = service.getForecast(parameters);

        call.enqueue(new Callback<ForecastApiResponse>() {
            @Override
            public void onResponse(Call<ForecastApiResponse> call,
                                   Response<ForecastApiResponse> response) {
                ForecastApiResponse apiResponse = response.body();
                TrailDetailsForecastAdapter adapter = new TrailDetailsForecastAdapter(apiResponse.getDaily());
                rvForecast.setAdapter(adapter);
                displayHikeTimeRecommendation(user, firestore,
                        trailsViewModel.getMSelectedTrailDetails(), apiResponse);
            }

            @Override
            public void onFailure(Call<ForecastApiResponse> call, Throwable t) {
                System.out.println("Failed to get forecast for trail");
            }
        });
    }

    private void displayHikeTimeRecommendation(FirebaseUser user, FirebaseFirestore firestore, TrailDetailsDTO trailDetails, ForecastApiResponse forecastResponse) {

        TrailTimeEstimationService trailTimeEstimationService = new TrailTimeEstimationService();
        if(user != null) {
            DocumentReference getUserDetails = firestore.collection("Users").document(user.getUid());
            getUserDetails.get().addOnCompleteListener(task -> {
                Map<String, Object> userData = task.getResult().getData();
                if (userData.containsKey("averageSpeed")) {
                    ForecastWithHikeTimeSuggestionDTO trailForecastAndTimeSuggestion = trailTimeEstimationService.getDateSuggestionForTrail(trailDetails, (Double) task.getResult().get("averageSpeed"), forecastResponse);
                    setRecommendationDetails(trailForecastAndTimeSuggestion);
                    setDisplayCustomized();
                    SetUpSaveSuggestionButton(trailForecastAndTimeSuggestion);
                } else {
                    ForecastWithHikeTimeSuggestionDTO trailForecastAndTimeSuggestion = trailTimeEstimationService.getUncustomizedDateSuggestionForTrail(trailDetails, forecastResponse);
                    setRecommendationDetails(trailForecastAndTimeSuggestion);
                    setDisplayUncustomized();
                    binding.trailDetailsTimeRecommendation.trailTimeSaveButton.setVisibility(View.GONE);
                }
            });
        }
        else {
            ForecastWithHikeTimeSuggestionDTO trailForecastAndTimeSuggestion = trailTimeEstimationService.getUncustomizedDateSuggestionForTrail(trailDetails, forecastResponse);
            setRecommendationDetails(trailForecastAndTimeSuggestion);
            setDisplayUncustomized();
            binding.trailDetailsTimeRecommendation.trailTimeSaveButton.setVisibility(View.GONE);
        }
    }

    private void setRecommendationDetails(ForecastWithHikeTimeSuggestionDTO trailForecastAndTimeSuggestion) {
        LocalDateTime dateRecommendation = LocalDateTime.ofEpochSecond(trailForecastAndTimeSuggestion.getHikeTimeSuggestion().getDateTime()/1000, 0, ZoneOffset.UTC);
        binding.trailDetailsTimeRecommendation.trailDetailsRecommendedDate.setText(DateTimeFormatter.ofPattern("dd-MM-yyyy").format(dateRecommendation));
        LocalDateTime earliestTime = LocalDateTime.ofEpochSecond(trailForecastAndTimeSuggestion.getHikeTimeSuggestion().getEarliestHikeTime()/1000, 0, ZoneOffset.UTC);
        LocalDateTime latestTime = LocalDateTime.ofEpochSecond(trailForecastAndTimeSuggestion.getHikeTimeSuggestion().getLatestHikeTime()/1000, 0, ZoneOffset.UTC);
        String earliestTimeString = DateTimeFormatter.ofPattern("HH:mm").format(earliestTime);
        String latestTimeString = DateTimeFormatter.ofPattern("HH:mm").format(latestTime);
        binding.trailDetailsTimeRecommendation.trailDetailsRecommendedTimesStart.setText(String.format("Earliest start of hike: %s", earliestTimeString));
        binding.trailDetailsTimeRecommendation.trailDetailsRecommendedTimesEnd.setText(String.format("Latest start of hike: %s", latestTimeString));
        int timeEstimateHours = (int) (trailForecastAndTimeSuggestion.getHikeTimeSuggestion().getUserTimeEstimate() / 60);
        int timeEstimateMinutes = (int) (trailForecastAndTimeSuggestion.getHikeTimeSuggestion().getUserTimeEstimate() % 60);
        binding.trailDetailsTimeRecommendation.trailDetailsRecommendedTimeEstimate.setText(String.format("Time to complete: %s hours %s mins", timeEstimateHours, timeEstimateMinutes));
        String iconURI = String.format("%s:drawable/weather_icon_%s", getContext().getPackageName(), trailForecastAndTimeSuggestion.getSuggestionForecast().getWeather().get(0).getIcon());
        int imageResource = getContext().getResources().getIdentifier(iconURI, null, null);
        binding.trailDetailsTimeRecommendation.recommendationForecastIconImage.setImageDrawable(getContext().getDrawable(imageResource));
        binding.trailDetailsTimeRecommendation.recommendationForecastIconImage.setBackgroundColor(getResources().getColor(R.color.white, null));
    }

    private void setDisplayUncustomized() {
        FloatingActionButton recommendationInfoIcon = binding.recommendationInfoUncustomizedIcon;
        recommendationInfoIcon.setVisibility(View.VISIBLE);
        TooltipCompat.setTooltipText(recommendationInfoIcon, getResources().getString(R.string.trail_details_recommended_not_customized_tooltip));
    }

    private void setDisplayCustomized() {
        FloatingActionButton recommendationInfoIcon = binding.recommendationInfoCustomizedIcon;
        recommendationInfoIcon.setVisibility(View.VISIBLE);
        TooltipCompat.setTooltipText(recommendationInfoIcon, getResources().getString(R.string.trail_details_recommended_customized_tooltip));
    }

    private void SetUpOptionsButton(FirebaseUser user, TrailDetailsDTO trailDetails) {
        binding.trailOptionsButton.setOnClickListener((v) -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.trail_details_top_popup, popupMenu.getMenu());
            popupMenu.show();
            //query to check for this trail previously being saved to db
            Query savedTrailQuery = firestore.collection("Users").document(user.getUid())
                                             .collection("Saved Trails")
                                             .whereEqualTo("id", trailDetails.getId());
            savedTrailQuery.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    if(!task.getResult().getDocuments().isEmpty()) {
                        popupMenu.getMenu().getItem(1).setTitle("Unsave Trail");
                    }
                    popupMenu.setOnMenuItemClickListener((view) -> {
                        if (view.getItemId() == R.id.log_hike_to_trail) {
                            //navigate to the log hikes page which will accept the trail id in the viewmodel
                            LogHikesFragment nextFragment = new LogHikesFragment();
                            FragmentManager fragmentManager = getParentFragmentManager();
                            fragmentManager.beginTransaction()
                                           .replace(R.id.nav_host_fragment_activity_main, nextFragment)
                                           .commit();
                            return true;
                        } else if (view.getItemId() == R.id.save_trail) {
                            FirebaseDatabase db = new FirebaseDatabase();
                            if(task.getResult().getDocuments().isEmpty()) {
                                TrailListDTO trailToSave = new TrailListDTO(trailDetails.getId(), trailDetails.getTrailName(), trailDetails.getImageLink(), trailDetails.getLocation(), trailDetails.getLatitude(), trailDetails.getLongitude(), trailDetails.getLength(), trailDetails.getDifficulty());
                                db.addNewSavedTrail(trailToSave.LogMapper(), user.getUid(), getContext());
                            }
                            else {
                                db.deleteSavedTrail(task.getResult().getDocuments().get(0).getId(), user.getUid(), getContext());
                            }
                            return true;
                        }
                        return false;
                    });
                }
            });
        });
    }

    private void SetUpSaveSuggestionButton(ForecastWithHikeTimeSuggestionDTO timeSuggestion) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Query savedTrailQuery = firestore.collection("Users").document(user.getUid())
                                         .collection("Saved Times")
                                         .whereEqualTo("trailId", timeSuggestion.getHikeTimeSuggestion().getTrailId())
                                         .whereEqualTo("dateTime", timeSuggestion.getHikeTimeSuggestion().getDateTime());
        savedTrailQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseDatabase db = new FirebaseDatabase();
                if (task.getResult().getDocuments().isEmpty()) {
                    binding.trailDetailsTimeRecommendation.trailTimeSaveButton.setVisibility(View.VISIBLE);
                    binding.trailDetailsTimeRecommendation.trailTimeUnsaveButton.setVisibility(View.GONE);
                    binding.trailDetailsTimeRecommendation.trailTimeSaveButton.setOnClickListener((v) -> {
                        db.addNewSavedTime(timeSuggestion.getHikeTimeSuggestion().LogMapper(), user.getUid(), v.getContext());
                        SetUpSaveSuggestionButton(timeSuggestion);
                    });
                }
                else {
                    binding.trailDetailsTimeRecommendation.trailTimeSaveButton.setVisibility(View.GONE);
                    binding.trailDetailsTimeRecommendation.trailTimeUnsaveButton.setVisibility(View.VISIBLE);
                    binding.trailDetailsTimeRecommendation.trailTimeUnsaveButton.setOnClickListener((v) -> {
                        db.deleteSavedTime(task.getResult().getDocuments().get(0).getId(), user.getUid(), v.getContext());
                        SetUpSaveSuggestionButton(timeSuggestion);
                    });
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
