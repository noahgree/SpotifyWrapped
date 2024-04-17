package com.example.spotifywrapped;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.bumptech.glide.Glide;
import com.example.spotifywrapped.user.User;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.example.spotifywrapped.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final OkHttpClient mOkHttpClient = new OkHttpClient();
    private AppBarConfiguration mAppBarConfiguration;
    private static ActivityMainBinding binding;
    private static User currentUser;
    private static NavigationView navigationView;
    private FirebaseAuth mAuth;

    private static MainActivity instance;

    private String publicID;

    public static Context context;

    private FirebaseFirestore db;

    private static String theme = "Default";

    interface CallbackTwo {
        void onComplete(boolean isValid);
    }

    public interface TrackUriCallback {
        void onCompleted(String trackUri);
        void onError(String errorMessage);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navigationView = findViewById(R.id.nav_view);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        instance = this;
        context = MainActivity.this;

        EdgeToEdge.enable(this);

        CoordinatorLayout appBarLayoutMA = binding.getRoot().findViewById(R.id.main_app_bar);
        FrameLayout toolbarFrame = appBarLayoutMA.findViewById(R.id.toolbarFrame);
        NavigationView navView = binding.getRoot().findViewById(R.id.nav_view);
        applyInsetsListener(toolbarFrame, false, false, true, false);
        applyInsetsListenerPadding(navView, false, false, true, true);

        // Ensure the content extends into the system bars by adjusting the window.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        // Ensure status bar icons are light
        WindowInsetsController insetsController = getWindow().getInsetsController();
        if (insetsController != null) {
            // This tells the system that your UI is dark and status bar icons should be light.
            insetsController.setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            );
            insetsController.hide(WindowInsets.Type.navigationBars());
            insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }


        publicID = "vGLXVzArF0OObsE5bJT4jNpdOy33";
        mAuth = FirebaseAuth.getInstance();

        currentUser = loadUser();
        checkForHolidays();
        //Deals with if user needs to log in or refresh the spotify token.
        if (mAuth.getCurrentUser() != null && currentUser != null) {
            // User is still logged in with Firebase, load the user profile
            onLoginSuccess(currentUser.getName(), currentUser.getEmail());
            updateUserProfilePhoto();
            if (currentUser.getmAccessToken() == null) {
                Log.d(TAG, "NO ACCESS TOKEN");
                navigateToSpotifyLoginFragment();
            } else {
                isSpotifyTokenValid(new CallbackTwo() {
                    @Override
                    public void onComplete(boolean isValid) {
                        if (!isValid) {
                            navigateToSpotifyLoginFragment();
                        } else {
                            //TODO: Make it so if the token from the saved user is not valid, then it also checks the token stored in firebase
                            setupNavigationAndToolbar();
                        }
                    }
                });
            }
        } else {
            navigateToLoginFragment();
        }
        //TODO: NEED TO IMPLEMENT THE LOG OUT BUTTON

        // For swapping toolbar icon depending on current layout
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        ImageView currentPageIcon = binding.getRoot().findViewById(R.id.currentPageIcon);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destId = destination.getId();

            FrameLayout bgFrame = binding.getRoot().findViewById(R.id.bgFrame);
            ImageView imageView = binding.getRoot().findViewById(R.id.imageViewBG);
            imageView.setImageResource(R.drawable.stripes_image);

            Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
            Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                currentPageIcon.startAnimation(slideDown);
                if (destId == R.id.nav_home) {
                    bgFrame.setBackgroundColor(ContextCompat.getColor(context, R.color.spotify_black));
                    bgFrame.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0AFFFFFF")));

                    currentPageIcon.setImageResource(R.drawable.key);
                } else if (destId == R.id.nav_gallery) {
                    bgFrame.setBackgroundColor(ContextCompat.getColor(context, R.color.spotify_black));
                    bgFrame.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0AFFFFFF")));

                    currentPageIcon.setImageResource(R.drawable.rectangle_stack_person_crop_fill);
                } else if (destId == R.id.nav_public) {
                    bgFrame.setBackgroundColor(ContextCompat.getColor(context, R.color.spotify_black));
                    bgFrame.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0AFFFFFF")));

                    currentPageIcon.setImageResource(R.drawable.people_nearby);
                } else if (destId == R.id.nav_games) {
                    bgFrame.setBackgroundColor(ContextCompat.getColor(context, R.color.spotify_black));
                    bgFrame.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0AFFFFFF")));

                    currentPageIcon.setImageResource(R.drawable.gameboy_solid);
                } else if (destId == R.id.nav_settings) {
                    bgFrame.setBackgroundColor(ContextCompat.getColor(context, R.color.spotify_black));
                    bgFrame.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0AFFFFFF")));

                    currentPageIcon.setImageResource(R.drawable.settings);
                } else if (destId == R.id.nav_addWrap) {
                    bgFrame.setBackgroundColor(ContextCompat.getColor(context, R.color.spotify_black));
                    bgFrame.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0AFFFFFF")));

                    currentPageIcon.setImageResource(R.drawable.rectangle_stack_fill_badge_plus);
                }
                else {
                    currentPageIcon.setImageResource(R.drawable.rectangle_stack_fill); // Fallback icon

                    if (destId == R.id.nav_topSong) {
                        bgFrame.setBackgroundTintList(null);

                        FrameLayout frameLayout = binding.getRoot().findViewById(R.id.topSongLayout);
                        AnimationDrawable animDrawable = (AnimationDrawable) frameLayout.getBackground();
                        frameLayout.setBackground(new ColorDrawable(Color.TRANSPARENT));
                        bgFrame.setBackground(animDrawable);
                        AnimationDrawable animDrawable2 = (AnimationDrawable) bgFrame.getBackground();
                        animDrawable2.setEnterFadeDuration(2500);
                        animDrawable2.setExitFadeDuration(5000);
                        animDrawable2.start();
                    } else if (destId == R.id.nav_top5Songs) {
                        bgFrame.setBackgroundTintList(null);

                        FrameLayout frameLayout = binding.getRoot().findViewById(R.id.top5SongsLayout);
                        AnimationDrawable animDrawable = (AnimationDrawable) frameLayout.getBackground();
                        frameLayout.setBackground(new ColorDrawable(Color.TRANSPARENT));
                        bgFrame.setBackground(animDrawable);
                        AnimationDrawable animDrawable2 = (AnimationDrawable) bgFrame.getBackground();
                        animDrawable2.setEnterFadeDuration(2500);
                        animDrawable2.setExitFadeDuration(5000);
                        animDrawable2.start();
                    } else if (destId == R.id.nav_topArtist) {
                        bgFrame.setBackgroundTintList(null);

                        FrameLayout frameLayout = binding.getRoot().findViewById(R.id.topArtistLayout);
                        AnimationDrawable animDrawable = (AnimationDrawable) frameLayout.getBackground();
                        frameLayout.setBackground(new ColorDrawable(Color.TRANSPARENT));
                        bgFrame.setBackground(animDrawable);
                        AnimationDrawable animDrawable2 = (AnimationDrawable) bgFrame.getBackground();
                        animDrawable2.setEnterFadeDuration(2500);
                        animDrawable2.setExitFadeDuration(5000);
                        animDrawable2.start();
                    } else if (destId == R.id.nav_top5Artists) {
                        bgFrame.setBackgroundTintList(null);

                        FrameLayout frameLayout = binding.getRoot().findViewById(R.id.top5ArtistsLayout);
                        AnimationDrawable animDrawable = (AnimationDrawable) frameLayout.getBackground();
                        frameLayout.setBackground(new ColorDrawable(Color.TRANSPARENT));
                        bgFrame.setBackground(animDrawable);
                        AnimationDrawable animDrawable2 = (AnimationDrawable) bgFrame.getBackground();
                        animDrawable2.setEnterFadeDuration(2500);
                        animDrawable2.setExitFadeDuration(5000);
                        animDrawable2.start();
                    } else if (destId == R.id.nav_topGenre) {
                        bgFrame.setBackgroundTintList(null);

                        FrameLayout frameLayout = binding.getRoot().findViewById(R.id.topGenreLayout);
                        AnimationDrawable animDrawable = (AnimationDrawable) frameLayout.getBackground();
                        frameLayout.setBackground(new ColorDrawable(Color.TRANSPARENT));
                        bgFrame.setBackground(animDrawable);
                        AnimationDrawable animDrawable2 = (AnimationDrawable) bgFrame.getBackground();
                        animDrawable2.setEnterFadeDuration(2500);
                        animDrawable2.setExitFadeDuration(5000);
                        animDrawable2.start();
                    } else if (destId == R.id.nav_wrappedSummary) {
                        bgFrame.setBackgroundTintList(null);

                        FrameLayout frameLayout = binding.getRoot().findViewById(R.id.wrapSummaryLayout);
                        AnimationDrawable animDrawable = (AnimationDrawable) frameLayout.getBackground();
                        frameLayout.setBackground(new ColorDrawable(Color.TRANSPARENT));
                        bgFrame.setBackground(animDrawable);
                        AnimationDrawable animDrawable2 = (AnimationDrawable) bgFrame.getBackground();
                        animDrawable2.setEnterFadeDuration(2500);
                        animDrawable2.setExitFadeDuration(5000);
                        animDrawable2.start();

                        imageView.setImageResource(R.drawable.stars_image);
                    }
                }
                Log.d("NavController", "Destination ID: " + destId);
            }, 0);
        });
    }

    public static String getThemeName() {
        return theme;
    }

    public static void setTheme(String theme) {
        MainActivity.theme = theme;
    }

    public static void updateForHoliday(ViewBinding binding) {
        ImageView imageView = (ImageView) binding.getRoot().findViewById(R.id.topSongPattern);
        if (theme.equals("Christmas")) {
            Glide.with(context)
                    .load(R.drawable.christmas_pattern)
                    .into(imageView);
            imageView.setImageAlpha(100);
        }
        if (theme.equals("Valentines")) {
            Glide.with(context)
                    .load(R.drawable.valentines_pattern)
                    .into(imageView);
            imageView.setImageAlpha(100);

        }
        if (theme.equals("Halloween")) {
            Glide.with(context)
                    .load(R.drawable.halloween_pattern)
                    .into(imageView);
            imageView.setImageAlpha(100);
        }
    }

    private void checkForHolidays() {
        Calendar now = Calendar.getInstance();
        int month = now.get(Calendar.MONTH); // Note: January is 0, December is 11
        int day = now.get(Calendar.DAY_OF_MONTH);

        if (month == Calendar.DECEMBER && day == 25) {
            theme = "Christmas";
        } else if (month == Calendar.FEBRUARY && day == 14) {
            theme = "Valentines";
        } else if (month == Calendar.OCTOBER && day == 31) {
            theme = "Halloween";
        } else {
            theme = "Default"; // Default theme when it's not a special holiday
        }
    }

    private void applyInsetsListener(View view, boolean left, boolean right, boolean top, boolean bottom) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            if (left) {
                mlp.leftMargin = insets.left;
            }
            if (right) {
                mlp.rightMargin = insets.right;
            }
            if (top) {
                mlp.topMargin = insets.top;
            }
            if (bottom) {
                mlp.bottomMargin = insets.bottom;
            }

            v.setLayoutParams(mlp);

            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void applyInsetsListenerPadding(View view, boolean left, boolean right, boolean top, boolean bottom) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            if (left) {
                v.setPadding(insets.left, v.getPaddingTop(), v.getPaddingRight(), v.getPaddingBottom());
            }
            if (right) {
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), insets.right, v.getPaddingBottom());
            }
            if (top) {
                v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), v.getPaddingBottom());
            }
            if (bottom) {
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(),insets.bottom);
            }

            return WindowInsetsCompat.CONSUMED;
        });
    }

    public static void fetchTrackUriFromSongName(String songName, TrackUriCallback callback) {
        if (!isSpotifyLoggedIn()) {
            callback.onError("User is not logged in to Spotify.");
            return;
        }

        String accessToken = currentUser.getmAccessToken();
        OkHttpClient client = new OkHttpClient();
        String encodedSongName;
        try {
            encodedSongName = URLEncoder.encode(songName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            callback.onError("Error encoding the song name.");
            return;
        }

        String searchUrl = "https://api.spotify.com/v1/search?q=" + encodedSongName + "&type=track&limit=1";

        Request request = new Request.Builder()
                .url(searchUrl)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to search song: " + e.getMessage());
                new Handler(Looper.getMainLooper()).post(() -> callback.onError("Failed to search for the song."));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Search failed: " + response.message());
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError("Failed to fetch song URI."));
                    return;
                }

                String responseData = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    JSONObject tracks = jsonObject.getJSONObject("tracks");
                    JSONArray items = tracks.getJSONArray("items");

                    if (items.length() > 0) {
                        String trackUri = items.getJSONObject(0).getString("uri");
                        new Handler(Looper.getMainLooper()).post(() -> callback.onCompleted(trackUri));
                    } else {
                        new Handler(Looper.getMainLooper()).post(() -> callback.onError("No track found for the given song name."));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse data: " + e.getMessage());
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError("Failed to parse track data."));
                }
            }
        });
    }

