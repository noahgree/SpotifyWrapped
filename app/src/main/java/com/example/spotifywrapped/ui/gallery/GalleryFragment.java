package com.example.spotifywrapped.ui.gallery;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.SwipeItem;
import com.example.spotifywrapped.databinding.FragmentGalleryBinding;
import com.example.spotifywrapped.ui.CustomLinearLayoutManager;
import com.example.spotifywrapped.ui.gallery.pages.WrappedSummary;
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
    public static RecyclerView recyclerView;
    public static WrapAdapter wrapAdapterP;
    public static CustomLinearLayoutManager layoutManager;
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

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            LinearLayout mainLayout = root.findViewById(R.id.mainG);
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {

                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

                if (insets.bottom > 0) {
                    mlp.bottomMargin = insets.bottom;
                    v.setLayoutParams(mlp);
                }

                return WindowInsetsCompat.CONSUMED;
            });
        }, 100); // Delay in milliseconds

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        ArrayList<WrapObject> wraps = new ArrayList<>();
        layoutManager = new CustomLinearLayoutManager(getContext());
        recyclerView = root.findViewById(R.id.wrapRecycler);
        recyclerView.setLayoutManager(layoutManager);
        wrapAdapterP = new WrapAdapter(getContext(), wraps);
        recyclerView.setAdapter(wrapAdapterP);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeItem(wrapAdapterP));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Assuming you have the current user's ID stored (e.g., as a field in the User object)
        //FirebaseUser user = mAuth.getCurrentUser();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Reference to the user's document in Firestore
            DocumentReference userRef = db.collection("Accounts").document(user.getUid());

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
                                        ((ArrayList<String>) wrapData.get("tracks")).get(0),
                                        (String)wrapData.get("timeframe"),
                                        (String)wrapData.get("username"),
                                        (String)wrapData.get("creationdate"),
                                        (String)wrapData.get("alsopublic"));
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
        binding.addButtonTask.setOnClickListener(v -> {
            AddWrapFragment.setVisibilityOrigin("Private");
            WrappedSummary.setReturnToPrivate(true);
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.nav_addWrap);
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

    public static int getAdapterSize() {
        return wrapAdapterP.getItemCount();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

