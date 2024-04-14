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
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentAddWrapBinding;
import com.example.spotifywrapped.user.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
 * Use the {@link AddWrapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddWrapFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentAddWrapBinding binding;
    public static Context context;
    private static User currentUser;
    private static final OkHttpClient mOkHttpClient = new OkHttpClient();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;



    public AddWrapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddWrapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddWrapFragment newInstance(String param1, String param2) {
        AddWrapFragment fragment = new AddWrapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

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

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the spinner
        Spinner timeFrameSpinner = view.findViewById(R.id.timeFrameSpinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.time_frame_options, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        // Apply the adapter to the spinner.
        timeFrameSpinner.setAdapter(adapter);

        // Set the spinner's onItemSelectedListener if you need to handle selection events
        timeFrameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView) view).setTextColor(ContextCompat.getColor(timeFrameSpinner.getContext(), R.color.spotify_white));
                ((TextView) view).setTextSize(18);
                // Create a Typeface from the font resource
                Typeface spotifyTypeface = ResourcesCompat.getFont(timeFrameSpinner.getContext(), R.font.spotify_font);
                Typeface boldSpotifyTypeface = Typeface.create(spotifyTypeface, Typeface.BOLD);
                ((TextView) view).setTypeface(spotifyTypeface);
                ((TextView) view).setPadding(8, 0, 0, 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });
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
                                    for (int i = 0; i < 5; i++) {
                                        if (thing.equals("artists")) {
                                            name.add(jsonObject.getJSONArray("items").getJSONObject(i).getString("name"));
                                            url.add(jsonObject.getJSONArray("items").getJSONObject(i).getJSONArray("images").getJSONObject(0).getString("url"));
                                            genre.add(jsonObject.getJSONArray("items").getJSONObject(i).getJSONArray("genres").getString(0));
                                        } else {
                                            name.add(jsonObject.getJSONArray("items").getJSONObject(i).getString("name"));
                                            url.add(jsonObject.getJSONArray("items").getJSONObject(i).getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url"));
                                        }
                                    }
                                    wrap.put(thing, name);
                                    wrap.put(thing + "image", url);
                                    if (thing.equals("artists")) {
                                        wrap.put(thing + "genre", genre);
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
        Spinner spinner = (Spinner) root.findViewById(R.id.timeFrameSpinner);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Assuming you have the current user's ID stored (e.g., as a field in the User object)
        FirebaseUser user = mAuth.getCurrentUser();


        // Reference to the user's document in Firestore
        DocumentReference userRef = db.collection("Accounts").document(user.getUid());
        DocumentReference publicRef = db.collection("Accounts").document("bIQXuN4oAPUWGUx6ikPoDw1cjx62");

        binding.generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // hide action bar up top
                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                if (actionBar != null) {
                    actionBar.hide();
                    ImageView imageView = getActivity().findViewById(R.id.currentPageIcon);
                    imageView.setVisibility(View.GONE);
                }

                String term = spinner.getSelectedItem().toString().split(" ")[0].toLowerCase();
                EditText name = (EditText) root.findViewById(R.id.editTextName);
                Map<String, Object> wrap = new HashMap<>();
                wrap.put("Name", name.getText().toString());

                DataCompletionHandler handler = updatedWrap -> {
                    // This block will be called once data fetching is complete.
                    getActivity().runOnUiThread(() -> {
//                        TextView testText = (TextView) root.findViewById(R.id.testText);
//                        testText.setText(updatedWrap.toString());
                        Map<String, Object> dataToUpdate = new HashMap<>();

                        userRef.update("wraps", FieldValue.arrayUnion(updatedWrap))
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Public wrap added to array successfully"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error adding public wrap to array", e));

                        Log.d("Firestore CHECK", user.getUid());
                        //int count = GalleryFragment.wrapAdapterP.getItemCount();
                        //GalleryFragment.wrapAdapterP.addWrap(new WrapObject(count, "Wrap #" + (count + 1), ((List<String>) updatedWrap.get("artistsimage")).get(0), ((List<String>) updatedWrap.get("tracksimage")).get(0), ((List<String>) updatedWrap.get("artists")).get(0), ((List<String>) updatedWrap.get("tracks")).get(0)));
                        currentUser.addWrap(updatedWrap);
                        RadioButton pub = (RadioButton) root.findViewById(R.id.radioButton2);
                        if (pub.isChecked()) {

                            publicRef.update("wraps", FieldValue.arrayUnion(updatedWrap))
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Public wrap added to array successfully"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error adding public wrap to array", e));

                            Log.d("Firestore CHECK", user.getUid());
                            //count = PublicFragment.wrapAdapter.getItemCount();
                            //PublicFragment.wrapAdapter.addWrap(new WrapObject(count, "Wrap #" + (count + 1), ((List<String>) updatedWrap.get("artistsimage")).get(0), ((List<String>) updatedWrap.get("tracksimage")).get(0), ((List<String>) updatedWrap.get("artists")).get(0), ((List<String>) updatedWrap.get("tracks")).get(0)));
                        }
                        // switch to slideshow view
                        NavController navController = Navigation.findNavController(v);
                        navController.navigate(R.id.nav_topSong);
                    });
                };

                onWrapMade(context, root, mOkHttpClient, term, wrap, handler);

            }
        });


        return root;
    }
}