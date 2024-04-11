package com.example.spotifywrapped.ui.gallery.pages;

import static android.content.ContentValues.TAG;
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
public class TopArtist extends Fragment {

    private FragmentTopArtistBinding binding;

    public void loadWrap(View root) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Assuming 'users' is your collection and 'userId' is the specific user's ID who has the wraps
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current logged-in user ID
        DocumentReference docRef = db.collection("users").document(userId);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Assuming wraps is a field which is a list of maps
                    ArrayList<Map<String, Object>> wraps = (ArrayList<Map<String, Object>>) document.get("wraps");
                    if (wraps != null && !wraps.isEmpty()) {
                        Map<String, Object> latestWrap = wraps.get(wraps.size() - 1); // Get the last wrap
                        handleWrap(latestWrap, root); // Process or display your wrap
                    } else {
                        Log.d(TAG, "No wraps found");
                    }
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void handleWrap(Map<String, Object> wrap, View root) {
        TextView artistName = root.findViewById(R.id.topartist);
        ImageView topartistimage = root.findViewById(R.id.artistimage);

        List<String> artists = (ArrayList<String>) wrap.get("artists");
        Log.d("artistssssss", artists.toString());
        List<String> images = (ArrayList<String>) wrap.get("artistsimage");

        if (artists != null && !artists.isEmpty() && images != null && !images.isEmpty()) {
            artistName.setText(artists.get(0));
            Glide.with(getActivity()) // Use getActivity() for safer context usage.
                    .load(images.get(0))
                    .into(topartistimage);
        } else {
            artistName.setText("No track info available");
        }

    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTopArtistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        loadWrap(root);

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