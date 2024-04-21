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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

    private static FragmentHangmanHomeBinding binding;
    public static Context context;
    private static final OkHttpClient mOkHttpClient = new OkHttpClient();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String term = "long";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MainActivity.getInstance();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        setEnterTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_slide_right));
        setExitTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_slide_left));
    }

    private String getTimeFrame() {
        RadioButton shortTerm = getActivity().findViewById(R.id.shortTermBtnHM);
        RadioButton mediumTerm = getActivity().findViewById(R.id.mediumTermBtnHM);
        RadioButton longTerm = getActivity().findViewById(R.id.longTermBtnHM);
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

        // set insets
        FrameLayout mainLayout = root.findViewById(R.id.hangmanHomeLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            if (insets.bottom > 0) {
                mlp.bottomMargin = insets.bottom;
                v.setLayoutParams(mlp);
            }

            return WindowInsetsCompat.CONSUMED;
        });

        Button playButton = root.findViewById(R.id.hangmanstart);
        playButton.setText("BEGIN");

        ImageView spaceFiller = binding.getRoot().findViewById(R.id.spaceFillerHM);
        LinearLayout resultsBox = binding.getRoot().findViewById(R.id.resultsBoxHM);
        LinearLayout explainBox = binding.getRoot().findViewById(R.id.explainHM);
        spaceFiller.setVisibility(View.VISIBLE);
        resultsBox.setVisibility(View.GONE);
        explainBox.setVisibility(View.VISIBLE);
        if (hangmanGameFragment.getHasPlayed()) {
            playButton.setText("PLAY AGAIN");
            explainBox.setVisibility(View.GONE);
            setUpResults();
        }

        RadioButton shortTerm = binding.getRoot().findViewById(R.id.shortTermBtnHM);
        shortTerm.setChecked(true);

        playButton.setOnClickListener(v -> {
            Map<String, Object> wrap = new HashMap<>();
            AddWrapFragment.DataCompletionHandler handler = updatedImages -> {
                // This block will be called once data fetching is complete.
                getActivity().runOnUiThread(() -> {
                    // switch to matching game view
                    List<String> artists = (List<String>) updatedImages.get("artists");
                    navigateToHangmanGameFragment(artists);
                });
            };

            onMatchStarted(context, root, mOkHttpClient, getTimeFrame(), wrap, handler);
        });

        return root;
    }

    public void navigateToHangmanGameFragment(List<String> artists) {
        NavController navController = Navigation.findNavController(requireView());
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("artists", (ArrayList<String>) artists);
        navController.navigate(R.id.navHangmanGame, bundle);
    }

    public static void setUpResults() {
        TextView scoreText = binding.getRoot().findViewById(R.id.scoreValueHomeHM);
        TextView resultsTitle = binding.getRoot().findViewById(R.id.resultsTextHM);
        TextView timeText = binding.getRoot().findViewById(R.id.timeResultHM);
        ImageView spaceFiller = binding.getRoot().findViewById(R.id.spaceFillerHM);
        LinearLayout resultsBox = binding.getRoot().findViewById(R.id.resultsBoxHM);
        LinearLayout explainBox = binding.getRoot().findViewById(R.id.explainHM);

        spaceFiller.setVisibility(View.GONE);
        resultsBox.setVisibility(View.VISIBLE);
        explainBox.setVisibility(View.GONE);
        timeText.setVisibility(View.VISIBLE);

        if (hangmanGameFragment.getScore() == 0) {
            scoreText.setText(String.valueOf(0));
            timeText.setTextColor(ContextCompat.getColor(context, R.color.spotify_red));
            timeText.setText("YOU LOST");
        } else {
            scoreText.setText(String.valueOf(hangmanGameFragment.getScore()));
            timeText.setTextColor(ContextCompat.getColor(context, R.color.spotify_black));
            timeText.setText("FINISHED IN: " + hangmanGameFragment.getTime());
        }
    }
}