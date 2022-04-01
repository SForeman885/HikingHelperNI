package com.example.hikinghelperni.ui.trail_details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikinghelperni.ForecastApiResponse;
import com.example.hikinghelperni.ForecastService;
import com.example.hikinghelperni.ForecastWithHikeTimeSuggestionDTO;
import com.example.hikinghelperni.GetTrailsController;
import com.example.hikinghelperni.GlideApp;
import com.example.hikinghelperni.R;
import com.example.hikinghelperni.TrailDetailsDTO;
import com.example.hikinghelperni.TrailDetailsForecastAdapter;
import com.example.hikinghelperni.TrailHikeTimeSuggestionDTO;
import com.example.hikinghelperni.TrailTimeEstimationService;
import com.example.hikinghelperni.databinding.FragmentTrailDetailsBinding;
import com.example.hikinghelperni.ui.trails.TrailsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.sql.Time;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
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

        return root;
    }

    private void displayTrailDetails(TrailDetailsDTO trailDetails) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("trailImages").child(trailDetails.getImageLink());
        GlideApp.with(getContext())
                .load(imageRef)
                .into(binding.trailDetailsImage);
        binding.trailDetailsName.setText(trailDetails.getTrailName());
        TextView difficultyView = binding.trailDetailsDifficulty;
        difficultyView.setText(trailDetails.getDifficulty().toLowerCase(Locale.ROOT));
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
        binding.expandTextView.setText(trailDetails.getDescription());
        binding.trailDetailsLength.setText(String.format("%s", trailDetails.getLength()));
        binding.trailDetailsElevation.setText(String.format("%s", trailDetails.getElevation()));
        StorageReference mapRef = storageRef.child("trailMaps").child(trailDetails.getMapLink());
        GlideApp.with(getContext())
                .load(mapRef)
                .into(binding.trailDetailsRouteMapImage);
        getForecastDataAndHikeRecommendation(trailDetails.getLatitude(), trailDetails.getLongitude());
    }

    private void getForecastDataAndHikeRecommendation(Double latitude, Double longitude) {
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
        parameters.put("appid", "4887d49bf0dd5494e5bc48915c8bc36d");
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
                displayHikeTimeRecommendation(firestore, trailsViewModel.getMSelectedTrailDetails(), apiResponse);
            }

            @Override
            public void onFailure(Call<ForecastApiResponse> call, Throwable t) {
                System.out.println("Failed to get forecast for trail");
            }
        });
    }

    private void displayHikeTimeRecommendation(FirebaseFirestore firestore, TrailDetailsDTO trailDetails, ForecastApiResponse forecastResponse) {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        TrailTimeEstimationService trailTimeEstimationService = new TrailTimeEstimationService();
        DocumentReference getUserDetails = firestore.collection("Users").document(user.getUid());
        getUserDetails.get().addOnCompleteListener(task -> {
            Map<String, Object> userData = task.getResult().getData();
            if(userData.containsKey("averageSpeed")) {
                ForecastWithHikeTimeSuggestionDTO trailForecastAndTimeSuggestion = trailTimeEstimationService.getDateSuggestionForTrail(trailDetails, (Double) task.getResult().get("averageSpeed"), forecastResponse);
                setRecommendationDetails(trailForecastAndTimeSuggestion);
                setDisplayCustomized();
            }
            else {
                ForecastWithHikeTimeSuggestionDTO trailForecastAndTimeSuggestion = trailTimeEstimationService.getUncustomizedDateSuggestionForTrail(trailDetails, forecastResponse);
                setRecommendationDetails(trailForecastAndTimeSuggestion);
                setDisplayUncustomized();
            }
        });
    }

    private void setRecommendationDetails(ForecastWithHikeTimeSuggestionDTO trailForecastAndTimeSuggestion) {
        LocalDateTime dateRecommendation = LocalDateTime.ofEpochSecond(trailForecastAndTimeSuggestion.getHikeTimeSuggestion().getDateTime(), 0, ZoneOffset.UTC);
        binding.trailDetailsRecommendedDate.setText(DateTimeFormatter.ofPattern("dd-MM-yyyy").format(dateRecommendation));
        LocalDateTime earliestTime = LocalDateTime.ofEpochSecond(trailForecastAndTimeSuggestion.getHikeTimeSuggestion().getEarliestHikeTime(), 0, ZoneOffset.UTC);
        LocalDateTime latestTime = LocalDateTime.ofEpochSecond(trailForecastAndTimeSuggestion.getHikeTimeSuggestion().getLatestHikeTime(), 0, ZoneOffset.UTC);
        String earliestTimeString = DateTimeFormatter.ofPattern("HH:mm").format(earliestTime);
        String latestTimeString = DateTimeFormatter.ofPattern("HH:mm").format(latestTime);
        binding.trailDetailsRecommendedTimesStart.setText(String.format("Earliest start of hike: %s", earliestTimeString));
        binding.trailDetailsRecommendedTimesEnd.setText(String.format("Latest start of hike: %s", latestTimeString));
        int timeEstimateHours = (int) (trailForecastAndTimeSuggestion.getHikeTimeSuggestion().getUserTimeEstimate() / 60);
        int timeEstimateMinutes = (int) (trailForecastAndTimeSuggestion.getHikeTimeSuggestion().getUserTimeEstimate() % 60);
        binding.trailDetailsRecommendedTimeEstimate.setText(String.format("Time to complete: %s hours %s mins", timeEstimateHours, timeEstimateMinutes));
        String iconURI = String.format("%s:drawable/weather_icon_%s", getContext().getPackageName(), trailForecastAndTimeSuggestion.getSuggestionForecast().getWeather().get(0).getIcon());
        int imageResource = getContext().getResources().getIdentifier(iconURI, null, null);
        binding.recommendationForecastIconImage.setImageDrawable(getContext().getDrawable(imageResource));
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
