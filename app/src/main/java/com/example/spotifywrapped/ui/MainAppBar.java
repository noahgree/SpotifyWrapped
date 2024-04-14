package com.example.spotifywrapped.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentMainAppBarBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainAppBar extends Fragment {
    private FragmentMainAppBarBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMainAppBarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // For swapping toolbar icon
        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);

        ImageView currentPageIcon = root.findViewById(R.id.currentPageIcon);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destId = destination.getId();
            Log.d("navigation", "changed to" + destId);

            if (destId == R.id.nav_home) {
                currentPageIcon.setImageResource(R.drawable.key);
            } else if (destId == R.id.nav_gallery) {
                currentPageIcon.setImageResource(R.drawable.rectangle_stack_fill);
            } else if (destId == R.id.nav_public) {
                currentPageIcon.setImageResource(R.drawable.rectangle_stack_person_crop_fill);
            } else if (destId == R.id.nav_games) {
                currentPageIcon.setImageResource(R.drawable.gameboy_solid);
            } else if (destId == R.id.nav_settings) {
                currentPageIcon.setImageResource(R.drawable.settings);
            } else {
                currentPageIcon.setImageResource(R.drawable.rectangle_stack_fill); // Fallback icon
            }
        });

        return root;
    }
}