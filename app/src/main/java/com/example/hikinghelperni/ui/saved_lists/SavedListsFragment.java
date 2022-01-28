package com.example.hikinghelperni.ui.saved_lists;

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

import com.example.hikinghelperni.databinding.FragmentSavedListsBinding;

public class SavedListsFragment extends Fragment {

    private FragmentSavedListsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SavedListsViewModel savedListsViewModel = new ViewModelProvider(this).get(SavedListsViewModel.class);

        binding = FragmentSavedListsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textSavedLists;
        savedListsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}