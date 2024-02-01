package com.harshjais369.kitplus.ui.my_classmates;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.harshjais369.kitplus.databinding.FragmentMyClassmatesBinding;

public class MyClassmatesFragment extends Fragment {

    private FragmentMyClassmatesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MyClassmatesViewModel myClassmatesViewModel = new ViewModelProvider(this).get(MyClassmatesViewModel.class);

        binding = FragmentMyClassmatesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textSlideshow;
        myClassmatesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}