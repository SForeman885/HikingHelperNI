package com.example.hikinghelperni;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikinghelperni.forecast.ForecastApiResponse;
import com.example.hikinghelperni.forecast.ForecastService;
import com.example.hikinghelperni.ui.trail_details.TrailDetailsFragment;
import com.example.hikinghelperni.ui.trails.TrailsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TimeRecommendationsAdapter extends RecyclerView.Adapter<TimeRecommendationsAdapter.ViewHolder> {

    public List<TrailHikeTimeSuggestionDTO> mRecommendedTimes;

    private TrailsViewModel trailsViewModel;

    private FragmentManager fragmentManager;

    public TimeRecommendationsAdapter(Context context, FragmentManager _fragmentManager,
                                      List<TrailHikeTimeSuggestionDTO> recommendedTimes) {
        mRecommendedTimes = recommendedTimes;
        fragmentManager = _fragmentManager;
        trailsViewModel =
                new ViewModelProvider((FragmentActivity) context).get(TrailsViewModel.class);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView dateTextView, earliestTimeTextView, latestTimeTextView, timeEstimateTextView;
        ImageView forecastImage;
        public FloatingActionButton saveFab, deleteTimeFab;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.trail_details_recommended_date);
            earliestTimeTextView =
                    itemView.findViewById(R.id.trail_details_recommended_times_start);
            latestTimeTextView = itemView.findViewById(R.id.trail_details_recommended_times_end);
            timeEstimateTextView =
                    itemView.findViewById(R.id.trail_details_recommended_time_estimate);
            forecastImage = itemView.findViewById(R.id.recommendation_forecast_icon_image);
            itemView.setOnClickListener(v -> {
                trailsViewModel.setMTrailId(mRecommendedTimes.get(getAdapterPosition()).getTrailId());
                TrailDetailsFragment nextFragment = new TrailDetailsFragment();
                fragmentManager.beginTransaction()
                               .replace(R.id.nav_host_fragment_activity_main, nextFragment)
                               .addToBackStack("SavedTimesFragment")
                               .commit();
            });
            saveFab = itemView.findViewById(R.id.trail_time_save_button);
            deleteTimeFab = itemView.findViewById(R.id.trail_time_delete_button);
        }
    }

    @Override
    public TimeRecommendationsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                    int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View recommendedTimeView = inflater.inflate(R.layout.trail_time_recommendation_item,
                parent, false);
        return new ViewHolder(recommendedTimeView);
    }

    @Override
    public void onBindViewHolder(TimeRecommendationsAdapter.ViewHolder holder, int position) {
        TrailHikeTimeSuggestionDTO recommendedTime = mRecommendedTimes.get(position);

        // Set item views based on your views and data model
        TextView dateTextView = holder.dateTextView;
        DateFormat simple = DateFormat.getDateInstance();
        Date date = new Date(recommendedTime.getDateTime());
        dateTextView.setText(simple.format(date));
        TextView earliestTimeTextView = holder.earliestTimeTextView;
        TextView latestTimeTextView = holder.latestTimeTextView;
        LocalDateTime earliestTime =
                LocalDateTime.ofEpochSecond(recommendedTime.getEarliestHikeTime(), 0,
                        ZoneOffset.UTC);
        LocalDateTime latestTime =
                LocalDateTime.ofEpochSecond(recommendedTime.getLatestHikeTime(), 0, ZoneOffset.UTC);
        String earliestTimeString = DateTimeFormatter.ofPattern("HH:mm").format(earliestTime);
        String latestTimeString = DateTimeFormatter.ofPattern("HH:mm").format(latestTime);
        earliestTimeTextView.setText(earliestTimeString);
        latestTimeTextView.setText(latestTimeString);
        TextView timeEstimateTextView = holder.timeEstimateTextView;
        int timeEstimateHours = (int) (recommendedTime.getUserTimeEstimate() / 60);
        int timeEstimateMinutes = (int) (recommendedTime.getUserTimeEstimate() % 60);
        timeEstimateTextView.setText(String.format("Time to complete: %s hours %s mins",
                timeEstimateHours, timeEstimateMinutes));
        ImageView iconImageView = holder.forecastImage;

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("lat", recommendedTime.getLatitude().toString());
        parameters.put("lon", recommendedTime.getLongitude().toString());
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
                apiResponse.getDaily().stream().filter(forecast -> forecast.getDt() == (recommendedTime.getDateTime() / 1000));
                String icon = apiResponse.getDaily().get(0).getWeather().get(0).getIcon();
                String iconURI = String.format("%s:drawable/weather_icon_%s",
                        holder.itemView.getContext().getPackageName(), icon);
                int imageResource =
                        holder.itemView.getContext().getResources().getIdentifier(iconURI, null,
                                null);
                iconImageView.setImageDrawable(holder.itemView.getContext().getDrawable(imageResource));
                iconImageView.setBackgroundColor(holder.itemView.getResources().getColor(R.color.white, null));
            }

            @Override
            public void onFailure(Call<ForecastApiResponse> call, Throwable t) {
                System.out.println("Failed to get forecast for trail");
            }
        });
        SetUpDeleteButton(holder, recommendedTime, position);
    }

    private void SetUpDeleteButton(TimeRecommendationsAdapter.ViewHolder holder,
                                   TrailHikeTimeSuggestionDTO recommendedTime, int position) {
        FirebaseDatabase db = new FirebaseDatabase();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        holder.saveFab.setVisibility(View.GONE);
        holder.deleteTimeFab.setVisibility(View.VISIBLE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Query savedTrailQuery = firestore.collection("Users").document(user.getUid())
                                         .collection("Saved Times")
                                         .whereEqualTo("trailId", recommendedTime.getTrailId())
                                         .whereEqualTo("date", recommendedTime.getDateTime());
        savedTrailQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                holder.deleteTimeFab.setOnClickListener((v) -> {
                    db.deleteSavedTimeFromList(task.getResult().getDocuments().get(0).getId(),
                            user.getUid(), v.getContext(), this, position);
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return mRecommendedTimes.size();
    }
}
