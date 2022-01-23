package com.example.hikinghelperni.ui.log_hikes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.hikinghelperni.databinding.FragmentLogHikesBinding;

public class LogHikesFragment extends Fragment {

    private FragmentLogHikesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LogHikesViewModel logHikesViewModel = new ViewModelProvider(this).get(LogHikesViewModel.class);

        binding = FragmentLogHikesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textLogHikes;
        logHikesViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}