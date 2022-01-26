package com.example.hikinghelperni.ui.trails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TrailsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public TrailsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is trails fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}