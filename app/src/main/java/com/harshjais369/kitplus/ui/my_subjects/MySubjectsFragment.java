package com.harshjais369.kitplus.ui.my_subjects;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.harshjais369.kitplus.databinding.FragmentMySubjectsBinding;

public class MySubjectsFragment extends Fragment {

    private FragmentMySubjectsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MySubjectsViewModel mySubjectsViewModel = new ViewModelProvider(this).get(MySubjectsViewModel.class);

        binding = FragmentMySubjectsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textGallery;
        mySubjectsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}