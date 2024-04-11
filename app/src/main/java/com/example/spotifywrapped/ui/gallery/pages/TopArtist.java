package com.example.spotifywrapped.ui.gallery.pages;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentTopArtistBinding;
import com.example.spotifywrapped.databinding.FragmentTopSongBinding;
import com.example.spotifywrapped.user.User;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class TopArtist extends Fragment {

    private FragmentTopArtistBinding binding;
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
        binding = FragmentTopArtistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = MainActivity.getInstance();
        currentUser = loadUser();

        ArrayList<Map<String, Object>> wraps = currentUser.getwraps();
        if (!wraps.isEmpty()) {
            Map<String, Object> wrap = wraps.get(0);
            String name = (String) ((ArrayList<String>) wrap.get("artists")).get(0);
            String image = (String) ((ArrayList<String>) wrap.get("artistsimage")).get(0);
            TextView artistName = (TextView) root.findViewById(R.id.topartist);
            artistName.setText(name);
            ImageView topartistimage = (ImageView) root.findViewById(R.id.artistimage);
            Glide.with(context)
                    .load(image)
                    .into(topartistimage);
        }
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