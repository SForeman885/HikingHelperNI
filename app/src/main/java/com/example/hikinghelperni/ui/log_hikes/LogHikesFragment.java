package com.example.hikinghelperni.ui.log_hikes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.hikinghelperni.R;
import com.example.hikinghelperni.databinding.FragmentLogHikesBinding;

public class LogHikesFragment extends Fragment {

    private FragmentLogHikesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LogHikesViewModel logHikesViewModel = new ViewModelProvider(this).get(LogHikesViewModel.class);

        binding = FragmentLogHikesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textLogHikes;
        logHikesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        setHasOptionsMenu(true);
        ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        //set back icon and make visible on left of action bar
        ab.setHomeAsUpIndicator(R.drawable.ic_back_arrow_black_24);
        ab.setDisplayHomeAsUpEnabled(true);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}