//    public static void playSong(String trackUri) {
//        if (!isSpotifyLoggedIn()) {
//            Toast.makeText(context, "You need to log in to Spotify first!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        String accessToken = currentUser.getmAccessToken();
//        OkHttpClient client = new OkHttpClient();
//        String playUrl = "https://api.spotify.com/v1/me/player/play";
//
//        JSONObject json = new JSONObject();
//        try {
//            JSONArray uris = new JSONArray();
//            uris.put(trackUri);
//            json.put("uris", uris);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.toString());
//        Request request = new Request.Builder()
//                .url(playUrl)
//                .addHeader("Authorization", "Bearer " + accessToken)
//                .post(body)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.e(TAG, "Failed to play song: " + e.getMessage());
//                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Failed to play song", Toast.LENGTH_SHORT).show());
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (!response.isSuccessful()) {
//                    Log.e(TAG, "Failed to play song: " + response.message());
//                    new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Failed to play song", Toast.LENGTH_SHORT).show());
//                } else {
//                    Log.d(TAG, "Song played successfully.");
//                    new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Song played successfully!", Toast.LENGTH_SHORT).show());
//                }
//            }
//        });
//    }


    public static MainActivity getInstance() {
        return instance;
    }

    private void isSpotifyTokenValid(final CallbackTwo callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Request request = new Request.Builder()
                        .url("https://api.spotify.com/v1/me")
                        .addHeader("Authorization", "Bearer " + currentUser.getmAccessToken())
                        .build();

                try {
                    Response response = mOkHttpClient.newCall(request).execute();
                    // If the response is successful, the token is valid
                    boolean isValid = response.isSuccessful();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        callback.onComplete(isValid);
                    });
                } catch (IOException e) {
                    Log.d("SpotifyTokenCheck", "Failed to validate token: " + e);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        callback.onComplete(false);
                    });
                }
            }
        }).start();
    }


    public static void saveSpotifyToken(String token) {
        SharedPreferences sharedPreferences = getInstance().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("SpotifyToken", token);
        editor.apply();
        currentUser.setmAccessToken(token);
    }

    public static String getSpotifyToken() {
        SharedPreferences sharedPreferences = getInstance().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("SpotifyToken", null);
    }

    public static boolean isSpotifyLoggedIn() {
        return getSpotifyToken() != null;
    }


    private User loadUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String userJson = sharedPreferences.getString("CurrentUser", null);

        return gson.fromJson(userJson, User.class);
    }

    private void navigateToLoginFragment() {
        // Ensure the AppBar (Toolbar) is not shown for the Login Fragment
        binding.mainAppBar.toolbar.setVisibility(View.GONE);
        // Navigate to the Login Fragment immediately
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.nav_login); // Adjust this ID based on your navigation graph
    }

    private void navigateToNewWrapFragment() {
        // Ensure the AppBar (Toolbar) is not shown for the Login Fragment
        binding.mainAppBar.toolbar.setVisibility(View.GONE);
        // Navigate to the Login Fragment immediately
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.nav_addWrap); // Adjust this ID based on your navigation graph
    }

    private void updateToolbarForLoggedOutUser() {
        setSupportActionBar(binding.mainAppBar.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false); // Remove the back/up button
    }

    private void navigateToSpotifyLoginFragment() {
        // Ensure the AppBar (Toolbar) is not shown for the Login Fragment
//        binding.appBarMain.toolbar.setVisibility(View.GONE);
//        // Navigate to the Login Fragment immediately
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        navController.navigate(R.id.spotifyLoginFragment); // Adjust this ID based on your navigation graph
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.popBackStack(R.id.nav_home, true); // Clear back stack up to home
        navController.navigate(R.id.nav_login);
    }

    public void setupNavigationAndToolbar() {
        // Set up Toolbar
        setSupportActionBar(binding.mainAppBar.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_public, R.id.nav_games, R.id.nav_settings) // Add or remove IDs as needed
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Show the AppBar (Toolbar) if it was previously hidden
        binding.mainAppBar.toolbar.setVisibility(View.VISIBLE);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Updates new values with the current user.
    public static void onLoginSuccess(String name, String email) {
        NavigationView navigationView = (NavigationView) binding.navView;
        View navView = navigationView.getHeaderView(0);
        //Side Navigation
        TextView navName = navView.findViewById(R.id.navName);

        TextView navEmail = navView.findViewById(R.id.navEmail);
        navName.setText(name);
        navEmail.setText(email);
        //Wrapped Fragment
    }

    private void logoutUser() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Optional: Clear any stored data (e.g., SharedPreferences)
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Optional: Navigate the user to the login screen
        navigateToLoginFragment();
        updateToolbarForLoggedOutUser();
        // Show a message to the user
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    public static void updateUserProfilePhoto() {
        onGetUserProfileClicked(context, binding.navView, currentUser, mOkHttpClient);
    }

    //Loading profile image
    public static void onGetUserProfileClicked(Context context, NavigationView navigationView, User user, OkHttpClient okHttpClient) {
        View navView = navigationView.getHeaderView(0);

        if (user.getmAccessToken() == null) {
            Toast.makeText(context, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .addHeader("Authorization", "Bearer " + user.getmAccessToken())
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
                            if (jsonObject.getJSONArray("images").length() > 0) {
                                String imageUrl = jsonObject.getJSONArray("images").getJSONObject(0).getString("url");
                                // Use Glide to load the image into the ImageView
                                ImageView profilePhoto = (ImageView) navView.findViewById(R.id.profilePhoto);
                                if (profilePhoto != null) {
                                    Log.d("ImageURL", imageUrl);
                                    Glide.with(context).load(imageUrl).into(profilePhoto);
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


    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        MainActivity.currentUser = currentUser;
    }
    //SharedPreferences
    @Override
    protected void onPause() {
        super.onPause();
        saveUser(currentUser);
    }

    private void saveUser(User user) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String userJson = gson.toJson(user);
        editor.putString("CurrentUser", userJson);
        editor.apply();
    }
}