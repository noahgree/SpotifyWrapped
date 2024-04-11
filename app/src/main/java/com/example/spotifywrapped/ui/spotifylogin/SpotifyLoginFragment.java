package com.example.spotifywrapped.ui.spotifylogin;

import static com.example.spotifywrapped.MainActivity.context;
import static com.example.spotifywrapped.MainActivity.updateUserProfilePhoto;

import android.app.Activity;
import android.os.Bundle;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifywrapped.databinding.FragmentHomeBinding;
import com.example.spotifywrapped.databinding.FragmentLogInBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.example.spotifywrapped.databinding.FragmentSpotifyLoginBinding;
public class SpotifyLoginFragment extends Fragment {

    public static final String CLIENT_ID = "b063d7ec2077456d898909d249da6e49";
    public static final String REDIRECT_URI = "com.example.spotifywrapped://auth";

    public static final int AUTH_TOKEN_REQUEST_CODE = 0;
    public static final int AUTH_CODE_REQUEST_CODE = 1;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mAccessToken, mAccessCode;
    private Call mCall;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ActivityResultLauncher<Intent> spotifyAuthLauncher;

    private AppCompatButton loginButton;
    private FragmentSpotifyLoginBinding binding;

    public SpotifyLoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity mainActivity = MainActivity.getInstance();

        // Initialize the ActivityResultLauncher
        spotifyAuthLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        final AuthorizationResponse response = AuthorizationClient.getResponse(result.getResultCode(), result.getData());
                        if (response.getType() == AuthorizationResponse.Type.TOKEN) {
                            MainActivity.saveSpotifyToken(response.getAccessToken());
                            mainActivity.setupNavigationAndToolbar();
                            updateUserProfilePhoto();
                            mAuth = FirebaseAuth.getInstance();
                            db = FirebaseFirestore.getInstance();
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Map<String, Object> userAccount = new HashMap<>();
                                userAccount.put("token", response.getAccessToken());

                                db.collection("Accounts").document(user.getUid())
                                        .set(userAccount, SetOptions.merge())
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                                        .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                            }
                            NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_content_main);
                            navController.navigate(R.id.nav_gallery);
                        }
                    }
                });

        if (MainActivity.isSpotifyLoggedIn()) {
            // User is already logged in with Spotify, navigate away or load data

        }

        binding.spotifySignIn.setOnClickListener(v -> getToken());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSpotifyLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        loginButton = binding.spotifySignIn;
        loginButton.setOnClickListener((v) -> {
            getToken();
        });
        // Inflate the layout for this fragment
        return root;
    }
    /**
     * Get token from Spotify
     * This method will open the Spotify login activity and get the token
     * What is token?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */

    public void getToken() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
        Intent intent = AuthorizationClient.createLoginActivityIntent(getActivity(), request);
        spotifyAuthLauncher.launch(intent);
    }
    /**
     * Get code from Spotify
     * This method will open the Spotify login activity and get the code
     * What is code?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public void getCode() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.CODE);
        AuthorizationClient.openLoginActivity(getActivity(), AUTH_CODE_REQUEST_CODE, request);
    }


//    /**
//     * When the app leaves this activity to momentarily get a token/code, this function
//     * fetches the result of that external activity to get the response from Spotify
//     */
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);
//
//        // Check which request code is present (if any)
//        if (AUTH_TOKEN_REQUEST_CODE == requestCode) {
//            mAccessToken = response.getAccessToken();
//            setTextAsync(mAccessToken, tokenTextView);
//
//        } else if (AUTH_CODE_REQUEST_CODE == requestCode) {
//            mAccessCode = response.getCode();
//            setTextAsync(mAccessCode, codeTextView);
//        }
//    }

    /**
     * Get user profile
     * This method will get the user profile using the token
     */
    public void onGetUserProfileClicked() {
        if (mAccessToken == null) {
            Toast.makeText(getActivity(), "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request to get the user profile
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                Toast.makeText(getContext(), "Failed to fetch data, watch Logcat for more details",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    //setTextAsync(jsonObject.toString(3), profileTextView);
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    Toast.makeText(getContext(), "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Creates a UI thread to update a TextView in the background
     * Reduces UI latency and makes the system perform more consistently
     *
     * @param text the text to set
     * @param textView TextView object to update
     */
    private void setTextAsync(final String text, TextView textView) {
        //runOnUiThread(() -> textView.setText(text));
    }

    /**
     * Get authentication request
     *
     * @param type the type of the request
     * @return the authentication request
     */
    private AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type) {
        return new AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                .setShowDialog(false)
                .setScopes(new String[] { "user-read-email", "user-top-read" })
                .setCampaign("your-campaign-token")
                .build();
    }

    /**
     * Gets the redirect Uri for Spotify
     *
     * @return redirect Uri object
     */
    private Uri getRedirectUri() {
        return Uri.parse(REDIRECT_URI);
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    @Override
    public void onDestroy() {
        cancelCall();
        super.onDestroy();
    }
}