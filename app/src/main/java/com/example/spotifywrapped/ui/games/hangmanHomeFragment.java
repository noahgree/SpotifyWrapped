package com.example.spotifywrapped.ui.games;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentHangmanHomeBinding;
import com.example.spotifywrapped.databinding.FragmentHomeBinding;
import com.example.spotifywrapped.databinding.FragmentMatchingHomeBinding;
import com.example.spotifywrapped.ui.gallery.AddWrapFragment;
import com.example.spotifywrapped.user.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class hangmanHomeFragment extends Fragment {

    private FragmentHangmanHomeBinding binding;


    public static Context context;
    private static User currentUser;
    private static final OkHttpClient mOkHttpClient = new OkHttpClient();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MainActivity.getInstance();
        currentUser = loadUser();
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


    public static String getSpotifyToken() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String string = sharedPreferences.getString("SpotifyToken", null);
        Log.d("SharedPreferences", "Loaded token: " + string);
        return sharedPreferences.getString("SpotifyToken", null);
    }

    public static void onMatchStarted(Context context, View view, OkHttpClient okHttpClient, String term, Map<String, Object> wrap, AddWrapFragment.DataCompletionHandler handler) {
        if (getSpotifyToken() == null) {
            Toast.makeText(context, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/artists?time_range=" + term + "_term&limit=10")
                .addHeader("Authorization", "Bearer " + getSpotifyToken())
                .build();

        Call mCall = okHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Failed to fetch data, watch Logcat for more details", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                try {
                    final JSONObject jsonObject = new JSONObject(responseData);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        try {
                            if (jsonObject.getJSONArray("items").length() > 0) {
                                List<String> name = new ArrayList<>();
                                for (int i = 0; i < 8; i++) {
                                    name.add(jsonObject.getJSONArray("items").getJSONObject(i).getString("name"));
                                }
                                wrap.put("artists", name);

                                if (handler != null) {
                                    handler.onDataCompleted(wrap);
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Failed to parse data, watch Logcat for more details", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHangmanHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setEnterTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_slide_right));
        setExitTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_slide_left));

        Button playButton = root.findViewById(R.id.hangmanstart);
        Spinner spinner = null;
        playButton.setOnClickListener(v -> {

            String term = "long";
            Map<String, Object> wrap = new HashMap<>();


            AddWrapFragment.DataCompletionHandler handler = updatedImages -> {
                // This block will be called once data fetching is complete.
                getActivity().runOnUiThread(() -> {
                    // switch to matching game view
                    List<String> artists = (List<String>) updatedImages.get("artists");
                    navigateToHangmanGameFragment(artists);
                });
            };


            onMatchStarted(context, root, mOkHttpClient, term, wrap, handler);
        });

        // Inflate the layout for this fragment
        return root;

    }
    public void navigateToHangmanGameFragment(List<String> artists) {
        NavController navController = Navigation.findNavController(requireView());
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("artists", (ArrayList<String>) artists);
        navController.navigate(R.id.navHangmanGame, bundle);
    }


}