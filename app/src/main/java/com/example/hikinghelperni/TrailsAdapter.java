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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Locale;

public class TrailsAdapter extends RecyclerView.Adapter<TrailsAdapter.ViewHolder>{

    private List<TrailListDTO> mTrails;
    private TrailsViewModel trailsViewModel;

    FragmentManager fragmentManager;

    public  TrailsAdapter(Context context, FragmentManager _fragmentManager, List<TrailListDTO> trails) {
        mTrails = trails;
        trailsViewModel = new ViewModelProvider((FragmentActivity) context).get(TrailsViewModel.class);
        fragmentManager = _fragmentManager;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // a member variable for each view that will be set as you render a row
        public View trailLayoutView;
        public TextView nameTextView, locationTextView, difficultyTextView, lengthTextView;
        public ImageView trailImageView;

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
        nameTextView.setText(trail.getTrailName());
        TextView locationTextView = holder.locationTextView;
        locationTextView.setText(trail.getLocation());
        TextView lengthTextView = holder.lengthTextView;
        lengthTextView.setText(String.format("%skm", trail.getLength()));
        TextView difficultyView = holder.difficultyTextView;
        difficultyView.setText(trail.getDifficulty().toLowerCase(Locale.ROOT));
        //Set colour and appearance of difficulty indicator
        if (trail.getDifficulty().equalsIgnoreCase("easy")) {
            difficultyView.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.difficulty_icon_easy));
        }
        else if (trail.getDifficulty().equalsIgnoreCase("medium")) {
            difficultyView.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.difficulty_icon_medium));
        }
        else {
            difficultyView.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.difficulty_icon_challenging));
        }

        ImageView trailImageView = holder.trailImageView;
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("trailImages").child(trail.getImageLink());
        //Glide with firebaseui allows us to directly access and download image files from firebase storage
        //And place them into our image view
        GlideApp.with(holder.trailImageView.getContext())
                .load(imageRef)
                .into(trailImageView);
    }

    @Override
    public int getItemCount() { return mTrails.size(); }
}
