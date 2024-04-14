package com.example.spotifywrapped;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.bumptech.glide.Glide;
import com.example.spotifywrapped.user.User;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;


import com.example.spotifywrapped.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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

    interface CallbackTwo {
        void onComplete(boolean isValid);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        publicID = "bIQXuN4oAPUWGUx6ikPoDw1cjx62";
        mAuth = FirebaseAuth.getInstance();
        navigationView = findViewById(R.id.nav_view);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //currentUser = loadUser();
        instance = this;
        currentUser = loadUser();
        context = MainActivity.this;
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
                            return;
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
    }

//    private String fetchAccessTokenFirebase() {
//        return;
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
        binding.appBarMain.toolbar.setVisibility(View.GONE);
        // Navigate to the Login Fragment immediately
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.nav_login); // Adjust this ID based on your navigation graph
    }

    private void navigateToNewWrapFragment() {
        // Ensure the AppBar (Toolbar) is not shown for the Login Fragment
        binding.appBarMain.toolbar.setVisibility(View.GONE);
        // Navigate to the Login Fragment immediately
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.nav_addWrap); // Adjust this ID based on your navigation graph
    }

    private void updateToolbarForLoggedOutUser() {
        setSupportActionBar(binding.appBarMain.toolbar);
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
        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_public, R.id.nav_slideshow, R.id.nav_settings) // Add or remove IDs as needed
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        // Show the AppBar (Toolbar) if it was previously hidden
        binding.appBarMain.toolbar.setVisibility(View.VISIBLE);
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

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
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