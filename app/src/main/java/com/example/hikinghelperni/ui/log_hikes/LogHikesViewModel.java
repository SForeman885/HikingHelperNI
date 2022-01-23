package com.example.hikinghelperni.ui.log_hikes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LogHikesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public LogHikesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is log hikes fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}