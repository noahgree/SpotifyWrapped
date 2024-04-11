package com.example.spotifywrapped.ui.gallery.pages;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentTopGenreBinding;
import com.example.spotifywrapped.databinding.FragmentWrappedSummaryBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class WrappedSummary extends Fragment {

    private FragmentWrappedSummaryBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWrappedSummaryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set the click listener for the button
        binding.wrappedsummaryback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_topGenre);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}