package com.example.hikinghelperni;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikinghelperni.ui.trail_details.TrailDetailsFragment;
import com.example.hikinghelperni.ui.trails.TrailsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Locale;

public class TrailsAdapter extends RecyclerView.Adapter<TrailsAdapter.ViewHolder> {

    public List<TrailListDTO> mTrails;
    private TrailsViewModel trailsViewModel;
    private String currentFragment;

    private FragmentManager fragmentManager;

    public TrailsAdapter(Context context, FragmentManager _fragmentManager,
                         List<TrailListDTO> trails, String _currentFragment) {
        mTrails = trails;
        trailsViewModel =
                new ViewModelProvider((FragmentActivity) context).get(TrailsViewModel.class);
        fragmentManager = _fragmentManager;
        currentFragment = _currentFragment;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // a member variable for each view that will be set as you render a row
        public View trailLayoutView;
        public TextView nameTextView, locationTextView, difficultyTextView, lengthTextView;
        public ImageView trailImageView;
        public FloatingActionButton saveTrailFab, unsaveTrailFab, deleteSavedTrailFab;

        // constructor that accepts the entire item row and finds each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            trailLayoutView = itemView.findViewById(R.id.trails_item_layout);
            trailImageView = itemView.findViewById(R.id.trail_image);
            nameTextView = itemView.findViewById(R.id.trail_name);
            locationTextView = itemView.findViewById(R.id.trail_location);
            lengthTextView = itemView.findViewById(R.id.trail_length);
            difficultyTextView = itemView.findViewById(R.id.trail_difficulty);
            saveTrailFab = itemView.findViewById(R.id.trail_list_save_button);
            unsaveTrailFab = itemView.findViewById(R.id.trail_list_unsave_button);
            deleteSavedTrailFab = itemView.findViewById(R.id.trail_list_delete_button);
            itemView.setOnClickListener(v -> {
                trailsViewModel.setMTrailId(mTrails.get(getAdapterPosition()).getId());
                TrailDetailsFragment nextFragment = new TrailDetailsFragment();
                fragmentManager.beginTransaction()
                               .replace(R.id.nav_host_fragment_activity_main, nextFragment)
                               .addToBackStack("TrailsFragment")
                               .commit();
            });
        }
    }

    @Override
    public TrailsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View trailView = inflater.inflate(R.layout.list_trails_item, parent, false);
        return new ViewHolder(trailView);
    }

    @Override
    public void onBindViewHolder(TrailsAdapter.ViewHolder holder, int position) {
        TrailListDTO trail = mTrails.get(position);
        View trailLayoutView = holder.trailLayoutView;
        trailLayoutView.setClipToOutline(true);

        // Set item views based on your views and data model
        TextView nameTextView = holder.nameTextView;
        nameTextView.setText(trail.getName());
        TextView locationTextView = holder.locationTextView;
        locationTextView.setText(trail.getLocationName());
        TextView lengthTextView = holder.lengthTextView;
        lengthTextView.setText(String.format("%skm", trail.getLength()));
        TextView difficultyView = holder.difficultyTextView;
        difficultyView.setText(trail.getDifficulty().toLowerCase());
        //Set colour and appearance of difficulty indicator
        if (trail.getDifficulty().equalsIgnoreCase("easy")) {
            difficultyView.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),
                    R.drawable.difficulty_icon_easy));
        } else if (trail.getDifficulty().equalsIgnoreCase("medium")) {
            difficultyView.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),
                    R.drawable.difficulty_icon_medium));
        } else {
            difficultyView.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),
                    R.drawable.difficulty_icon_challenging));
        }

        ImageView trailImageView = holder.trailImageView;
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("trailImages").child(trail.getImageLink());
        //Glide with firebaseui allows us to directly access and download image files from
        // firebase storage
        //And place them into our image view
        GlideApp.with(holder.trailImageView.getContext())
                .load(imageRef)
                .into(trailImageView);

        if(currentFragment.equals("Saved Trails")) {
            SetUpDeleteButton(holder, trail, position);
        }
        else {
            SetUpSaveButton(holder, trail);
        }
    }

    private void SetUpSaveButton(ViewHolder holder, TrailListDTO trail) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Query savedTrailQuery = firestore.collection("Users").document(user.getUid())
                                         .collection("Saved Trails")
                                         .whereEqualTo("id", trail.getId());
        savedTrailQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseDatabase db = new FirebaseDatabase();
                if (task.getResult().getDocuments().isEmpty()) {
                    holder.saveTrailFab.setVisibility(View.VISIBLE);
                    holder.unsaveTrailFab.setVisibility(View.GONE);
                    holder.saveTrailFab.setOnClickListener((v) -> {
                        db.addNewSavedTrail(trail.LogMapper(), user.getUid(), v.getContext());
                    });
                }
                else {
                    holder.saveTrailFab.setVisibility(View.GONE);
                    holder.unsaveTrailFab.setVisibility(View.VISIBLE);
                    holder.unsaveTrailFab.setOnClickListener((v) -> {
                        db.deleteSavedTrail(task.getResult().getDocuments().get(0).getId(), user.getUid(), v.getContext());
                    });
                }
                SetUpSaveButton(holder, trail);
            }
        });
    }

    public void SetUpDeleteButton(ViewHolder holder, TrailListDTO trail, int position) {
        holder.saveTrailFab.setVisibility(View.GONE);
        holder.unsaveTrailFab.setVisibility(View.GONE);
        holder.deleteSavedTrailFab.setVisibility(View.VISIBLE);
        FirebaseDatabase db = new FirebaseDatabase();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Query savedTrailQuery = firestore.collection("Users").document(user.getUid())
                                         .collection("Saved Trails")
                                         .whereEqualTo("id", trail.getId());
        savedTrailQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                holder.deleteSavedTrailFab.setOnClickListener((v) -> {
                    db.deleteSavedTrailFromList(task.getResult().getDocuments().get(0).getId(), user.getUid(), v.getContext(), this, position);
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTrails.size();
    }

    public TrailListDTO getItem(int position) {
        return mTrails.get(position);
    }

    public void clearItems() {
        mTrails.clear();
    }
}
