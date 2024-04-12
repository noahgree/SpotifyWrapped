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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentTop5SongsBinding;
import com.example.spotifywrapped.databinding.FragmentTopSongBinding;
import com.example.spotifywrapped.user.User;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class Top5Songs extends Fragment {

    private User loadUser() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String userJson = sharedPreferences.getString("CurrentUser", null);
        Log.d("SharedPreferences", "Loaded token: " + userJson);
        return gson.fromJson(userJson, User.class);
    }

    private FragmentTop5SongsBinding binding;
    public static Context context;

    private static User currentUser;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentTop5SongsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = MainActivity.getInstance();
        currentUser = loadUser();

        ArrayList<Map<String, Object>> wraps = currentUser.getwraps();
        if (!wraps.isEmpty()) {
            Map<String, Object> wrap = wraps.get(wraps.size() - 1);
            String name1 = (String) ((ArrayList<String>) wrap.get("tracks")).get(0);
            String image1 = (String) ((ArrayList<String>) wrap.get("tracksimage")).get(0);
            String name2 = (String) ((ArrayList<String>) wrap.get("tracks")).get(1);
            String image2 = (String) ((ArrayList<String>) wrap.get("tracksimage")).get(1);
            String name3 = (String) ((ArrayList<String>) wrap.get("tracks")).get(2);
            String image3 = (String) ((ArrayList<String>) wrap.get("tracksimage")).get(2);
            String name4 = (String) ((ArrayList<String>) wrap.get("tracks")).get(3);
            String image4 = (String) ((ArrayList<String>) wrap.get("tracksimage")).get(3);
            String name5 = (String) ((ArrayList<String>) wrap.get("tracks")).get(4);
            String image5 = (String) ((ArrayList<String>) wrap.get("tracksimage")).get(4);
            TextView songName = (TextView) root.findViewById(R.id.topsongtext1);
            songName.setText(name1);
            songName = (TextView) root.findViewById(R.id.topsongtext2);
            songName.setText(name2);
            songName = (TextView) root.findViewById(R.id.topsongtext3);
            songName.setText(name3);
            songName = (TextView) root.findViewById(R.id.topsongtext4);
            songName.setText(name4);
            songName = (TextView) root.findViewById(R.id.topsongtext5);
            songName.setText(name5);
            ImageView topsongimage = (ImageView) root.findViewById(R.id.topsongimage1);
            Glide.with(context)
                    .load(image1)
                    .into(topsongimage);
            topsongimage = (ImageView) root.findViewById(R.id.topsongimage2);
            Glide.with(context)
                    .load(image2)
                    .into(topsongimage);
            topsongimage = (ImageView) root.findViewById(R.id.topsongimage3);
            Glide.with(context)
                    .load(image3)
                    .into(topsongimage);
            topsongimage = (ImageView) root.findViewById(R.id.topsongimage4);
            Glide.with(context)
                    .load(image4)
                    .into(topsongimage);
            topsongimage = (ImageView) root.findViewById(R.id.topsongimage5);
            Glide.with(context)
                    .load(image5)
                    .into(topsongimage);
        }

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
        binding.top5songsexit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_gallery);
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