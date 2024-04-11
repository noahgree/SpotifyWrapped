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
import com.example.spotifywrapped.databinding.FragmentTop5SongsBinding;
import com.example.spotifywrapped.databinding.FragmentTopSongBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class Top5Songs extends Fragment {

    private FragmentTop5SongsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTop5SongsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set the click listener for the button
        binding.top5songsnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_topArtist);
            }
        });

        binding.top5songsback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_topSong);
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