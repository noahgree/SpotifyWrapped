package com.example.spotifywrapped.ui.games;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.Looper;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
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
public class matchingHomeFragment extends Fragment {

    private FragmentMatchingHomeBinding binding;
    public static Context context;
    private static User currentUser;
    static final OkHttpClient mOkHttpClient = new OkHttpClient();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String term = "short";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MainActivity.getInstance();
        currentUser = loadUser();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        setEnterTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_slide_right));
        setExitTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_slide_left));
    }

    private User loadUser() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String userJson = sharedPreferences.getString("CurrentUser", null);
        Log.d("SharedPreferences", "Loaded token: " + userJson);
        return gson.fromJson(userJson, User.class);
    }

    private String getTimeFrame() {
        RadioButton shortTerm = getActivity().findViewById(R.id.matchingShortTermBtn);
        RadioButton mediumTerm = getActivity().findViewById(R.id.matchingMediumTermBtn);
        RadioButton longTerm = getActivity().findViewById(R.id.matchingLongTermBtn);
        if (shortTerm.isChecked()) {
            term = "short";
        } else if (mediumTerm.isChecked()) {
            term = "medium";
        } else if (longTerm.isChecked()) {
            term = "long";
        }

        return term;
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
                .url("https://api.spotify.com/v1/me/top/artists?time_range=" + term + "_term&limit=8")
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
                                List<String> url = new ArrayList<>();
                                for (int i = 0; i < 8; i++) {
                                    url.add(jsonObject.getJSONArray("items").getJSONObject(i).getJSONArray("images").getJSONObject(0).getString("url"));
                                }
                                wrap.put("artists", url);

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
        binding = FragmentMatchingHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Button playButton = root.findViewById(R.id.matchingstart);
        Spinner spinner = null;
        playButton.setOnClickListener(v -> {

            term = getTimeFrame();
            Map<String, Object> wrap = new HashMap<>();


            AddWrapFragment.DataCompletionHandler handler = updatedImages -> {
                // This block will be called once data fetching is complete.
                getActivity().runOnUiThread(() -> {
                    // switch to matching game view
                    List<String> images = (List<String>) updatedImages.get("artists");
                    navigateToMatchingGameFragment(images);
                });
            };


            onMatchStarted(context, root, mOkHttpClient, term, wrap, handler);
        });

        return root;

    }

    public void navigateToMatchingGameFragment(List<String> imageUrls) {
        NavController navController = Navigation.findNavController(requireView());
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("imageUrls", (ArrayList<String>) imageUrls);
        navController.navigate(R.id.matchingGameFragment, bundle);
    }

}