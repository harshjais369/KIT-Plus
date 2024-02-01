package com.harshjais369.kitplus.ui.my_classmates;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MyClassmatesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MyClassmatesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is classmates fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}