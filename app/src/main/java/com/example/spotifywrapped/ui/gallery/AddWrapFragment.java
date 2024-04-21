package com.example.spotifywrapped.ui.gallery;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentAddWrapBinding;
import com.example.spotifywrapped.ui.gallery.pages.WrappedSummary;
import com.example.spotifywrapped.user.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddWrapFragment extends Fragment {
    private FragmentAddWrapBinding binding;
    public static Context context;
    private static User currentUser;
    private static final OkHttpClient mOkHttpClient = new OkHttpClient();

    // for auto setting the radio button on entering the add wrap fragment
    public static String visibilityOrigin = "Private";

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
        // determine time frame of wrap
        RadioButton shortTerm = getActivity().findViewById(R.id.shortTermBtn);
        RadioButton mediumTerm = getActivity().findViewById(R.id.mediumTermBtn);
        RadioButton longTerm = getActivity().findViewById(R.id.longTermBtn);
        if (shortTerm.isChecked()) {
            term = "short";
        } else if (mediumTerm.isChecked()) {
            term = "medium";
        } else if (longTerm.isChecked()) {
            term = "long";
        }

        return term;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RadioButton publicBtn = getActivity().findViewById(R.id.radioButtonPub);
        RadioButton privateBtn = getActivity().findViewById(R.id.radioButtonPriv);
        if (AddWrapFragment.getVisibilityOrigin().equals("Private")) {
            privateBtn.setChecked(true);
            publicBtn.setChecked(false);
        } else {
            privateBtn.setChecked(false);
            publicBtn.setChecked(true);
        }

        RadioButton shortTerm = getActivity().findViewById(R.id.shortTermBtn);
        shortTerm.setChecked(true);
    }

    public interface DataCompletionHandler {
        void onDataCompleted(Map<String, Object> wrap);
    }

    public static String getSpotifyToken() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String string = sharedPreferences.getString("SpotifyToken", null);
        Log.d("SharedPreferences", "Loaded token: " + string);
        return sharedPreferences.getString("SpotifyToken", null);
    }

    public static void onWrapMade(Context context, View view, OkHttpClient okHttpClient, String term, Map<String, Object> wrap, DataCompletionHandler handler) {
        if (getSpotifyToken() == null) {
            Toast.makeText(context, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        final AtomicInteger completionCounter = new AtomicInteger(0); // Synchronization counter
        List<String> endpoints = Arrays.asList("artists", "tracks"); // Assuming these are your two categories
        for (String thing : endpoints) {

            final Request request = new Request.Builder()
                    .url("https://api.spotify.com/v1/me/top/" + thing + "?time_range=" + term + "_term&limit=5")
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
                                    List<String> url = new ArrayList<>();
                                    List<String> genre = new ArrayList<>();
                                    List<String> uniqueness = new ArrayList<>();
                                    JSONArray images;
                                    for (int i = 0; i < 5; i++) {
                                        if (thing.equals("artists")) {
                                            name.add(jsonObject.getJSONArray("items").getJSONObject(i).getString("name"));
                                            images = jsonObject.getJSONArray("items").getJSONObject(i).getJSONArray("images");
                                            if (!images.isNull(0)) {
                                                url.add(images.getJSONObject(0).getString("url"));
                                            } else {
                                                url.add("default");
                                            }
                                            Log.d("test", jsonObject.getJSONArray("items").getJSONObject(i).getJSONArray("genres").toString());
                                            if (jsonObject.getJSONArray("items").getJSONObject(i).getJSONArray("genres").length() != 0) {
                                                genre.add(jsonObject.getJSONArray("items").getJSONObject(i).getJSONArray("genres").getString(0));
                                            }
                                            uniqueness.add(jsonObject.getJSONArray("items").getJSONObject(i).getString("popularity"));
                                        } else {
                                            name.add(jsonObject.getJSONArray("items").getJSONObject(i).getString("name"));
                                            images = jsonObject.getJSONArray("items").getJSONObject(i).getJSONObject("album").getJSONArray("images");
                                            if (!images.isNull(0)) {
                                                url.add(images.getJSONObject(0).getString("url"));
                                            } else {
                                                url.add("default");
                                            }
                                        }
                                    }
                                    if (genre.size() == 0) {
                                        genre.add("None Found");
                                    }
                                    wrap.put(thing, name);
                                    wrap.put(thing + "image", url);
                                    if (thing.equals("artists")) {
                                        wrap.put(thing + "genre", genre);
                                        wrap.put(thing + "popularity", uniqueness);
                                    }

                                    if (handler != null && completionCounter.incrementAndGet() == 2) {
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
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddWrapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        FrameLayout mainLayout = root.findViewById(R.id.mainAW);
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
        FirebaseUser user = mAuth.getCurrentUser();

        // Reference to the user's document in Firestore
        DocumentReference userRef = db.collection("Accounts").document(user.getUid());
        DocumentReference publicRef = db.collection("Accounts").document("vGLXVzArF0OObsE5bJT4jNpdOy33");

        binding.generateButton.setOnClickListener(v -> {

            EditText name = root.findViewById(R.id.editTextName);
            Map<String, Object> wrap = new HashMap<>();
            wrap.put("Name", name.getText().toString());

            wrap.put("timeframe", getTimeFrame());

            SimpleDateFormat sdf = new SimpleDateFormat("MM.dd.yy");
            Date currentDate = new Date();
            String formattedDate = sdf.format(currentDate);
            wrap.put("creationdate", formattedDate);

            wrap.put("username", currentUser.getName());

            RadioButton publicBtn = getActivity().findViewById(R.id.radioButtonPub);
            boolean publicBool = publicBtn.isChecked();
            wrap.put("alsopublic", String.valueOf(publicBool));

            Log.d("private", String.valueOf(GalleryFragment.getAdapterSize()));
            WrappedSummary.setPrivateWrapIndex(GalleryFragment.getAdapterSize());
            WrappedSummary.setPublicWrap(false);

            DataCompletionHandler handler = updatedWrap -> {
                // This block will be called once data fetching is complete.
                getActivity().runOnUiThread(() -> {
                    Map<String, Object> dataToUpdate = new HashMap<>();

                    userRef.update("wraps", FieldValue.arrayUnion(updatedWrap))
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Public wrap added to array successfully"))
                            .addOnFailureListener(e -> Log.e(TAG, "Error adding public wrap to array", e));

                    Log.d("Firestore CHECK", user.getUid());
                    currentUser.addWrap(updatedWrap);

                    RadioButton pub = root.findViewById(R.id.radioButtonPub);
                    if (pub.isChecked()) {
                        publicRef.update("wraps", FieldValue.arrayUnion(updatedWrap))
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Public wrap added to array successfully"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error adding public wrap to array", e));

                        Log.d("Firestore CHECK", user.getUid());
                        WrappedSummary.setReturnToPrivate(false);
                    } else {
                        WrappedSummary.setReturnToPrivate(true);
                    }

                    // switch to slideshow view
                    NavController navController = Navigation.findNavController(v);
                    navController.popBackStack(R.id.nav_gallery, true); // Clear back stack up to home
                    navController.navigate(R.id.nav_topSong);


                    // hide action bar up top
                    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.hide();
                        ImageView imageView = getActivity().findViewById(R.id.currentPageIcon);
                        imageView.setVisibility(View.GONE);
                    }
                });
            };

            onWrapMade(context, root, mOkHttpClient, term, wrap, handler);

        });


        return root;
    }

    public static void setVisibilityOrigin(String origin) {
        AddWrapFragment.visibilityOrigin = origin;
    }
    public static String getVisibilityOrigin() {
        return visibilityOrigin;
    }
}