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
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GalleryFragment extends Fragment {
    private FragmentGalleryBinding binding;
    private RecyclerView recyclerView;
    private WrapAdapter wrapAdapter;
    private User currentUser; // Assume this is obtained correctly

    private Context context;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = MainActivity.getInstance();
        currentUser = MainActivity.getCurrentUser();
        ArrayList<WrapObject> wraps = new ArrayList<>();
        //currentUser = loadUser(); // Ensure this method exists and correctly fetches the current user
        if (currentUser != null && currentUser.getwraps() != null) {
            Log.d("GALLERY", "LOADED: " + currentUser.getwraps().size());
            List<Map<String, Object>> temp = currentUser.getwraps();
            for(int i = 0; i < temp.size(); i++) {
                Log.d("GALLERY", "LOADED: " + ((List<String>) temp.get(i).get("artistsimage")).get(0));
                wraps.add(new WrapObject(i, "Wrap #" + i + 1, ((List<String>) temp.get(i).get("artistsimage")).get(0), ((List<String>) temp.get(i).get("tracksimage")).get(0), ((List<String>) temp.get(i).get("artists")).get(0), ((List<String>) temp.get(i).get("tracks")).get(0)));
            }
        }
        recyclerView = root.findViewById(R.id.wrapRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        wrapAdapter = new WrapAdapter(getContext(), wraps);
        recyclerView.setAdapter(wrapAdapter);

        // Set the click listener for the button
        binding.addButtonTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_addWrap);
            }
        });

        return root;
    }

    private User loadUser() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String userJson = sharedPreferences.getString("CurrentUser", null);
        Log.d("SharedPreferences", "Loaded token: " + userJson);
        return gson.fromJson(userJson, User.class);
    }


//    private User loadUser() {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
//        Gson gson = new Gson();
//        String userJson = sharedPreferences.getString("CurrentUser", null);
//        Log.d("SharedPreferences", "Loaded token: " + userJson);
//        return gson.fromJson(userJson, User.class);
//    }

//    private FragmentGalleryBinding binding;
//    private RecyclerView recyclerView;
//    private static ArrayList<WrapObject> WrapArrayList = new ArrayList<>();
//    private static WrapListAdapter myAdapter;
//
//    private WrapAdapter wrapAdapter;
//    private List<WrapObject> wrapList; // Assume this list will be populated accordingly
//
//    public static Context context;
//
//    private static User currentUser;




//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        GalleryViewModel galleryViewModel =
//                new ViewModelProvider(this).get(GalleryViewModel.class);
//
//        binding = FragmentGalleryBinding.inflate(inflater, container, false);
//        View root = binding.getRoot();
//        context = MainActivity.getInstance();
//
//        initializeUserAndWraps();
//
//        recyclerView = root.findViewById(R.id.wrapRecycler); // Ensure this is the correct ID from your layout
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        recyclerView.setAdapter(wrapAdapter);
//
//    // Set the click listener for the button
//        binding.addButtonTask.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                NavController navController = Navigation.findNavController(v);
//                navController.navigate(R.id.nav_addWrap);
//            }
//        });


//        //ISSUE
//        if (MainActivity.getInstance() != null) {
//            currentUser = loadUser();
//            ArrayList<Map<String, Object>> wraps = currentUser.getwraps();
//            for (int i = 0; i < wraps.size(); i++) {
//                Map<String, Object> wrap = wraps.get(i);
//                wrapList.add(new WrapObject(i, (String) wrap.get("name"), ((ArrayList<String>) wrap.get("artistsimage")).get(0), ((ArrayList<String>) wrap.get("tracksimage")).get(0), ((ArrayList<String>) wrap.get("artists")).get(0), ((ArrayList<String>) wrap.get("tracks")).get(0)));
//            }
//        }
//
//
//        recyclerView = root.findViewById(R.id.wrapRecycler); // make sure recyclerView is in your FragmentGalleryBinding
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
////        myAdapter = new WrapListAdapter(getContext(), WrapArrayList);
////        //recyclerView.setAdapter(myAdapter);
////        myAdapter.notifyDataSetChanged();
//
//        // Initialize the list and adapter here (assuming list is pre-populated or will be fetched)
//        if (wrapList != null || wrapList.isEmpty()) {
//            wrapList = new ArrayList<>(); // Or fetch from a source
//        }
//        wrapAdapter = new WrapAdapter(getActivity(), wrapList);
//
//        recyclerView.setAdapter(wrapAdapter);
//
//        // Set the click listener for the button
//        binding.addButtonTask.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                NavController navController = Navigation.findNavController(v);
//                navController.navigate(R.id.nav_addWrap);
//            }
//        });

//        final TextView textView = binding.textGallery;
//        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
//        return root;
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

