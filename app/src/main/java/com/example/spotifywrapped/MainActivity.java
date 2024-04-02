package com.example.spotifywrapped;

import android.content.ClipData;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.example.spotifywrapped.databinding.FragmentLogInBinding;
import com.example.spotifywrapped.user.User;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
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

import org.w3c.dom.Text;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


    private AppBarConfiguration mAppBarConfiguration;
    private static ActivityMainBinding binding;
    private static User currentUser;
    private static NavigationView navigationView;
    private FirebaseAuth mAuth;

    private static MainActivity instance;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        navigationView = findViewById(R.id.nav_view);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //currentUser = loadUser();
        instance = this;
        currentUser = loadUser();
        if (mAuth.getCurrentUser() != null && currentUser != null) {
            // User is still logged in with Firebase, load the user profile
            onLoginSuccess(currentUser.getName(), currentUser.getEmail());
            if (currentUser.getmAccessToken() == null) {
                navigateToSpotifyLoginFragment();
            } else {
                setupNavigationAndToolbar();
                //onLoginSuccess(currentUser.getName(), currentUser.getEmail());
            }
        } else {
            // No Firebase session, user needs to log in again
            navigateToLoginFragment();
        }
        //TODO: NEED TO IMPLEMENT THE LOG OUT BUTTON
    }

    public static MainActivity getInstance() {
        return instance;
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

    private void navigateToSpotifyLoginFragment() {
        // Ensure the AppBar (Toolbar) is not shown for the Login Fragment
        binding.appBarMain.toolbar.setVisibility(View.GONE);
        // Navigate to the Login Fragment immediately
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.spotifyLoginFragment); // Adjust this ID based on your navigation graph
    }

    public void setupNavigationAndToolbar() {
        // Set up Toolbar
        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow) // Add or remove IDs as needed
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