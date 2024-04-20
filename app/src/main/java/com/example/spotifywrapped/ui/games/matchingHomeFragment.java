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
 * Use the {@link matchingHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class matchingHomeFragment extends Fragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private FragmentMatchingHomeBinding binding;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public static Context context;
    private static User currentUser;
    static final OkHttpClient mOkHttpClient = new OkHttpClient();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String term = "short";


    public matchingHomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment matchingHomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static matchingHomeFragment newInstance(String param1, String param2) {
        matchingHomeFragment fragment = new matchingHomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_fade));
        setExitTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_fade));
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

    private String getTimeFrame() {
        // determine time frame of wrap
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
        Button playButton = (Button) root.findViewById(R.id.matchingstart);
        Spinner spinner = null;//(Spinner) root.findViewById(R.id.timeFrameSpinner);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
            }
        });

        // Inflate the layout for this fragment
        return root;

    }

    public void navigateToMatchingGameFragment(List<String> imageUrls) {
        NavController navController = Navigation.findNavController(requireView());
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("imageUrls", (ArrayList<String>) imageUrls);
        navController.navigate(R.id.matchingGameFragment3, bundle);
    }

}