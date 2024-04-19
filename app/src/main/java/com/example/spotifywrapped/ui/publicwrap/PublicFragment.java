package com.example.spotifywrapped.ui.publicwrap;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentGalleryBinding;
import com.example.spotifywrapped.databinding.FragmentPublicBinding;
import com.example.spotifywrapped.ui.gallery.WrapAdapter;
import com.example.spotifywrapped.ui.gallery.WrapObject;
import com.example.spotifywrapped.user.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PublicFragment extends Fragment {
    private FragmentPublicBinding binding;

    private RecyclerView recyclerView;
    public static WrapAdapter wrapAdapter;
    private User currentUser; // Assume this is obtained correctly

    private Context context;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPublicBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = MainActivity.getInstance();
        currentUser = MainActivity.getCurrentUser();
        setEnterTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_fade));
        setExitTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_fade));

        LinearLayout mainLayout = root.findViewById(R.id.mainFP);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            if (insets.bottom > 0) {
                mlp.bottomMargin = insets.bottom;
                v.setLayoutParams(mlp);
            }

            return WindowInsetsCompat.CONSUMED;
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference publicRef = db.collection("Accounts").document("vGLXVzArF0OObsE5bJT4jNpdOy33");
        recyclerView = root.findViewById(R.id.publicWrapRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ArrayList<WrapObject> wraps = new ArrayList<>();
        wrapAdapter = new WrapAdapter(getContext(), wraps);
        recyclerView.setAdapter(wrapAdapter);
        wrapAdapter.notifyDataSetChanged();


        publicRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> wrapList = (List<Map<String, Object>>) documentSnapshot.get("wraps");
                if (wrapList != null) {
                    for (int i = 0; i < wrapList.size(); i++) {
                        Map<String, Object> wrapData = wrapList.get(i);
                        WrapObject wrap = new WrapObject(i, (String)wrapData.get("Name"),
                                ((ArrayList<String>) wrapData.get("artistsimage")).get(0),
                                ((ArrayList<String>) wrapData.get("tracksimage")).get(0),
                                ((ArrayList<String>) wrapData.get("artists")).get(0),
                                ((ArrayList<String>) wrapData.get("tracks")).get(0),
                                (String)wrapData.get("timeframe"),
                                (String)wrapData.get("username"),
                                (String)wrapData.get("creationdate"));
                        wrap.setPublicWrap(true);
                        wraps.add(wrap);
                    }
                    wrapAdapter.notifyDataSetChanged();
                }
            } else {
                Log.d("FIRESTORE", "No such document");
            }
        }).addOnFailureListener(e -> Log.d("FIRESTORE", "Error getting document", e));

        return root;
    }

    private User loadUser() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String userJson = sharedPreferences.getString("CurrentUser", null);
        Log.d("SharedPreferences", "Loaded token: " + userJson);
        return gson.fromJson(userJson, User.class);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
