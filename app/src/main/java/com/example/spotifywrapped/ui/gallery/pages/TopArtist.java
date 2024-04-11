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
import com.example.spotifywrapped.databinding.FragmentTopArtistBinding;
import com.example.spotifywrapped.databinding.FragmentTopSongBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class TopArtist extends Fragment {

    private FragmentTopArtistBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTopArtistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set the click listener for the button
        binding.topartistnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_top5Artists);
            }
        });
        binding.topartistback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_top5Songs);
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