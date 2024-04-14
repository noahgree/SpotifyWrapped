package com.example.spotifywrapped.ui.gallery.pages;

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
import com.example.spotifywrapped.user.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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
    public static Context context;

    private static User currentUser;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MainActivity.getInstance();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }


    private User loadUser() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String userJson = sharedPreferences.getString("CurrentUser", null);
        Log.d("SharedPreferences", "Loaded token: " + userJson);
        return gson.fromJson(userJson, User.class);
    }

    private void setNameonTitle() {
        TextView titlesWithName = (TextView) binding.getRoot().findViewById(R.id.topSongIntro);
        String userName = (String) MainActivity.getCurrentUser().getName();
        if (userName != null && !userName.isEmpty()) {
            userName = userName.substring(0, userName.indexOf(" "));
        } else {
            userName = "User";
        }
        userName = userName + "'s ";
        titlesWithName.setText(userName + titlesWithName.getText());
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTopArtistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Assuming you have the current user's ID stored (e.g., as a field in the User object)
        FirebaseUser user = mAuth.getCurrentUser();


        // Reference to the user's document in Firestore
        DocumentReference userRef = db.collection("Accounts").document(user.getUid());

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> wrapList = (List<Map<String, Object>>) documentSnapshot.get("wraps");
                if (wrapList != null) {
                    Map<String, Object> wrapData = wrapList.get(wrapList.size() - 1);
                    if (wrapData != null) {
                        Map<String, Object> wrap = wrapList.get(wrapList.size() - 1);
                        String name = (String) ((ArrayList<String>) wrap.get("artists")).get(0);
                        String image = (String) ((ArrayList<String>) wrap.get("artistsimage")).get(0);
                        TextView artistName = (TextView) root.findViewById(R.id.topartist);
                        artistName.setText(name);
                        ImageView topartistimage = (ImageView) root.findViewById(R.id.artistimage);
                        Glide.with(context)
                                .load(image)
                                .into(topartistimage);
                        setNameonTitle();
                    }
                }
            } else {
                Log.d("FIRESTORE", "No such document");
            }
        }).addOnFailureListener(e -> Log.d("FIRESTORE", "Error getting document", e));


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
        binding.topartistexit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_gallery);

                // Show the toolbar
                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                if (actionBar != null) {
                    actionBar.show();
                    ImageView imageView = getActivity().findViewById(R.id.currentPageIcon);
                    imageView.setVisibility(View.VISIBLE);
                }
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