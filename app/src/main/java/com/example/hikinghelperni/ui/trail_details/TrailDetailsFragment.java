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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
