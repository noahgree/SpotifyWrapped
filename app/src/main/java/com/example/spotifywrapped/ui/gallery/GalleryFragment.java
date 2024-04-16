package com.example.spotifywrapped.ui.gallery;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.transition.TransitionInflater;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GalleryFragment extends Fragment {
    private FragmentGalleryBinding binding;
    private RecyclerView recyclerView;
    public static WrapAdapter wrapAdapterP;
    private User currentUser; // Assume this is obtained correctly

    private Context context;
    private FirebaseAuth mAuth;

    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MainActivity.getInstance();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        setEnterTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_fade));
        setExitTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_fade));
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = MainActivity.getInstance();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Assuming you have the current user's ID stored (e.g., as a field in the User object)
        //FirebaseUser user = mAuth.getCurrentUser();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Reference to the user's document in Firestore
            DocumentReference userRef = db.collection("Accounts").document(user.getUid());

            ArrayList<WrapObject> wraps = new ArrayList<>();
            recyclerView = root.findViewById(R.id.wrapRecycler);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            wrapAdapterP = new WrapAdapter(getContext(), wraps);
            recyclerView.setAdapter(wrapAdapterP);
            wrapAdapterP.notifyDataSetChanged();

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<Map<String, Object>> wrapList = (List<Map<String, Object>>) documentSnapshot.get("wraps");
                    if (wrapList != null) {
                        for (int i = 0; i < wrapList.size(); i++) {
                            Map<String, Object> wrapData = wrapList.get(i);
                            if (wrapData != null) {
                                    
                                WrapObject wrap = new WrapObject(i, (String)wrapData.get("Name"),
                                        ((ArrayList<String>) wrapData.get("artistsimage")).get(0),
                                        ((ArrayList<String>) wrapData.get("tracksimage")).get(0),
                                        ((ArrayList<String>) wrapData.get("artists")).get(0),
                                        ((ArrayList<String>) wrapData.get("tracks")).get(0));
                                wraps.add(wrap);
                            }
                        }
                        wrapAdapterP.notifyDataSetChanged();
                    }
                } else {
                    Log.d("FIRESTORE", "No such document");
                }
            }).addOnFailureListener(e -> Log.d("FIRESTORE", "Error getting document", e));
        } else {
            // Handle the case where the user is not logged in
        }

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

