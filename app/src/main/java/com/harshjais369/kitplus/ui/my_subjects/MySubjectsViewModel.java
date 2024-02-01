package com.harshjais369.kitplus.ui.my_subjects;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MySubjectsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MySubjectsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is subjects fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}