package com.example.hikinghelperni.ui.saved_lists;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SavedListsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SavedListsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is saved lists fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}