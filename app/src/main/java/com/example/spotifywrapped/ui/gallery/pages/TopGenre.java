package com.example.spotifywrapped.ui.gallery.pages;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentTop5ArtistsBinding;
import com.example.spotifywrapped.databinding.FragmentTopGenreBinding;
import com.example.spotifywrapped.user.User;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class TopGenre extends Fragment {

    private FragmentTopGenreBinding binding;
    public static Context context;

    private static User currentUser;

    private User loadUser() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String userJson = sharedPreferences.getString("CurrentUser", null);
        Log.d("SharedPreferences", "Loaded token: " + userJson);
        return gson.fromJson(userJson, User.class);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTopGenreBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = MainActivity.getInstance();
        currentUser = loadUser();

        ArrayList<Map<String, Object>> wraps = currentUser.getwraps();
        if (!wraps.isEmpty()) {
            Map<String, Object> wrap = wraps.get(wraps.size() - 1);
            String genre = (String) ((ArrayList<String>) wrap.get("artistsgenre")).get(0);
            String image = (String) ((ArrayList<String>) wrap.get("artistsimage")).get(0);
            TextView artistName = (TextView) root.findViewById(R.id.topgenre);
            artistName.setText(genre);
            ImageView topartistimage = (ImageView) root.findViewById(R.id.genreimage);
            Glide.with(context)
                    .load(image)
                    .into(topartistimage);
        }



        // Set the click listener for the button
        binding.topgenreback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_top5Artists);
            }
        });
        binding.topgenreexit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActionBar actionBar = ((AppCompatActivity) MainActivity.getInstance()).getSupportActionBar();
                if (actionBar != null) {
                    actionBar.show();
                }
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_gallery);

                // Show the toolbar
                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                if (actionBar != null) {
                    actionBar.show();
                }
            }
        });

        return root;
    }

    private void showActionBar() {
        if (getActivity() != null && getActivity() instanceof AppCompatActivity) {
            AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
            if (appCompatActivity.getSupportActionBar() != null) {
                appCompatActivity.getSupportActionBar().show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}