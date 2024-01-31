package com.harshjais369.kitplus.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.harshjais369.kitplus.R;
import com.harshjais369.kitplus.databinding.FragmentHomeBinding;
import com.harshjais369.kitplus.MainActivity;

import org.json.JSONObject;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        setLastVisit();

        return view;
    }

    void setLastVisit() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) return;
        String url = getString(R.string.erp_domain) + "/Home.aspx/getLastVisit";
        String method = "POST";
        String params = "";
        new Thread(() -> {
            String resp = mainActivity.sendRequest(url, method, params);
            mainActivity.runOnUiThread(() -> {
                if (resp == null) {
                    Toast.makeText(mainActivity.getApplicationContext(), "Failed to fetch response from server",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                String[] respArr = resp.split(":::");
                if (!respArr[0].equals("200")) {
                    Toast.makeText(mainActivity.getApplicationContext(), "Error: Server returned code " + respArr[0],
                            Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    String lastVisit_html = new JSONObject(respArr[1]).getString("d");
                    TextView textView = binding.textHome;
                    textView.setText("Last visit: " + convertHtmlToText(lastVisit_html));
                } catch (Exception e) {
                    Toast.makeText(mainActivity.getApplicationContext(), "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    String convertHtmlToText(String htmlSnippet) {
        // Remove HTML tags using regular expression
        String text = htmlSnippet.replaceAll("<[^>]*>", "");
        // Remove extra whitespaces and trim the result
        text = text.replaceAll("\\s+", " ").trim();
        return text;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}