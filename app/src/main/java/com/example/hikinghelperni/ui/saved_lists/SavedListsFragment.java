package com.example.hikinghelperni.ui.saved_lists;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager.widget.ViewPager;

import com.example.hikinghelperni.R;
import com.example.hikinghelperni.SavedListsViewPagerAdapter;
import com.example.hikinghelperni.databinding.FragmentSavedListsBinding;
import com.example.hikinghelperni.ui.saved_times.SavedTimesFragment;
import com.example.hikinghelperni.ui.saved_trails.SavedTrailsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

public class SavedListsFragment extends Fragment {

    private FragmentSavedListsBinding binding;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSavedListsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        tabLayout = binding.savedListsTabLayout;
        viewPager = binding.savedListsViewPager;

        tabLayout.setupWithViewPager(viewPager);

        SavedListsViewPagerAdapter pagerAdapter = new SavedListsViewPagerAdapter(getChildFragmentManager(), SavedListsViewPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        pagerAdapter.addFragment(new SavedTrailsFragment(), "Saved Trails");
        pagerAdapter.addFragment(new SavedTimesFragment(), "Saved Hike Recommendations");
        viewPager.setAdapter(pagerAdapter);

        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}