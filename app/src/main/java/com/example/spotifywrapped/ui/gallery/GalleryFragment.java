package com.example.spotifywrapped.ui.gallery;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentGalleryBinding;
import com.example.spotifywrapped.user.User;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GalleryFragment extends Fragment {

    private User loadUser() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String userJson = sharedPreferences.getString("CurrentUser", null);
        Log.d("SharedPreferences", "Loaded token: " + userJson);
        return gson.fromJson(userJson, User.class);
    }

    private FragmentGalleryBinding binding;
    private RecyclerView recyclerView;
    private static ArrayList<WrapObject> WrapArrayList;
    private static WrapListAdapter myAdapter;
    public static Context context;

    private static User currentUser;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = MainActivity.getInstance();
        /*if (MainActivity.getInstance() != null) {
            currentUser = loadUser();
            ArrayList<Map<String, Object>> wraps = currentUser.getwraps();
            for (int i = 0; i < wraps.size(); i++) {
                Map<String, Object> wrap = wraps.get(i);
                WrapArrayList.add(new WrapObject(i, (String) wrap.get("name"), ((ArrayList<String>) wrap.get("artistsimage")).get(0), ((ArrayList<String>) wrap.get("tracksimage")).get(0), ((ArrayList<String>) wrap.get("artists")).get(0), ((ArrayList<String>) wrap.get("tracks")).get(0)));
            }
        }*/


        recyclerView = root.findViewById(R.id.wrapRecycler); // make sure recyclerView is in your FragmentGalleryBinding
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        myAdapter = new WrapListAdapter(getContext(), WrapArrayList);
        //recyclerView.setAdapter(myAdapter);
        //myAdapter.notifyDataSetChanged();


        // Set the click listener for the button
        binding.addButtonTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_addWrap);
            }
        });

//        final TextView textView = binding.textGallery;
//        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

