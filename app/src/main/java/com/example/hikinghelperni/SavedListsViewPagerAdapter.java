package com.example.hikinghelperni;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class SavedListsViewPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragmentsList = new ArrayList<>();
    private ArrayList<String> fragmentTitles = new ArrayList<>();

    public SavedListsViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    public void addFragment(Fragment fragment, String title) {
        fragmentsList.add(fragment);
        fragmentTitles.add(title);
    }

    @Override
    public String getPageTitle(int position) {

        return fragmentTitles.get(position);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragmentsList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentsList.size();
    }
}
