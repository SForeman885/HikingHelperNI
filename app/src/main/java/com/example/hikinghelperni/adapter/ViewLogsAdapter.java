package com.example.hikinghelperni.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikinghelperni.dto.CustomLoggedHikeDTO;
import com.example.hikinghelperni.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ViewLogsAdapter extends RecyclerView.Adapter<ViewLogsAdapter.ViewHolder>{

    private List<CustomLoggedHikeDTO> mLoggedHikes;

    public ViewLogsAdapter(List<CustomLoggedHikeDTO> loggedHikes) {
        mLoggedHikes = loggedHikes;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView, dateTextView, lengthTextView, timeTakenTextView, difficultyTextView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            nameTextView = itemView.findViewById(R.id.log_name);
            dateTextView = itemView.findViewById(R.id.log_date);
            lengthTextView = itemView.findViewById(R.id.log_length);
            timeTakenTextView = itemView.findViewById(R.id.log_time_taken);
            difficultyTextView = itemView.findViewById(R.id.log_difficulty);
        }
    }

    @Override
    public ViewLogsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View logsView = inflater.inflate(R.layout.view_logs_item, parent, false);
        return new ViewHolder(logsView);
    }

    // This populates data into each item through holder
    @Override
    public void onBindViewHolder(ViewLogsAdapter.ViewHolder holder, int position) {
        CustomLoggedHikeDTO log = mLoggedHikes.get(position);

        // Set item views based on your views and data model
        TextView nameTextView = holder.nameTextView;
        nameTextView.setText(log.getHikeName());
        TextView dateTextView = holder.dateTextView;
        DateFormat simple = DateFormat.getDateInstance();
        Date date = new Date(log.getDate());
        dateTextView.setText(simple.format(date));
        TextView lengthTextView = holder.lengthTextView;
        lengthTextView.setText(String.format("%skm", log.getLength()));
        TextView timeTakenView = holder.timeTakenTextView;
        //figure out if and how many hours fit into the timeTaken minutes value and set text based on this
        int noOfFullHours = log.getTimeTaken()/60;
        if(noOfFullHours > 1) {
            timeTakenView.setText(String.format("Time: %s hours %s minutes", noOfFullHours, (log.getTimeTaken() - (noOfFullHours * 60))));
        }
        else if(noOfFullHours == 1) {
            timeTakenView.setText(String.format("Time: %s hour %s minutes", noOfFullHours, (log.getTimeTaken() - (noOfFullHours * 60))));
        }
        else {
            timeTakenView.setText(String.format("Time: %s minutes", log.getTimeTaken()));
        }
        TextView difficultyView = holder.difficultyTextView;
        difficultyView.setText(log.getDifficulty().toLowerCase());
        //Set colour and appearance of difficulty indicator
        if (log.getDifficulty().equalsIgnoreCase("easy")) {
            difficultyView.setBackground(
                    ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.difficulty_icon_easy));
        }
        else if (log.getDifficulty().equalsIgnoreCase("medium")) {
            difficultyView.setBackground(
                    ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.difficulty_icon_medium));
        }
        else {
            difficultyView.setBackground(
                    ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.difficulty_icon_challenging));
        }
    }

    @Override
    public int getItemCount() {
        return mLoggedHikes.size();
    }
}
