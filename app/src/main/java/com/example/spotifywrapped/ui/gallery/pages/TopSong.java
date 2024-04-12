package com.example.spotifywrapped.ui.gallery.pages;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
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
import com.example.spotifywrapped.databinding.FragmentTopArtistBinding;
import com.example.spotifywrapped.databinding.FragmentTopSongBinding;
import com.example.spotifywrapped.ui.gallery.WrapObject;
import com.example.spotifywrapped.user.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class TopSong extends Fragment {
    private User loadUser() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String userJson = sharedPreferences.getString("CurrentUser", null);
        Log.d("SharedPreferences", "Loaded token: " + userJson);
        return gson.fromJson(userJson, User.class);
    }

    private static WrapObject wrap;

    public static TopSong newInstance(WrapObject selectedEvent) {
        TopSong.wrap = selectedEvent;
        return new TopSong();
    }


    private FragmentTopSongBinding binding;

    public static Context context;

    private static User currentUser;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTopSongBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = MainActivity.getInstance();
        currentUser = loadUser();

        ArrayList<Map<String, Object>> wraps = currentUser.getwraps();
        if (wraps != null && !wraps.isEmpty()) {
            Map<String, Object> wrap = wraps.get(wraps.size() - 1);
            String name = (String) ((ArrayList<String>) wrap.get("tracks")).get(0);
            String image = (String) ((ArrayList<String>) wrap.get("tracksimage")).get(0);
            TextView songName = (TextView) root.findViewById(R.id.topsong);
            songName.setText(name);
            ImageView topartistimage = (ImageView) root.findViewById(R.id.topsongimage);
            Glide.with(context)
                    .load(image)
                    .into(topartistimage);
        }
        // Set the click listener for the button
        binding.topsongnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_top5Songs);
            }
        });
        binding.topsongexit.setOnClickListener(new View.OnClickListener() {
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