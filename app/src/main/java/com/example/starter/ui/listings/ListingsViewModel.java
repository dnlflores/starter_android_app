package com.example.starter.ui.listings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ListingsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ListingsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is listings fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
} 